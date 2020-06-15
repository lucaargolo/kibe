package io.github.lucaargolo.kibe.fluids.miscellaneous

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.fluid.BaseFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import net.minecraft.world.WorldView

abstract class ModdedFluid: BaseFluid() {

    override fun matchesType(fluid: Fluid): Boolean {
        return fluid === still || fluid === flowing
    }

    override fun isInfinite(): Boolean {
        return false
    }

    override fun beforeBreakingBlock(world: IWorld, pos: BlockPos, state: BlockState) {
        val blockEntity = if (state.block.hasBlockEntity()) world.getBlockEntity(pos) else null
        Block.dropStacks(state, world.world, pos, blockEntity)
    }

    override fun canBeReplacedWith(fluidState: FluidState, blockView: BlockView, blockPos: BlockPos, fluid: Fluid, direction: Direction): Boolean {
        return false
    }

    override fun method_15733(world: WorldView?): Int {
        return 2
    }

    override fun getLevelDecreasePerBlock(worldView: WorldView?): Int {
        return 2
    }

    override fun getTickRate(worldView: WorldView?): Int {
        return 5
    }

    override fun getBlastResistance(): Float {
        return 100.0f
    }

}