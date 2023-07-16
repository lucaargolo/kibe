package io.github.lucaargolo.kibe.blocks.cooler

import io.github.lucaargolo.kibe.utils.BlockScreenHandlerFactory
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.screen.ScreenHandler
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class Cooler: BlockWithEntity(FabricBlockSettings.of(Material.METAL, MapColor.PALE_PURPLE).strength(0.2F).sounds(BlockSoundGroup.SNOW)) {

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return CoolerBlockEntity(this, blockPos, blockState)
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing.opposite)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState? {
        return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state[Properties.HORIZONTAL_FACING]))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState? {
        return state.rotate(mirror.getRotation(state[Properties.HORIZONTAL_FACING]))
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand?, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos))
        return ActionResult.SUCCESS
    }

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos): Int {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos))
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? CoolerBlockEntity)?.let {
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getCollisionShape(state: BlockState, view: BlockView, pos: BlockPos, ePos: ShapeContext) = getShape(state[Properties.HORIZONTAL_FACING])

    override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, ePos: ShapeContext) = getShape(state[Properties.HORIZONTAL_FACING])

    companion object {
        private val EMPTY = createCuboidShape(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        private val SHAPES = mutableMapOf<Direction, VoxelShape>()

        init {
            Direction.values().forEach {
                SHAPES[it] = when(it) {
                    Direction.EAST, Direction.WEST -> createCuboidShape(5.0, 0.0, 1.0, 11.0, 12.0, 15.0)
                    else -> createCuboidShape(1.0, 0.0, 5.0, 15.0, 12.0, 11.0)
                }
            }
        }

        fun getShape(direction: Direction): VoxelShape = SHAPES[direction] ?: EMPTY
    }

}