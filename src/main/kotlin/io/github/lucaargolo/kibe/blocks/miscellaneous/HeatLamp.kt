package io.github.lucaargolo.kibe.blocks.miscellaneous

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.item.ItemPlacementContext
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*

class HeatLamp: BlockWithEntity(FabricBlockSettings.copyOf(Blocks.GLOWSTONE).luminance { if(it[Properties.ENABLED]) 15 else 0 }) {

    override fun createBlockEntity(world: BlockView?) = HeatLampBlockEntity(this)

    init {
        defaultState = stateManager.defaultState.with(Properties.ENABLED, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.ENABLED)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.ENABLED, ctx.world.isReceivingRedstonePower(ctx.blockPos))
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos?, block: Block?, fromPos: BlockPos?, notify: Boolean) {
        if (!world.isClient) {
            val isEnabled = state[Properties.ENABLED]
            if (isEnabled != world.isReceivingRedstonePower(pos)) {
                if (isEnabled) world.blockTickScheduler.schedule(pos, this, 4)
                else world.setBlockState(pos, state.cycle(Properties.ENABLED), 2)
            }
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos?, random: Random?) {
        if (state[Properties.ENABLED] && !world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.cycle(Properties.ENABLED), 2)
        }
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if(state[Properties.ENABLED]) {
            repeat(4) {
                val x = pos.x.toDouble() + 0.5
                val y = pos.y.toDouble() + 0.5
                val z = pos.z.toDouble() + 0.5

                val velocityX = (random.nextDouble() - 0.5) * 0.25
                val velocityY = (random.nextDouble() - 0.5) * 0.25
                val velocityZ = (random.nextDouble() - 0.5) * 0.25

                world.addParticle(ParticleTypes.FLAME, x, y, z, -velocityX, -velocityY, -velocityZ)
            }
        }
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}