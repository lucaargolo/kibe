package io.github.lucaargolo.kibe.blocks.tank

import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.interactPlayerHand
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class Tank: BlockWithEntity(FabricBlockSettings.of(Material.GLASS).strength(0.5F).nonOpaque().luminance { state -> state[Properties.LEVEL_15] }.sounds(BlockSoundGroup.GLASS)) {

    init {
        defaultState = stateManager.defaultState.with(Properties.LEVEL_15, 0)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.LEVEL_15)
        super.appendProperties(builder)
    }

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return TankBlockEntity(this, blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, blockState: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return checkType(blockEntityType, getEntityType(this)) { wrld, pos, state, blockEntity -> TankBlockEntity.tick(wrld, pos, state, blockEntity as TankBlockEntity) }
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos): Int {
        return StorageUtil.calculateComparatorOutput((world.getBlockEntity(pos) as? TankBlockEntity)?.tank, null)
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? TankBlockEntity)?.let {
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        return (world.getBlockEntity(pos) as? TankBlockEntity)?.let { interactPlayerHand(it.tank, player, hand) } ?: ActionResult.FAIL
    }

    override fun afterBreak(world: World?, player: PlayerEntity?, pos: BlockPos?, state: BlockState?, blockEntity: BlockEntity?, stack: ItemStack?) {
        (blockEntity as? TankBlockEntity)?.markBroken()
        super.afterBreak(world, player, pos, state, blockEntity, stack)
    }

}