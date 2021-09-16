package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.MOD_CONFIG
import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.MovementType
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class XpShowerBlockEntity(xpShower: XpShower, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(xpShower), pos, state) {

    var tickDelay = 0

    companion object {

        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: XpShowerBlockEntity) {
            if(state[Properties.ENABLED]) return
            if(blockEntity.tickDelay++ < 5) return else blockEntity.tickDelay = 0
            var dir = state[Properties.FACING]
            if(dir != Direction.UP) dir = dir.opposite

            val extractable = FluidStorage.SIDED.find(world, pos.add(dir.vector), dir.opposite)

            var i = 3 + world.random.nextInt(5) + world.random.nextInt(5)
            i = MathHelper.fastFloor(i* MOD_CONFIG.miscellaneousModule.xpShowerSpeedMultiplier)

            while (i > 0) {
                val j = ExperienceOrbEntity.roundToOrbSize(i)
                i -= j
                val toExtractAmount = j*810L
                var extractedVolume = -1L
                Transaction.openOuter().also {
                    extractable?.extract(FluidVariant.of(LIQUID_XP), toExtractAmount, it)?.let { extractedVolume = it }
                }.commit()
                val extractedAmount = extractedVolume/810
                if(extractedAmount > 0) {
                    val entity = ExperienceOrbEntity(world, pos.x+.5, pos.y+.2, pos.z+.5, extractedAmount.toInt())
                    entity.move(MovementType.SELF, Vec3d(0.0, 0.0, 0.0))
                    entity.setVelocity(0.0, -0.5, 0.0)
                    world.spawnEntity(entity)
                }
            }

        }

    }


}