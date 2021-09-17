@file:Suppress("net.fabricmc.fabric.api.transfer:DEPRECATION", "net.fabricmc.fabric.api.transfer:UnstableApiUsage")

package io.github.lucaargolo.kibe.items.entangledbucket

import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTank
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntity
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import io.github.lucaargolo.kibe.items.ENTANGLED_BUCKET
import io.github.lucaargolo.kibe.mixin.BucketItemAccessor
import io.github.lucaargolo.kibe.utils.FakePlayerEntity
import io.github.lucaargolo.kibe.utils.writeTank
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.FluidDrainable
import net.minecraft.block.FluidFillable
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.*
import net.minecraft.nbt.NbtCompound
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
            text.style = text.style.withColor(TextColor.fromRgb(dc.mapColor.color))
            color.append(text)
        }
        tag.putString("colorCode", colorCode)
        tooltip.add(color)
        val fluidInv = getFluidInv(world, tag)
        if(!fluidInv.isResourceBlank)
            tooltip.add(fluidInv.resource.fluid.defaultState.blockState.block.name.shallowCopy().append(LiteralText(": ${Formatting.GRAY}${fluidInv.amount/81}mB")))
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
        val fluid = if(fluidInv.isResourceBlank) Fluids.EMPTY else fluidInv.variant.fluid ?: Fluids.EMPTY
        val hasSpace = (fluidInv.amount + FluidConstants.BUCKET) <= fluidInv.capacity
        val hasBucket = fluidInv.amount >= FluidConstants.BUCKET

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
                        val state = serverWorld.server.overworld.persistentStateManager.getOrCreate({EntangledTankState.createFromTag(it, serverWorld, key)}, { EntangledTankState(serverWorld, key) }, key)
                        val stateInv = state.getOrCreateInventory(colorCode)
                        Transaction.openOuter().also {
                            stateInv.insert(FluidVariant.of(interact), FluidConstants.BUCKET, it)
                        }.commit()
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
                        val state = serverWorld.server.overworld.persistentStateManager.getOrCreate({EntangledTankState.createFromTag(it, serverWorld, key)}, { EntangledTankState(serverWorld, key) }, key)
                        val stateInv = state.getOrCreateInventory(colorCode)
                        Transaction.openOuter().also {
                            stateInv.extract(FluidVariant.of(fluid), FluidConstants.BUCKET, it)
                        }.commit()
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
                        if ((fluid != Fluids.EMPTY && drainedFluid.item == fluid.bucketItem) || (fluid == Fluids.EMPTY && drainedFluid.item != Fluids.EMPTY.bucketItem)) {
                            user.incrementStat(Stats.USED.getOrCreateStat(this))
                            user.playSound(if (fluid.isIn(FluidTags.LAVA)) SoundEvents.ITEM_BUCKET_FILL_LAVA else SoundEvents.ITEM_BUCKET_FILL, 1.0f, 1.0f)
                            if (!world.isClient) {
                                val serverWorld = world as ServerWorld
                                val state = serverWorld.server.overworld.persistentStateManager.getOrCreate({EntangledTankState.createFromTag(it, serverWorld, key)}, { EntangledTankState(serverWorld, key) }, key)
                                val stateInv = state.getOrCreateInventory(colorCode)
                                Transaction.openOuter().also {
                                    val containedFluidStorage = ContainerItemContext.withInitial(drainedFluid).find(FluidStorage.ITEM)
                                    StorageUtil.findExtractableContent(containedFluidStorage, it)?.let { extractableContent ->
                                        stateInv.insert(extractableContent.resource, extractableContent.amount, it)
                                    }
                                }.commit()
                                state.markDirty(colorCode)
                                Criteria.FILLED_BUCKET.trigger(user as ServerPlayerEntity, ItemStack(ENTANGLED_BUCKET))
                            }
                            return TypedActionResult.success(itemStack)
                        }else{
                            (drainedFluid.item as? BucketItem)?.placeFluid(user, world, pos, blockHitResult)
                        }
                    }
                }else {
                    val interactablePos = if (blockState.block is FluidFillable && fluid == Fluids.WATER) pos else offsetPos
                    val interactableBlockState = world.getBlockState(interactablePos)
                    if(hasBucket && (interactableBlockState.block !is FluidDrainable || (interactableBlockState.contains(Properties.LEVEL_15) && interactableBlockState[Properties.LEVEL_15] != 0))) {
                        val bucketItem = fluid.bucketItem as BucketItem
                        if (bucketItem.placeFluid(user, world, interactablePos, blockHitResult)) {
                            bucketItem.onEmptied(null, world, itemStack, interactablePos)
                            if (!world.isClient) {
                                val serverWorld = world as ServerWorld
                                val state = serverWorld.server.overworld.persistentStateManager.getOrCreate({EntangledTankState.createFromTag(it, serverWorld, key)}, { EntangledTankState(serverWorld, key) }, key)
                                val stateInv = state.getOrCreateInventory(colorCode)
                                Transaction.openOuter().also {
                                    stateInv.extract(FluidVariant.of(fluid), FluidConstants.BUCKET, it)
                                }.commit()
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
                val blockEntityTag = blockEntity.writeNbt(NbtCompound())
                val newTag = NbtCompound()
                newTag.putString("key", blockEntityTag.getString("key"))
                newTag.putString("owner", blockEntityTag.getString("owner"))
                (1..8).forEach {
                    newTag.putString("rune$it", blockEntityTag.getString("rune$it"))
                }
                newTag.putString("colorCode", blockEntity.colorCode)
                context.stack.nbt = newTag
                if(!context.world.isClient) context.player!!.sendMessage(TranslatableText("chat.kibe.entangled_bucket.success"), true)
                return ActionResult.SUCCESS
            }

        }
        return ActionResult.PASS
    }

    private fun getFluidInv(world: World?, tag: NbtCompound): SingleVariantStorage<FluidVariant> {
        val key = tag.getString("key")
        val colorCode = tag.getString("colorCode")
        val fluidInv = if(world is ServerWorld) {
            val state = world.server.overworld.persistentStateManager.getOrCreate( {EntangledTankState.createFromTag(it, world, key) }, { EntangledTankState(world, key) }, key)
            state.getOrCreateInventory(colorCode)
        }else {
            EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.add(Pair(key, colorCode))
            EntangledTankState.CLIENT_STATES[key]?.fluidInvMap?.get(colorCode) ?: object: SingleVariantStorage<FluidVariant>() {
                override fun getCapacity(variant: FluidVariant?) = 0L
                override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
            }
        }
        writeTank(tag, fluidInv)
        return fluidInv
    }

    private fun getTag(stack: ItemStack): NbtCompound {
        return if(stack.hasNbt()) {
            stack.orCreateNbt
        }else{
            val newTag = NbtCompound()
            newTag.putString("key", EntangledTank.DEFAULT_KEY)
            (1..8).forEach {
                newTag.putString("rune$it", DyeColor.WHITE.name)
            }
            newTag.putString("colorCode", "00000000")
            newTag
        }
    }

}