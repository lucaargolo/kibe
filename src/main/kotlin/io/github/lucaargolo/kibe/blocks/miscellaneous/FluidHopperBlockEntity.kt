package io.github.lucaargolo.kibe.blocks.miscellaneous

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.minus
import io.github.lucaargolo.kibe.utils.plus
import net.minecraft.block.BlockState
import net.minecraft.block.HopperBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable

class FluidHopperBlockEntity(block: FluidHopper): BlockEntity(getEntityType(block)), Tickable {

    val fluidInv = SimpleFixedFluidInv(1, FluidAmount.ofWhole(1))
    private var volume
        get() = fluidInv.getInvFluid(0)
        set(value) {
            fluidInv.setInvFluid(0, value, Simulation.ACTION)
        }

    override fun tick() {
        val world = world ?: return
        if(!cachedState[HopperBlock.ENABLED]) return
        val direction = cachedState[HopperBlock.FACING]

        val extractable = FluidAttributes.EXTRACTABLE.get(world, pos.up())
        val insertable = FluidAttributes.INSERTABLE.get(world, pos.add(direction.vector))

        if(volume.isEmpty) {
            volume = extractable.attemptAnyExtraction(FluidAmount.of(50, 1000), Simulation.ACTION)
        }else {
            if(volume.amount() <= FluidAmount.of(950, 1000)) {
                val extracted = extractable.attemptExtraction({it == volume.fluidKey}, FluidAmount.of(50, 1000), Simulation.ACTION)
                volume = volume.withAmount(volume.amount()+extracted.amount())
            }
        }

        if(!volume.isEmpty && volume.amount() >= FluidAmount.of(50, 1000)) {
            val notInserted = insertable.attemptInsertion(volume.fluidKey.withAmount(FluidAmount.of(50, 1000)), Simulation.ACTION)
            val insertedAmount = FluidAmount.of(50, 1000) - notInserted.amount()
            volume = volume.withAmount(volume.amount()-insertedAmount)
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        fluidInv.toTag(tag)
        return tag
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        fluidInv.fromTag(tag)
    }
}