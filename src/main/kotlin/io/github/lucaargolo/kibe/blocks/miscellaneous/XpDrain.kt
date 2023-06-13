package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.MOD_CONFIG
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.*
import net.minecraft.entity.Entity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import kotlin.math.pow

class XpDrain: Block(FabricBlockSettings.copyOf(Blocks.STONE).requiresTool().strength(1.5F, 6.0F)) {

    override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
        if(!world.isClient && entity is PlayerEntity) {
            val insertable = FluidStorage.SIDED.find(world, pos.down(), Direction.UP)

            var i = 3 + world.random.nextInt(5) + world.random.nextInt(5)
            i = MathHelper.floor(i*MOD_CONFIG.miscellaneousModule.xpDrainSpeedMultiplier)

            while (i > 0) {
                val j = ExperienceOrbEntity.roundToOrbSize(i)
                i -= j
                if(getTotalExperience(entity) > j) {
                    val toInsertAmount = j * 810L
                    var insertedAmount = -1L;
                    Transaction.openOuter().also {
                        insertable?.insert(FluidVariant.of(LIQUID_XP), toInsertAmount, it)?.let { insertedAmount = it }
                    }.commit()
                    if(insertedAmount > 0) {
                        entity.addExperience(-insertedAmount.toInt()/810)
                    }
                }
            }
        }
    }

    private fun getTotalExperience(player: PlayerEntity): Int {
        var experience: Int
        return when (val level: Int = player.experienceLevel) {
            in 0..15 -> {
                experience = MathHelper.ceil(level.toDouble().pow(2.0) + 6 * level)
                val requiredExperience = 2 * level + 7
                val currentExp = player.experienceProgress
                experience += MathHelper.ceil(currentExp * requiredExperience)
                experience
            }
            in 16..30 -> {
                experience = MathHelper.ceil(2.5 * level.toDouble().pow(2.0) - 40.5 * level + 360)
                val requiredExperience = 5 * level - 38
                val currentExp = player.experienceProgress
                experience += MathHelper.ceil(currentExp * requiredExperience)
                experience
            }
            else -> {
                experience = MathHelper.ceil(4.5 * level.toDouble().pow(2.0) - 162.5 * level + 2220)
                val requiredExperience = 9 * level - 158
                val currentExp = player.experienceProgress
                experience += MathHelper.ceil(currentExp * requiredExperience)
                experience
            }
        }
    }

    override fun getOutlineShape(state: BlockState, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?): VoxelShape = SHAPE

    override fun getCollisionShape(state: BlockState, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?): VoxelShape = SHAPE

    companion object {
        private val SHAPE = createCuboidShape(0.5, 0.0, 0.5, 15.5, 1.0, 15.5)
    }
}