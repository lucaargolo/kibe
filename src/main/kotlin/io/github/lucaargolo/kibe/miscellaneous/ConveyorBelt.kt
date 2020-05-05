package io.github.lucaargolo.kibe.miscellaneous

import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.EntityContext
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld


class ConveyorBelt(val speed: Float): Block(FabricBlockSettings.of(Material.METAL).build()) {

    init {
        defaultState = stateManager.defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.HORIZONTAL_FACING)
        stateManager.add(Properties.NORTH)
        stateManager.add(Properties.EAST)
        stateManager.add(Properties.WEST)
        stateManager.add(Properties.SOUTH)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState
            .with(HorizontalConnectedBlock.NORTH, ctx.world.getBlockState(ctx.blockPos.south()).block is ConveyorBelt)
            .with(HorizontalConnectedBlock.SOUTH, ctx.world.getBlockState(ctx.blockPos.north()).block is ConveyorBelt)
            .with(HorizontalConnectedBlock.EAST, ctx.world.getBlockState(ctx.blockPos.west()).block is ConveyorBelt)
            .with(HorizontalConnectedBlock.WEST, ctx.world.getBlockState(ctx.blockPos.east()).block is ConveyorBelt)
            .with(Properties.HORIZONTAL_FACING, ctx.playerFacing)
    }

    override fun getStateForNeighborUpdate(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState {
        return if (facing.axis.type == Direction.Type.HORIZONTAL)
            state.with(HorizontalConnectedBlock.NORTH, world.getBlockState(pos.south()).block is ConveyorBelt)
                 .with(HorizontalConnectedBlock.SOUTH, world.getBlockState(pos.north()).block is ConveyorBelt)
                 .with(HorizontalConnectedBlock.EAST, world.getBlockState(pos.west()).block is ConveyorBelt)
                 .with(HorizontalConnectedBlock.WEST, world.getBlockState(pos.east()).block is ConveyorBelt)
        else super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun hasSidedTransparency(state: BlockState?): Boolean {
        return true
    }

    private val shape: VoxelShape = VoxelShapes.union(
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 2.0),
        createCuboidShape(0.0, 0.0, 2.0, 16.0, 3.0, 14.0),
        createCuboidShape(0.0, 0.0, 14.0, 16.0, 3.0, 16.0)
    )

    override fun getOutlineShape(state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: EntityContext?): VoxelShape {
        return shape
    }

    override fun getCollisionShape(state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: EntityContext?): VoxelShape {
        return shape
    }

}