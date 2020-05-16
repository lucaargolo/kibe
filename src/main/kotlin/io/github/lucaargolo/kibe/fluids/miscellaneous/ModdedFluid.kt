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

    /**
     * @return is the given fluid an instance of this fluid?
     */
    override fun matchesType(fluid: Fluid): Boolean {
        return fluid === still || fluid === flowing
    }

    /**
     * @return is the fluid infinite like water?
     */
    override fun isInfinite(): Boolean {
        return false
    }

    /**
     * Perform actions when fluid flows into a replaceable block. Water drops
     * the block's loot table. Lava plays the "block.lava.extinguish" sound.
     */
    override fun beforeBreakingBlock(world: IWorld, pos: BlockPos?, state: BlockState) {
        val blockEntity = if (state.block.hasBlockEntity()) world.getBlockEntity(pos) else null
        Block.dropStacks(state, world.world, pos, blockEntity)
    }

    /**
     * Lava returns true if its FluidState is above a certain height and the
     * Fluid is Water.
     *
     * @return if the given Fluid can flow into this FluidState?
     */
    override fun canBeReplacedWith(fluidState: FluidState, blockView: BlockView, blockPos: BlockPos, fluid: Fluid, direction: Direction): Boolean {
        return false
    }

    /**
     * Possibly related to the distance checks for flowing into nearby holes?
     * Water returns 4. Lava returns 2 in the Overworld and 4 in the Nether.
     */
    override fun method_15733(worldView: WorldView?): Int {
        return 2
    }

    /**
     * Water returns 1. Lava returns 2 in the Overworld and 1 in the Nether.
     */
    override fun getLevelDecreasePerBlock(worldView: WorldView?): Int {
        return 2
    }

    /**
     * Water returns 5. Lava returns 30 in the Overworld and 10 in the Nether.
     */
    override fun getTickRate(worldView: WorldView?): Int {
        return 5
    }

    /**
     * Water and Lava both return 100.0F.
     */
    override fun getBlastResistance(): Float {
        return 100.0f
    }
}