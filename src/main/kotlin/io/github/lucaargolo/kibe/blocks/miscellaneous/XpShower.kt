package io.github.lucaargolo.kibe.blocks.miscellaneous

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

class XpShower: BlockWithEntity(FabricBlockSettings.of(Material.STONE, MaterialColor.STONE).requiresTool().strength(1.5F, 6.0F)) {

    override fun createBlockEntity(world: BlockView?) = XpShowerBlockEntity(this)

    init {
        defaultState = stateManager.defaultState.with(Properties.FACING, Direction.SOUTH)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        var side = ctx.side
        if(side == Direction.DOWN)
            side = Direction.UP
        return defaultState.with(Properties.FACING, side)
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getOutlineShape(state: BlockState, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?) = getShape(state)

    override fun getCollisionShape(state: BlockState, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?) = getShape(state)

    private fun getShape(state: BlockState): VoxelShape {
        val dir = state[Properties.FACING]
        val hrz = dir != Direction.UP
        val head = VoxelShapes.union(
            createCuboidShape(3.5, 9.0, 3.5, 12.5, 10.0, 12.5),
            createCuboidShape(6.5, 10.0, 6.5, 9.5, 11.0, 9.5)
        )
        val duct = VoxelShapes.union(
            createCuboidShape(7.5, 11.0, 7.5, 8.5, if(hrz) 15.0 else 16.0, 8.5),
            when(dir) {
                Direction.NORTH -> createCuboidShape(7.5, 14.0, 8.5, 8.5, 15.0, 16.0)
                Direction.SOUTH -> createCuboidShape(7.5, 14.0, 0.0, 8.5, 15.0, 7.5)
                Direction.WEST -> createCuboidShape(8.5, 14.0, 7.5, 16.0, 15.0, 8.5)
                Direction.EAST -> createCuboidShape(0.0, 14.0, 7.5, 7.5, 15.0, 8.5)
                else -> VoxelShapes.empty()
            }
        )
        return VoxelShapes.union(head, duct)
    }

}