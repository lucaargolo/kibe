package io.github.lucaargolo.kibe.fluid.miscellaneous

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.FluidBlock
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.BucketItem
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

abstract class ModdedFluid: FlowableFluid() {

    var fluidBlock: FluidBlock? = null
    var fluidBucket: BucketItem? = null

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

    override fun getBucketItem(): Item? {
        return fluidBucket
    }

}