package io.github.lucaargolo.kibe.block

import com.mojang.serialization.MapCodec
import io.github.lucaargolo.kibe.blockentity.BlockEntityCompendium
import io.github.lucaargolo.kibe.blockentity.DrawbridgeBlockEntity
import io.github.lucaargolo.kibe.menu.DrawbridgeScreenHandler
import io.github.lucaargolo.kibe.utils.menu.BlockScreenHandlerFactory
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

class Drawbridge(settings: Settings): BlockWithEntity(settings) {

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return DrawbridgeBlockEntity(blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, state: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return validateTicker(blockEntityType, BlockEntityCompendium.DRAWBRIDGE, DrawbridgeBlockEntity::tick)
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.FACING)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(DispenserBlock.FACING, rotation.rotate(state[DispenserBlock.FACING]))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.rotate(mirror.getRotation(state[DispenserBlock.FACING]))
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

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos, ::DrawbridgeScreenHandler))
        return ActionResult.SUCCESS
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getCodec(): MapCodec<Drawbridge> = CODEC

    companion object {
        private val CODEC: MapCodec<Drawbridge> = createCodec(::Drawbridge)
    }

}