package io.github.lucaargolo.kibe.fluids.miscellaneous

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.SimpleFluidKey
import io.github.lucaargolo.kibe.fluids.getFluidBlock
import io.github.lucaargolo.kibe.fluids.getFluidBucket
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.Item
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

abstract class ModdedFluid(formatting: Formatting = Formatting.WHITE): FlowableFluid() {

    val fluidBlock: Block by lazy {
        getFluidBlock(this)
    }

    val fluidBucket: Item by lazy {
        getFluidBucket(this)
    }

    val key: FluidKey by lazy {
        SimpleFluidKey(FluidKey.FluidKeyBuilder(this).setName(TranslatableText(fluidBlock.translationKey).formatted(formatting)))
    }

    override fun matchesType(fluid: Fluid): Boolean {
        return fluid === still || fluid === flowing
    }

    override fun isInfinite(): Boolean {
        return false
    }

    override fun beforeBreakingBlock(world: WorldAccess, pos: BlockPos, state: BlockState) {
        val blockEntity = if (state.block.hasBlockEntity()) world.getBlockEntity(pos) else null
        Block.dropStacks(state, world, pos, blockEntity)
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