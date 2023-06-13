package io.github.lucaargolo.kibe.blocks.drawbridge

import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.BlockScreenHandlerFactory
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

class Drawbridge: BlockWithEntity(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL).nonOpaque()) {

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return DrawbridgeBlockEntity(this, blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, state: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return checkType(blockEntityType, getEntityType(this)) { wrld, pos, stt, blockEntity -> DrawbridgeBlockEntity.tick(wrld, pos, stt, blockEntity as DrawbridgeBlockEntity) }
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.FACING)
    }

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        var output = 0
        (world.getBlockEntity(pos) as? DrawbridgeBlockEntity)?.let { entity ->
            var i = 0
            var f = 0.0f
            val itemStack: ItemStack = entity.getStack(0)
            if (!itemStack.isEmpty) {
                f += itemStack.count.toFloat() / entity.maxCountPerStack.coerceAtMost(itemStack.maxCount).toFloat()
                ++i
            }
            output = MathHelper.floor(f * 14.0f) + if (i > 0) 1 else 0
        }
        return output
    }


    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? Inventory)?.let {
                ItemScatterer.spawn(world, pos, it)
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, notify)
        }
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.FACING, ctx.playerLookDirection.opposite)
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand?, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos))
        return ActionResult.SUCCESS
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}