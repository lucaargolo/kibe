package io.github.lucaargolo.kibe.blocks.drawbridge

import io.github.lucaargolo.kibe.utils.BlockScreenHandlerFactory
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class Drawbridge: BlockWithEntity(FabricBlockSettings.of(Material.METAL, MaterialColor.IRON).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL).nonOpaque()) {

    override fun createBlockEntity(world: BlockView?) = DrawbridgeBlockEntity(this)

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.FACING)
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? Inventory)?.let {
                ItemScatterer.spawn(world, pos, it)
            }
            super.onStateReplaced(state, world, pos, newState, notify)
        }
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.FACING, if(ctx.player?.isSneaking != false) ctx.side.opposite else ctx.side)
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand?, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos))
        return ActionResult.SUCCESS
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}