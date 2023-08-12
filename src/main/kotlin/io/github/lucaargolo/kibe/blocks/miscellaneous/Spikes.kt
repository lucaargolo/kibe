package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.utils.FakePlayerEntity
import io.github.lucaargolo.kibe.utils.SpikeHelper
import net.minecraft.block.*
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class Spikes(private val type: Type, settings: Settings): Block(settings) {

    enum class Type(val damage: Float) {
        STONE(4F),
        IRON(6F),
        GOLD(6F),
        DIAMOND(8F),
        NETHERITE(12F)
    }

    init {
        defaultState = defaultState.with(Properties.FACING, Direction.UP)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return defaultState.with(Properties.FACING, ctx.side)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(DispenserBlock.FACING, rotation.rotate(state[DispenserBlock.FACING]))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.rotate(mirror.getRotation(state[DispenserBlock.FACING]))
    }

    override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
        if(!world.isClient && entity is LivingEntity) {
            SpikeHelper.setSpike(entity, type)
            when(type) {
                Type.IRON, Type.STONE -> entity.damage(DamageSource.GENERIC, type.damage)
                Type.GOLD, Type.DIAMOND, Type.NETHERITE -> entity.damage(DamageSource.player(FakePlayerEntity(world)), type.damage)
            }
            SpikeHelper.setSpike(entity, null)
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