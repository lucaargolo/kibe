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
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FluidHopperBlockEntity(block: FluidHopper, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(block), pos, state) {

    val fluidInv = SimpleFixedFluidInv(1, FluidAmount(1))
    private var volume
        get() = fluidInv.getInvFluid(0)
        set(value) {
            fluidInv.setInvFluid(0, value, Simulation.ACTION)
        }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        fluidInv.toTag(tag)
        return tag
    }

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        fluidInv.fromTag(tag)
    }

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: FluidHopperBlockEntity) {
            if(!state[HopperBlock.ENABLED]) return
            val direction = state[HopperBlock.FACING]

            val extractable = FluidAttributes.EXTRACTABLE.get(world, pos.up())
            val insertable = FluidAttributes.INSERTABLE.get(world, pos.add(direction.vector))

            if(entity.volume.isEmpty) {
                entity.volume = extractable.attemptAnyExtraction(FluidAmount.of(50, 1000), Simulation.ACTION)
            }else {
                if(entity.volume.amount() <= FluidAmount.of(950, 1000)) {
                    val extracted = extractable.attemptExtraction({it == entity.volume.fluidKey}, FluidAmount.of(50, 1000), Simulation.ACTION)
                    entity.volume = entity.volume.withAmount(entity.volume.amount()+extracted.amount())
                }
            }

            if(!entity.volume.isEmpty && entity.volume.amount() >= FluidAmount.of(50, 1000)) {
                val notInserted = insertable.attemptInsertion(entity.volume.fluidKey.withAmount(FluidAmount.of(50, 1000)), Simulation.ACTION)
                val insertedAmount = FluidAmount.of(50, 1000) - notInserted.amount()
                entity.volume = entity.volume.withAmount(entity.volume.amount()-insertedAmount)
            }
        }
    }
}