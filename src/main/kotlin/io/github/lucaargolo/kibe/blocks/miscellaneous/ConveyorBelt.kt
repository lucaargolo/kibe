package io.github.lucaargolo.kibe.blocks.miscellaneous

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import kotlin.math.abs

class ConveyorBelt(private val speed: Float): Block(FabricBlockSettings.of(Material.METAL)) {

    init {
        defaultState = stateManager.defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
    }

    override fun getOpacity(state: BlockState, view: BlockView, pos: BlockPos) = 0

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.HORIZONTAL_FACING)
        stateManager.add(Properties.NORTH)
        stateManager.add(Properties.EAST)
        stateManager.add(Properties.WEST)
        stateManager.add(Properties.SOUTH)
    }

    override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
        val direction = state.get(Properties.HORIZONTAL_FACING)
        val desiredPos = Vec3d(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
        val adjustmentFactor = 0.1
        val adjustmentSpeed = speed*(desiredPos.distanceTo(entity.pos))
        val adjustmentVec3d = Vec3d(if (abs(entity.pos.x-desiredPos.x) > adjustmentFactor) MathHelper.sign((entity.pos.x-desiredPos.x)*-1).toDouble() else 0.0, 0.0, if (abs(entity.pos.z-desiredPos.z) > adjustmentFactor) MathHelper.sign((entity.pos.z-desiredPos.z)*-1).toDouble() else 0.0)
        if (entity.y - pos.y > 0.3 || (entity is PlayerEntity && entity.isSneaking)) return
        entity.velocity = Vec3d(if(direction.offsetX == 0) adjustmentVec3d.x*adjustmentSpeed else direction.offsetX.toDouble()*speed, 0.0, if(direction.offsetZ == 0) adjustmentVec3d.z*adjustmentSpeed else direction.offsetZ.toDouble()*speed)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState
            .with(HorizontalConnectingBlock.NORTH, ctx.world.getBlockState(ctx.blockPos.south()).block is ConveyorBelt)
            .with(HorizontalConnectingBlock.SOUTH, ctx.world.getBlockState(ctx.blockPos.north()).block is ConveyorBelt)
            .with(HorizontalConnectingBlock.EAST, ctx.world.getBlockState(ctx.blockPos.west()).block is ConveyorBelt)
            .with(HorizontalConnectingBlock.WEST, ctx.world.getBlockState(ctx.blockPos.east()).block is ConveyorBelt)
            .with(Properties.HORIZONTAL_FACING, ctx.playerFacing)
    }

    @Suppress("DEPRECATION")
    override fun getStateForNeighborUpdate(state: BlockState, facing: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
        return if (facing.axis.type == Direction.Type.HORIZONTAL)
            state.with(HorizontalConnectingBlock.NORTH, world.getBlockState(pos.south()).block is ConveyorBelt)
                 .with(HorizontalConnectingBlock.SOUTH, world.getBlockState(pos.north()).block is ConveyorBelt)
                 .with(HorizontalConnectingBlock.EAST, world.getBlockState(pos.west()).block is ConveyorBelt)
                 .with(HorizontalConnectingBlock.WEST, world.getBlockState(pos.east()).block is ConveyorBelt)
        else super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun hasSidedTransparency(state: BlockState?): Boolean {
        return true
    }


    override fun getOutlineShape(state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?): VoxelShape {
        return createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0)
    }

    override fun getCollisionShape(state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?): VoxelShape {
        return createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0)
    }

    override fun getCullingShape(state: BlockState?, view: BlockView?, pos: BlockPos?): VoxelShape {
        return createCuboidShape(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    }

}