package io.github.lucaargolo.kibe.fluids.miscellaneous

import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import io.github.lucaargolo.kibe.fluids.LIQUID_XP_FLOWING
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties

abstract class LiquidXpFluid: ModdedFluid() {

    override fun getStill() = LIQUID_XP
    override fun getFlowing() = LIQUID_XP_FLOWING

    override fun toBlockState(fluidState: FluidState): BlockState {
        return fluidBlock.defaultState.with(Properties.LEVEL_15, getBlockStateLevel(fluidState))
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