package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class XpShower: BlockWithEntity(FabricBlockSettings.of(Material.STONE, MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) {

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return XpShowerBlockEntity(this, blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World, blockState: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return if(!world.isClient) checkType(blockEntityType, getEntityType(this)) { wrld, pos, state, blockEntity -> XpShowerBlockEntity.tick(wrld, pos, state, blockEntity as XpShowerBlockEntity) } else null
    }

    init {
        defaultState = stateManager.defaultState.with(Properties.FACING, Direction.SOUTH).with(Properties.ENABLED, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.FACING)
        builder.add(Properties.ENABLED)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(DispenserBlock.FACING, rotation.rotate(state[DispenserBlock.FACING]))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.rotate(mirror.getRotation(state[DispenserBlock.FACING]))
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        var side = ctx.side
        if(side == Direction.DOWN) side = Direction.UP
        return defaultState.with(Properties.FACING, side).with(Properties.ENABLED, ctx.world.isReceivingRedstonePower(ctx.blockPos))
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos?, block: Block?, fromPos: BlockPos?, notify: Boolean) {
        if (!world.isClient) {
            val isEnabled = state[Properties.ENABLED]
            if (isEnabled != world.isReceivingRedstonePower(pos)) {
                if (isEnabled) world.createAndScheduleBlockTick(pos, this, 4)
                else world.setBlockState(pos, state.cycle(Properties.ENABLED), 2)
            }
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos?, random: Random?) {
        if (state[Properties.ENABLED] && !world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.cycle(Properties.ENABLED), 2)
        }
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getOutlineShape(state: BlockState, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?) = getShape(state[Properties.FACING])

    override fun getCollisionShape(state: BlockState, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?) = getShape(state[Properties.FACING])

    companion object {
        private val EMPTY = createCuboidShape(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        private val SHAPES = mutableMapOf<Direction, VoxelShape>()

        init {
            Direction.values().forEach {
                val hrz = it != Direction.UP
                val head = VoxelShapes.union(
                    createCuboidShape(3.5, 9.0, 3.5, 12.5, 10.0, 12.5),
                    createCuboidShape(6.5, 10.0, 6.5, 9.5, 11.0, 9.5)
                )
                val duct = VoxelShapes.union(
                    createCuboidShape(7.5, 11.0, 7.5, 8.5, if (hrz) 15.0 else 16.0, 8.5),
                    when (it) {
                        Direction.NORTH -> createCuboidShape(7.5, 14.0, 8.5, 8.5, 15.0, 16.0)
                        Direction.SOUTH -> createCuboidShape(7.5, 14.0, 0.0, 8.5, 15.0, 7.5)
                        Direction.WEST -> createCuboidShape(8.5, 14.0, 7.5, 16.0, 15.0, 8.5)
                        Direction.EAST -> createCuboidShape(0.0, 14.0, 7.5, 7.5, 15.0, 8.5)
                        else -> VoxelShapes.empty()
                    }
                )
                SHAPES[it] = VoxelShapes.union(head, duct)
            }
        }

        private fun getShape(facing: Direction) = SHAPES[facing] ?: EMPTY
    }

}