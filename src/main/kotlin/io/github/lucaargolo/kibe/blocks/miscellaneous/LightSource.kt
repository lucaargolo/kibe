package io.github.lucaargolo.kibe.blocks.miscellaneous

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.particle.ParticleTypes
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.tag.FluidTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import java.util.*

class LightSource: Block(FabricBlockSettings.of(Material.GLASS).lightLevel(15).ticksRandomly().collidable(false)), Waterloggable {

    init {
        defaultState = stateManager.defaultState.with(Properties.WATERLOGGED, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.WATERLOGGED)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val fluidState = ctx.world.getFluidState(ctx.blockPos)
        val bl = fluidState.isIn(FluidTags.WATER) && fluidState.level == 8
        return super.getPlacementState(ctx)!!.with(SeaPickleBlock.WATERLOGGED, bl)
    }

    override fun getFluidState(state: BlockState): FluidState? {
        return if (state.get(SeaPickleBlock.WATERLOGGED) as Boolean) Fluids.WATER.getStill(false)
        else super.getFluidState(state)
    }

    override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, newState: BlockState, world: WorldAccess, pos: BlockPos?, posFrom: BlockPos?): BlockState? {
        if (state.get(HorizontalConnectingBlock.WATERLOGGED) as Boolean) {
            world.fluidTickScheduler.schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
        }
        return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom)
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        (0..2).forEach {
            val vx = (random.nextDouble()-0.5)/5.0
            val vy = (random.nextDouble()-0.5)/5.0
            val vz = (random.nextDouble()-0.5)/5.0
            val vvy = (random.nextDouble()-0.5)/30.0
            world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.x+0.5+vx, pos.y+0.5+vy, pos.z+0.5+vz, 0.0, 0.0+vvy, 0.0)
        }
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.INVISIBLE
    }

    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return createCuboidShape(6.0, 6.0, 6.0, 10.0, 10.0, 10.0)
    }

}