package io.github.lucaargolo.kibe.block

import com.mojang.serialization.MapCodec
import io.github.lucaargolo.kibe.blockentity.BlockEntityCompendium
import io.github.lucaargolo.kibe.blockentity.RedstoneTimerEntity
import io.github.lucaargolo.kibe.utils.SyncableBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class RedstoneTimer(settings: Settings): BlockWithEntity(settings) {

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return RedstoneTimerEntity(blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, blockState: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return validateTicker(blockEntityType, BlockEntityCompendium.REDSTONE_TIMER, RedstoneTimerEntity::tick)
    }

    override fun emitsRedstonePower(state: BlockState) = true

    init {
        defaultState = stateManager.defaultState.with(Properties.ENABLED, false)
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.ENABLED)
    }

    override fun getWeakRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, facing: Direction): Int {
        return if(state[Properties.ENABLED]) 15 else 0
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hit: BlockHitResult): ActionResult {
        if(!world.isClient) {
            val blockEntity = world.getBlockEntity(pos)
            if(blockEntity is RedstoneTimerEntity) {
                val level = blockEntity.level
                if(player.isSneaking) {
                    if(level > 0) blockEntity.level = level-1
                    else blockEntity.level = 15
                }else{
                    if(level < 15) blockEntity.level = level+1
                    else blockEntity.level = 0
                }
                blockEntity.markDirty()
            }
            (blockEntity as SyncableBlockEntity).sync()
        }
        return ActionResult.SUCCESS
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun getCodec() = CODEC

    companion object {
        private val CODEC: MapCodec<RedstoneTimer> = createCodec(::RedstoneTimer)
    }

}