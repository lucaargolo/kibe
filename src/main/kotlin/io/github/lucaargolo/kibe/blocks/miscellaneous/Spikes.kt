package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.utils.FakePlayerEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class Spikes(private val damage: Float, private val isPlayer: Boolean, settings: Settings): Block(settings) {

    override fun onSteppedOn(world: World, pos: BlockPos, entity: Entity) {
        if(world is ServerWorld) {
            if(entity is LivingEntity) {
                if(isPlayer) entity.damage(DamageSource.player(FakePlayerEntity(world)), damage)
                else entity.damage(DamageSource.GENERIC, damage)
            }
        }
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }


    override fun hasSidedTransparency(state: BlockState?): Boolean {
        return true
    }


    override fun getOutlineShape(state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: ShapeContext): VoxelShape {
        return createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
    }

    override fun getCollisionShape(state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: ShapeContext): VoxelShape {
        return createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
    }

}