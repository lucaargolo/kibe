package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.utils.FakePlayerEntity
import net.minecraft.block.*
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class Spikes(private val damage: Float, private val isPlayer: Boolean, settings: Settings): Block(settings) {


    init {
        defaultState = defaultState.with(Properties.FACING, Direction.UP)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return defaultState.with(Properties.FACING, ctx.side)
    }

    override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
        if(!world.isClient && entity is LivingEntity) {
            if(isPlayer)
                entity.damage(DamageSource.player(FakePlayerEntity(world)), damage)
            else
                entity.damage(DamageSource.GENERIC, damage)
        }
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun hasSidedTransparency(state: BlockState?): Boolean {
        return true
    }

    override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, ePos: ShapeContext) = getShape(state[Properties.FACING])

    override fun getCollisionShape(state: BlockState, view: BlockView, pos: BlockPos, ePos: ShapeContext) = getShape(state[Properties.FACING])

    override fun getCullingShape(state: BlockState?, world: BlockView?, pos: BlockPos?): VoxelShape = EMPTY

    companion object {
        private val EMPTY = createCuboidShape(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        private val SHAPES = mutableMapOf<Direction, VoxelShape>()

        init {
            Direction.values().forEach {
                SHAPES[it] = when(it) {
                    Direction.UP -> createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
                    Direction.DOWN -> createCuboidShape(0.0, 8.0, 0.0, 16.0, 16.0, 16.0)
                    Direction.EAST -> createCuboidShape(0.0, 0.0, 0.0, 8.0, 16.0, 16.0)
                    Direction.WEST -> createCuboidShape(8.0, 0.0, 0.0, 16.0, 16.0, 16.0)
                    Direction.SOUTH -> createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 8.0)
                    Direction.NORTH -> createCuboidShape(0.0, 0.0, 8.0, 16.0, 16.0, 16.0)
                }
            }
        }

        private fun getShape(facing: Direction) = SHAPES[facing] ?: EMPTY
    }

}