package io.github.lucaargolo.kibe.fluids.miscellaneous

import io.github.lucaargolo.kibe.fluids.getFluidBlock
import io.github.lucaargolo.kibe.fluids.getFluidBucket
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

abstract class ModdedFluid: FlowableFluid() {

    val fluidBlock: Block by lazy {
        getFluidBlock(this)
    }

    val fluidBucket: Item by lazy {
        getFluidBucket(this)
    }

    override fun matchesType(fluid: Fluid): Boolean {
        return fluid === still || fluid === flowing
    }

    override fun isInfinite(world: World?): Boolean {
        return false
    }

    override fun beforeBreakingBlock(world: WorldAccess, pos: BlockPos, state: BlockState) {
        Block.dropStacks(state, world, pos, world.getBlockEntity(pos))
    }

    override fun canBeReplacedWith(fluidState: FluidState, blockView: BlockView, blockPos: BlockPos, fluid: Fluid, direction: Direction): Boolean {
        return false
    }

    override fun getFlowSpeed(world: WorldView?): Int {
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

    override fun getBucketItem(): Item {
        return fluidBucket
    }

}