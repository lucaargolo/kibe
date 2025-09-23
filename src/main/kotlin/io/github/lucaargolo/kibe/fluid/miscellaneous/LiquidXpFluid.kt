package io.github.lucaargolo.kibe.fluid.miscellaneous

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.fluid.FluidCompendium
import io.github.lucaargolo.kibe.item.ItemCompendium
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.Item
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.neoforged.neoforge.fluids.FluidType

abstract class LiquidXpFluid: ModdedFluid() {

    override fun getStill() = FluidCompendium.LIQUID_XP
    override fun getFlowing() = FluidCompendium.LIQUID_XP_FLOWING

    override fun toBlockState(fluidState: FluidState): BlockState? {
        return BlockCompendium.FLUID_BLOCKS[still]?.defaultState?.with(Properties.LEVEL_15, getBlockStateLevel(fluidState))
    }

    override fun getFluidType(): FluidType {
        return FluidCompendium.LIQUID_XP_TYPE
    }

    override fun getBucketItem(): Item? {
        return ItemCompendium.FLUID_BUCKETS[still]
    }

    class Flowing : LiquidXpFluid() {
        override fun appendProperties(builder: StateManager.Builder<Fluid?, FluidState?>) {
            super.appendProperties(builder)
            builder.add(LEVEL)
        }

        override fun getLevel(fluidState: FluidState): Int {
            return fluidState.get(LEVEL)
        }

        override fun isStill(fluidState: FluidState?): Boolean {
            return false
        }
    }

    class Still : LiquidXpFluid() {
        override fun getLevel(fluidState: FluidState?): Int {
            return 8
        }

        override fun isStill(fluidState: FluidState?): Boolean {
            return true
        }
    }
}