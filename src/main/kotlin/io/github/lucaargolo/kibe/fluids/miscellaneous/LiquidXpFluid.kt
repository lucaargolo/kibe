package io.github.lucaargolo.kibe.fluids.miscellaneous

import io.github.lucaargolo.kibe.fluids.*
import io.github.lucaargolo.kibe.items.CURSED_SEEDS
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.Item
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties


abstract class LiquidXpFluid: ModdedFluid() {

    override fun getStill(): Fluid = getFluidStill(LIQUID_XP)!!
    override fun getFlowing(): Fluid = getFluidFlowing(LIQUID_XP)!!
    override fun getBucketItem(): Item = getFluidBucket(LIQUID_XP)!!

    override fun toBlockState(fluidState: FluidState): BlockState {
        // method_15741 converts the LEVEL_1_8 of the fluid state to the LEVEL_15 the fluid block uses
        return getFluidBlock(LIQUID_XP)!!.defaultState.with(Properties.LEVEL_15, method_15741(fluidState));
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