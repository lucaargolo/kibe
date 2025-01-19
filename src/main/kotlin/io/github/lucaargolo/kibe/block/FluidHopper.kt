package io.github.lucaargolo.kibe.block

import com.mojang.serialization.MapCodec
import io.github.lucaargolo.kibe.blockentity.BlockEntityCompendium
import io.github.lucaargolo.kibe.blockentity.FluidHopperBlockEntity
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.minecraft.block.BlockState
import net.minecraft.block.HopperBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FluidHopper(settings: Settings): HopperBlock(settings) {

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return FluidHopperBlockEntity(blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World, blockState: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return if (!world.isClient) validateTicker(
            blockEntityType,
            BlockEntityCompendium.FLUID_HOPPER,
            FluidHopperBlockEntity::serverTick
        ) else null
    }

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos): Int {
        return StorageUtil.calculateComparatorOutput((world.getBlockEntity(pos) as? FluidHopperBlockEntity)?.tank)
    }

    override fun onUse(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?,
        hit: BlockHitResult?
    ): ActionResult {
        val hand = Hand.MAIN_HAND
        if (pos == null) return ActionResult.PASS
        val stack = player?.getStackInHand(hand) ?: return ActionResult.PASS
        val context = ContainerItemContext.ofPlayerHand(player, hand)
        val itemTank = FluidStorage.ITEM.find(stack, context) ?: return ActionResult.PASS
        val tank = (world?.getBlockEntity(pos) as? FluidHopperBlockEntity)?.tank ?: return ActionResult.PASS
        if (world.isClient) return ActionResult.SUCCESS

        val inserted = StorageUtil.move(itemTank, tank, { true }, Long.MAX_VALUE, null)
        if (inserted > 0) {
            playEmptyingSound(world, stack, pos)
            return ActionResult.CONSUME
        }

        val extracted = StorageUtil.move(tank, itemTank, { true }, Long.MAX_VALUE, null)
        if (extracted > 0) {
            playFillingSound(world, stack, pos)
            return ActionResult.CONSUME
        }

        return ActionResult.PASS
    }

    private fun playEmptyingSound(world: World, stack: ItemStack, pos: BlockPos) {
        when (stack.item) {
            Items.LAVA_BUCKET -> SoundEvents.ITEM_BUCKET_EMPTY_LAVA
            Items.POTION -> SoundEvents.ITEM_BOTTLE_EMPTY
            else -> SoundEvents.ITEM_BUCKET_EMPTY
        }?.let { world.playSound(null, pos, it, SoundCategory.BLOCKS, 1.0F, 1.0F) }
    }

    private fun playFillingSound(world: World, stack: ItemStack, pos: BlockPos) {
        when (stack.item) {
            Items.GLASS_BOTTLE -> SoundEvents.ITEM_BOTTLE_FILL
            else -> SoundEvents.ITEM_BUCKET_FILL
        }?.let { world.playSound(null, pos, it, SoundCategory.BLOCKS, 1.0F, 1.0F) }
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? FluidHopperBlockEntity)?.let {
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getCodec() = CODEC as MapCodec<HopperBlock>

    companion object {
        private val CODEC: MapCodec<FluidHopper> = createCodec(::FluidHopper)
    }

}