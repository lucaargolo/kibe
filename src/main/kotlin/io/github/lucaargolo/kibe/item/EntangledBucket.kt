package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.block.EntangledTank
import io.github.lucaargolo.kibe.blockentity.EntangledTankEntity
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import io.github.lucaargolo.kibe.data.state.EntangledTankState
import io.github.lucaargolo.kibe.mixin.BucketItemAccessor
import io.github.lucaargolo.kibe.utils.FakeClientPlayerEntity
import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.fabricmc.fabric.api.entity.FakePlayer
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.FluidDrainable
import net.minecraft.block.FluidFillable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.*
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.tag.FluidTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import kotlin.Pair

class EntangledBucket(settings: Settings): Item(settings)  {

    override fun appendTooltip(stack: ItemStack, context: TooltipContext?, tooltip: MutableList<Text>, type: TooltipType?) {
        super.appendTooltip(stack, context, tooltip, type)
        val ownerText = Text.translatable("tooltip.kibe.owner")
        val key = stack.get(ComponentTypeCompendium.ENTANGLED_KEY) ?: EntangledTank.DEFAULT_KEY
        if(key != EntangledTank.DEFAULT_KEY && stack.contains(ComponentTypeCompendium.OWNER))
            tooltip.add(ownerText.append(Text.literal(stack.get(ComponentTypeCompendium.OWNER)).formatted(Formatting.GRAY)))
        val color = Text.translatable("tooltip.kibe.color")
        var colorCode = ""
        if(stack.contains(ComponentTypeCompendium.RUNE_SET)) {
            stack.get(ComponentTypeCompendium.RUNE_SET)?.forEach { dc ->
                colorCode += dc.id.let { int -> Integer.toHexString(int) }
                val text = Text.literal("■")
                text.style = text.style.withColor(TextColor.fromRgb(dc.mapColor.color))
                color.append(text)
            }
        }else{
            colorCode = "00000000"
            color.append(Text.literal("■■■■■■■■"))
        }
        tooltip.add(color)
        val fluidInv = getFluidInv(null, key, colorCode)
        if(!fluidInv.isResourceBlank)
            tooltip.add(FluidVariantAttributes.getName(fluidInv.variant).copyContentOnly().append(Text.literal(": ${Formatting.GRAY}${FluidHelper.getMb(fluidInv.amount)}mB")))

    }

