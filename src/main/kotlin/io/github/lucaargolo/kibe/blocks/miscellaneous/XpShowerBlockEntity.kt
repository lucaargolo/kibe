package io.github.lucaargolo.kibe.blocks.miscellaneous

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.MovementType
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
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

            val extractable = FluidAttributes.EXTRACTABLE.get(world, pos.add(dir.vector))

            var i = 8 + world.random.nextInt(world.getReceivedRedstonePower(pos) * 5)

            while (i > 0) {
                val j = ExperienceOrbEntity.roundToOrbSize(i)
                i -= j
                val toExtractAmount = FluidAmount.of(j*10L, 1000L)
                val extractedVolume = extractable.attemptExtraction({ fluidKey: FluidKey -> fluidKey == LIQUID_XP.key }, toExtractAmount, Simulation.ACTION)
                val extractedAmount = extractedVolume.amount().asInt(1000)/10
                if(extractedAmount > 0) {
                    val entity = ExperienceOrbEntity(world, pos.x+.5, pos.y+.2, pos.z+.5, extractedAmount)
                    entity.move(MovementType.SELF, Vec3d(0.0, 0.0, 0.0))
                    entity.setVelocity(0.0, -0.5, 0.0)
                    world.spawnEntity(entity)
                }
            }

        }

    }


}