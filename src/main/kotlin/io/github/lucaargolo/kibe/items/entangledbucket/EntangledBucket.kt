package io.github.lucaargolo.kibe.items.entangledbucket

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTank
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntity
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import io.github.lucaargolo.kibe.items.ENTANGLED_BUCKET
import io.github.lucaargolo.kibe.mixin.BucketItemAccessor
import io.github.lucaargolo.kibe.utils.FakePlayerEntity
import io.github.lucaargolo.kibe.utils.plus
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.FluidDrainable
import net.minecraft.block.FluidFillable
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.state.property.Properties
import net.minecraft.tag.FluidTags
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.TranslatableText
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import kotlin.Pair

class EntangledBucket(settings: Settings): Item(settings)  {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val tag = getTag(stack)
        val ownerText = TranslatableText("tooltip.kibe.owner")
        if(tag.getString("key") != EntangledTank.DEFAULT_KEY) tooltip.add(ownerText.append(LiteralText(tag.getString("owner")).formatted(Formatting.GRAY)))
        val color = TranslatableText("tooltip.kibe.color")
        var colorCode = ""
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
            colorCode += dc.id.let { int -> Integer.toHexString(int) }
            val text = LiteralText("■")
            text.style = text.style.withColor(TextColor.fromRgb(dc.materialColor.color))
            color.append(text)
        }
        tag.putString("colorCode", colorCode)
        tooltip.add(color)
        val fluidInv = getFluidInv(world, tag)
        if(!fluidInv.getInvFluid(0).isEmpty)
            tooltip.add(fluidInv.getInvFluid(0).fluidKey.name.shallowCopy().append(LiteralText(": ${Formatting.GRAY}${fluidInv.getInvFluid(0).amount().asInt(1000)}mB")))
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack>? {
        val itemStack = user.getStackInHand(hand)
        val tag = getTag(itemStack)

        val key = tag.getString("key")
        var colorCode = ""
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
            colorCode += dc.id.let { int -> Integer.toHexString(int) }
        }
        tag.putString("colorCode", colorCode)

        val fluidInv = getFluidInv(world, tag)
        val fluid = if(fluidInv.getInvFluid(0).isEmpty) Fluids.EMPTY else fluidInv.getInvFluid(0).rawFluid ?: Fluids.EMPTY
        val hasSpace = (fluidInv.getInvFluid(0).amount() + FluidAmount.BUCKET) <= fluidInv.tankCapacity_F
        val hasBucket = fluidInv.getInvFluid(0).amount() >= FluidAmount.BUCKET

        val hitResult: HitResult = raycast(world, user,
            if (hasSpace) RaycastContext.FluidHandling.SOURCE_ONLY else RaycastContext.FluidHandling.NONE
        )

        return (hitResult as? BlockHitResult)?.let { blockHitResult ->
            val dir = blockHitResult.side
            val pos = blockHitResult.blockPos
            val offsetPos = pos.offset(dir)

            var fakeBucketItem = fluid.bucketItem as? BucketItem ?: Items.BUCKET as BucketItem
            if(user.isSneaking) fakeBucketItem = Items.BUCKET as BucketItem

            if(fakeBucketItem == Items.BUCKET && hasSpace) {
                val interact = fakeInteraction(fakeBucketItem, world, pos, blockHitResult)
                if(interact != null) {
                    user.playSound(if (interact.isIn(FluidTags.LAVA)) SoundEvents.ITEM_BUCKET_FILL_LAVA else SoundEvents.ITEM_BUCKET_FILL, 1.0f, 1.0f)
                    if (!world.isClient) {
                        val serverWorld = world as ServerWorld
                        val state = serverWorld.server.overworld.persistentStateManager.getOrCreate({ EntangledTankState(serverWorld, key) }, key)
                        val stateInv = state.getOrCreateInventory(colorCode)
                        stateInv.attemptInsertion(FluidKeys.get(interact).withAmount(FluidAmount.BUCKET), Simulation.ACTION)
                        state.markDirty(colorCode)
                    }
                    return TypedActionResult.success(itemStack)
                }
            }else if(fakeBucketItem != Items.BUCKET && hasBucket) {
                val interact = fakeInteraction(fakeBucketItem, world, pos, blockHitResult)
                if(interact != null) {
                    val soundEvent = if (fluid.isIn(FluidTags.LAVA)) SoundEvents.ITEM_BUCKET_EMPTY_LAVA else SoundEvents.ITEM_BUCKET_EMPTY
                    world.playSound(user, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f)
                    if (!world.isClient) {
                        val serverWorld = world as ServerWorld
                        val state = serverWorld.server.overworld.persistentStateManager.getOrCreate( { EntangledTankState(serverWorld, key) }, key)
                        val stateInv = state.getOrCreateInventory(colorCode)
                        stateInv.attemptExtraction({it.rawFluid == fluid}, FluidAmount.BUCKET, Simulation.ACTION)
                        state.markDirty(colorCode)
                    }
                    return TypedActionResult.success(itemStack)
                }
            }

            if (world.canPlayerModifyAt(user, pos) && user.canPlaceOn(offsetPos, dir, itemStack)) {
                val blockState = world.getBlockState(pos)
                if (blockState.block is FluidDrainable) {
                    if(hasSpace) {
                        val drainedFluid = (blockState.block as FluidDrainable).tryDrainFluid(world, pos, blockState)
                        if ((fluid != Fluids.EMPTY && drainedFluid == fluid) || (fluid == Fluids.EMPTY && drainedFluid != Fluids.EMPTY)) {
                            user.incrementStat(Stats.USED.getOrCreateStat(this))
                            user.playSound(if (fluid.isIn(FluidTags.LAVA)) SoundEvents.ITEM_BUCKET_FILL_LAVA else SoundEvents.ITEM_BUCKET_FILL, 1.0f, 1.0f)
                            if (!world.isClient) {
                                val serverWorld = world as ServerWorld
                                val state = serverWorld.server.overworld.persistentStateManager.getOrCreate({ EntangledTankState(serverWorld, key) }, key)
                                val stateInv = state.getOrCreateInventory(colorCode)
                                stateInv.attemptInsertion(FluidKeys.get(drainedFluid).withAmount(FluidAmount.BUCKET), Simulation.ACTION)
                                state.markDirty(colorCode)
                                Criteria.FILLED_BUCKET.trigger(user as ServerPlayerEntity, ItemStack(ENTANGLED_BUCKET))
                            }
                            return TypedActionResult.success(itemStack)
                        }else{
                            (drainedFluid.bucketItem as? BucketItem)?.placeFluid(user, world, pos, blockHitResult)
                        }
                    }
                }else {
                    val interactablePos = if (blockState.block is FluidFillable && fluid == Fluids.WATER) pos else offsetPos
                    val interactableBlockState = world.getBlockState(interactablePos)
                    if(hasBucket && (interactableBlockState.block !is FluidDrainable || interactableBlockState[Properties.LEVEL_15] != 0)) {
                        val bucketItem = fluid.bucketItem as BucketItem
                        if (bucketItem.placeFluid(user, world, interactablePos, blockHitResult)) {
                            bucketItem.onEmptied(world, itemStack, interactablePos)
                            if (!world.isClient) {
                                val serverWorld = world as ServerWorld
                                val state = serverWorld.server.overworld.persistentStateManager.getOrCreate( { EntangledTankState(serverWorld, key) }, key)
                                val stateInv = state.getOrCreateInventory(colorCode)
                                stateInv.attemptExtraction({it.rawFluid == fluid}, FluidAmount.BUCKET, Simulation.ACTION)
                                state.markDirty(colorCode)
                                Criteria.PLACED_BLOCK.trigger(user as ServerPlayerEntity, interactablePos, itemStack)
                            }
                            user.incrementStat(Stats.USED.getOrCreateStat(this))
                            return TypedActionResult.success(itemStack)
                        }
                    }
                }

            }
            TypedActionResult.fail(itemStack)

        } ?: TypedActionResult.pass(itemStack)

    }

    @Suppress("DEPRECATION")
    private fun fakeInteraction(bucketItem: BucketItem, world: World, pos: BlockPos, blockHitResult: BlockHitResult): Fluid? {
        val fakePlayer = FakePlayerEntity(world)
        fakePlayer.setStackInHand(Hand.MAIN_HAND, ItemStack(bucketItem))
        val blockState = world.getBlockState(pos)
        val block = blockState.block
        block.onUse(blockState, world, pos, fakePlayer, Hand.MAIN_HAND, blockHitResult)
        val resultStack = fakePlayer.getStackInHand(Hand.MAIN_HAND)
        val resultItem = resultStack.item
        val success = resultItem is BucketItem && ((bucketItem == Items.BUCKET && resultItem != Items.BUCKET) || (bucketItem != Items.BUCKET && resultItem == Items.BUCKET))
        return if(success) (resultItem as BucketItemAccessor).fluid else null
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if(context.player?.isSneaking == true) {
            (context.world.getBlockEntity(context.blockPos) as? EntangledTankEntity)?.let{ blockEntity ->
                val blockEntityTag = blockEntity.toTag(CompoundTag())
                val newTag = CompoundTag()
                newTag.putString("key", blockEntityTag.getString("key"))
                newTag.putString("owner", blockEntityTag.getString("owner"))
                (1..8).forEach {
                    newTag.putString("rune$it", blockEntityTag.getString("rune$it"))
                }
                newTag.putString("colorCode", blockEntity.colorCode)
                context.stack.tag = newTag
                if(!context.world.isClient) context.player!!.sendMessage(TranslatableText("chat.kibe.entangled_bucket.success"), true)
                return ActionResult.SUCCESS
            }

        }
        return ActionResult.PASS
    }

    private fun getFluidInv(world: World?, tag: CompoundTag): SimpleFixedFluidInv {
        val key = tag.getString("key")
        val colorCode = tag.getString("colorCode")
        val fluidInv = if(world is ServerWorld) {
            val state = world.server.overworld.persistentStateManager.getOrCreate( { EntangledTankState(world, key) }, key)
            state.getOrCreateInventory(colorCode)
        }else {
            (MinecraftClient.getInstance().player)?.let { player ->
                val list = EntangledTankState.CLIENT_PLAYER_REQUESTS[player] ?: linkedSetOf()
                list.add(Pair(key, colorCode))
                EntangledTankState.CLIENT_PLAYER_REQUESTS[player] = list
            }
            EntangledTankState.CLIENT_STATES[key]?.fluidInvMap?.get(colorCode) ?: SimpleFixedFluidInv(1, FluidAmount.ONE)
        }
        fluidInv.toTag(tag)
        return fluidInv
    }

    private fun getTag(stack: ItemStack): CompoundTag {
        return if(stack.hasTag()) {
            stack.orCreateTag
        }else{
            val newTag = CompoundTag()
            newTag.putString("key", EntangledTank.DEFAULT_KEY)
            (1..8).forEach {
                newTag.putString("rune$it", DyeColor.WHITE.name)
            }
            newTag.putString("colorCode", "00000000")
            newTag
        }
    }

}