    @Suppress("DEPRECATION")
    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack>? {
        val stack = user.getStackInHand(hand)

        val key = stack.get(ComponentTypeCompendium.ENTANGLED_KEY) ?: EntangledTank.DEFAULT_KEY
        var colorCode = ""
        if(stack.contains(ComponentTypeCompendium.RUNE_SET)) {
            stack.get(ComponentTypeCompendium.RUNE_SET)?.forEach { dc ->
                colorCode += dc.id.let { int -> Integer.toHexString(int) }
            }
        }else{
            colorCode = "00000000"
        }

        val fluidInv = getFluidInv(world, key, colorCode)
        val fluid = if(fluidInv.isResourceBlank) Fluids.EMPTY else fluidInv.variant.fluid ?: Fluids.EMPTY
        val hasSpace = (fluidInv.amount + FluidConstants.BUCKET) <= fluidInv.capacity
        val hasBucket = fluidInv.amount >= FluidConstants.BUCKET

        val blockHitResult = raycast(world, user, if (hasSpace) RaycastContext.FluidHandling.SOURCE_ONLY else RaycastContext.FluidHandling.NONE)

        return if(blockHitResult.type != HitResult.Type.MISS) {
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
                        val state = EntangledTankState.getPersistentState(serverWorld, key)
                        val stateInv = state.getOrCreateInventory(colorCode)
                        Transaction.openOuter().also {
                            stateInv.insert(FluidVariant.of(interact), FluidConstants.BUCKET, it)
                        }.commit()
                        state.markDirty(colorCode)
                    }
                    return TypedActionResult.success(stack)
                }
            }else if(fakeBucketItem != Items.BUCKET && hasBucket) {
                val interact = fakeInteraction(fakeBucketItem, world, pos, blockHitResult)
                if(interact != null) {
                    val soundEvent = if (fluid.isIn(FluidTags.LAVA)) SoundEvents.ITEM_BUCKET_EMPTY_LAVA else SoundEvents.ITEM_BUCKET_EMPTY
                    world.playSound(user, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f)
                    if (!world.isClient) {
                        val serverWorld = world as ServerWorld
                        val state = EntangledTankState.getPersistentState(serverWorld, key)
                        val stateInv = state.getOrCreateInventory(colorCode)
                        Transaction.openOuter().also {
                            stateInv.extract(FluidVariant.of(fluid), FluidConstants.BUCKET, it)
                        }.commit()
                        state.markDirty(colorCode)
                    }
                    return TypedActionResult.success(stack)
                }
            }

            if (world.canPlayerModifyAt(user, pos) && user.canPlaceOn(offsetPos, dir, stack)) {
                val blockState = world.getBlockState(pos)
                if (blockState.block is FluidDrainable) {
                    if(hasSpace) {
                        val drainedFluid = (blockState.block as FluidDrainable).tryDrainFluid(user, world, pos, blockState)
                        if ((fluid != Fluids.EMPTY && drainedFluid.item == fluid.bucketItem) || (fluid == Fluids.EMPTY && drainedFluid.item != Fluids.EMPTY.bucketItem)) {
                            user.incrementStat(Stats.USED.getOrCreateStat(this))
                            user.playSound(if (fluid.isIn(FluidTags.LAVA)) SoundEvents.ITEM_BUCKET_FILL_LAVA else SoundEvents.ITEM_BUCKET_FILL, 1.0f, 1.0f)
                            if (!world.isClient) {
                                val serverWorld = world as ServerWorld
                                val state = EntangledTankState.getPersistentState(serverWorld, key)
                                val stateInv = state.getOrCreateInventory(colorCode)
                                Transaction.openOuter().also {
                                    val containedFluidStorage = ContainerItemContext.withConstant(drainedFluid).find(FluidStorage.ITEM)
                                    StorageUtil.findExtractableContent(containedFluidStorage, it)?.let { extractableContent ->
                                        stateInv.insert(extractableContent.resource, extractableContent.amount, it)
                                    }
                                }.commit()
                                state.markDirty(colorCode)
                                Criteria.FILLED_BUCKET.trigger(user as ServerPlayerEntity, ItemStack(ItemCompendium.ENTANGLED_BUCKET))
                            }
                            return TypedActionResult.success(stack)
                        }
                    }
                }
                val interactablePos = if (blockState.block is FluidFillable && fluid == Fluids.WATER) pos else offsetPos
                val interactableBlockState = world.getBlockState(interactablePos)
                if(hasBucket && (interactableBlockState.block !is FluidDrainable || interactableBlockState.block is FluidFillable || (interactableBlockState.contains(Properties.LEVEL_15) && interactableBlockState[Properties.LEVEL_15] != 0))) {
                    val bucketItem = fluid.bucketItem as BucketItem
                    if (bucketItem.placeFluid(user, world, interactablePos, blockHitResult)) {
                        bucketItem.onEmptied(null, world, stack, interactablePos)
                        if (!world.isClient) {
                            val serverWorld = world as ServerWorld
                            val state = EntangledTankState.getPersistentState(serverWorld, key)
                            val stateInv = state.getOrCreateInventory(colorCode)
                            Transaction.openOuter().also {
                                stateInv.extract(FluidVariant.of(fluid), FluidConstants.BUCKET, it)
                            }.commit()
                            state.markDirty(colorCode)
                            Criteria.PLACED_BLOCK.trigger(user as ServerPlayerEntity, interactablePos, stack)
                        }
                        user.incrementStat(Stats.USED.getOrCreateStat(this))
                        return TypedActionResult.success(stack)
                    }
                }
            }
            TypedActionResult.fail(stack)

        }else TypedActionResult.pass(stack)

    }

    private fun fakeInteraction(bucketItem: BucketItem, world: World, pos: BlockPos, blockHitResult: BlockHitResult): Fluid? {
        val fakePlayer = if(world is ServerWorld) FakePlayer.get(world) else FakeClientPlayerEntity(world)
        fakePlayer.setStackInHand(Hand.MAIN_HAND, ItemStack(bucketItem))
        val blockState = world.getBlockState(pos)
        blockState.onUse(world, fakePlayer, blockHitResult)
        val resultStack = fakePlayer.getStackInHand(Hand.MAIN_HAND)
        val resultItem = resultStack.item
        val success = resultItem is BucketItem && ((bucketItem == Items.BUCKET && resultItem != Items.BUCKET) || (bucketItem != Items.BUCKET && resultItem == Items.BUCKET))
        return if(success) (resultItem as BucketItemAccessor).fluid else null
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if(context.player?.isSneaking == true) {
            (context.world.getBlockEntity(context.blockPos) as? EntangledTankEntity)?.let{ blockEntity ->
                val blockEntityTag = blockEntity.writeClientNbt(NbtCompound(), context.world.registryManager)
                val runeSet = mutableListOf<DyeColor>()
                (1..8).forEach {
                    runeSet.add(DyeColor.byName(blockEntityTag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE)
                }
                context.stack.set(ComponentTypeCompendium.ENTANGLED_KEY, blockEntityTag.getString("key"))
                context.stack.set(ComponentTypeCompendium.OWNER, blockEntityTag.getString("owner"))
                context.stack.set(ComponentTypeCompendium.RUNE_SET, runeSet)
                context.stack.set(ComponentTypeCompendium.COLOR_CODE, blockEntity.colorCode)
                if(!context.world.isClient) context.player!!.sendMessage(Text.translatable("chat.kibe.entangled_bucket.success"), true)
                return ActionResult.SUCCESS
            }

        }
        return ActionResult.PASS
    }

    companion object {
        fun getFluidInv(world: World?, key: String, colorCode: String): SingleVariantStorage<FluidVariant> {
            val fluidInv = if(world is ServerWorld) {
                val state = EntangledTankState.getPersistentState(world, key)
                state.getOrCreateInventory(colorCode)
            }else {
                EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.add(Pair(key, colorCode))
                EntangledTankState.CLIENT_STATES[key]?.fluidInvMap?.get(colorCode) ?: object: SingleVariantStorage<FluidVariant>() {
                    override fun getCapacity(variant: FluidVariant?) = 0L
                    override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
                }
            }
            return fluidInv
        }

    }



}