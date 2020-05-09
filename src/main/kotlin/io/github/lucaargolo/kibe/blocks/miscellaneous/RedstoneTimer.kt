package io.github.lucaargolo.kibe.blocks.miscellaneous

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class RedstoneTimer: BlockWithEntity(FabricBlockSettings.of(Material.METAL)) {

    override fun createBlockEntity(view: BlockView?) = RedstoneTimerEntity(this)

    override fun emitsRedstonePower(state: BlockState) = true

    init {
        defaultState = stateManager.defaultState.with(Properties.ENABLED, false)
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.ENABLED)
    }

    override fun getWeakRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, facing: Direction): Int {
        return if(state[Properties.ENABLED]) 15 else 0;
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
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
        return ActionResult.SUCCESS
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.ENTITYBLOCK_ANIMATED
    }

}