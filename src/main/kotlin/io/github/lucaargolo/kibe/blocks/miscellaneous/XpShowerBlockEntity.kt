package io.github.lucaargolo.kibe.blocks.miscellaneous

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.state.property.Properties
import net.minecraft.util.Tickable
import net.minecraft.util.math.Direction

class XpShowerBlockEntity(xpShower: XpShower): BlockEntity(getEntityType(xpShower)), Tickable {

    var tickDelay = 0

    override fun tick() {
        val world = world ?: return
        if(tickDelay++ < 5) return else tickDelay = 0
        var dir = cachedState[Properties.FACING]
        if(dir != Direction.UP) dir = dir.opposite

        val extractable = FluidAttributes.EXTRACTABLE.get(world, pos.add(dir.vector))

        var i = 3 + world.random.nextInt(5) + world.random.nextInt(5)

        while (i > 0) {
            val j = ExperienceOrbEntity.roundToOrbSize(i)
            i -= j
            val toExtractAmount = FluidAmount.of(j*10L, 1000L)
            val extractedVolume = extractable.attemptExtraction({ fluidKey: FluidKey -> fluidKey == LIQUID_XP.key }, toExtractAmount, Simulation.ACTION)
            val extractedAmount = extractedVolume.amount().asInt(1000)/10
            if(extractedAmount > 0) this.world!!.spawnEntity(ExperienceOrbEntity(world, pos.x+.5, pos.y+.5, pos.z+.5, extractedAmount))
        }

    }

}