package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.MOD_ID
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.function.Supplier

class RedstoneTimer: BlockWithEntity(FabricBlockSettings.of(Material.METAL)) {

    val id: Identifier = Identifier(MOD_ID, "redstone_timer")
    val entityType = BlockEntityType.Builder.create(Supplier { this.createBlockEntity(null) }, this).build(null)

    init {
        defaultState = stateManager.defaultState.with(Properties.LEVEL_15, 0).with(Properties.ENABLED, false)
    }

    override fun createBlockEntity(view: BlockView?) = RedstoneTimerEntity(this)

    override fun emitsRedstonePower(state: BlockState) = true

    override fun getStrongRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, facing: Direction): Int {
        return if(state.get(Properties.ENABLED)) 16 else 0;
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block, BlockState>) {
        stateManager.add(Properties.LEVEL_15)
        stateManager.add(Properties.ENABLED)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val level = state.get(Properties.LEVEL_15)
        if(player.isSneaking) {
            if(level > 0) world.setBlockState(pos, state.with(Properties.LEVEL_15, level-1))
            else world.setBlockState(pos, state.with(Properties.LEVEL_15, 15))
        }else{
            if(level < 15) world.setBlockState(pos, state.with(Properties.LEVEL_15, level+1))
            else world.setBlockState(pos, state.with(Properties.LEVEL_15, 0))
        }

        return ActionResult.SUCCESS
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

}