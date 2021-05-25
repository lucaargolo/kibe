package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.WATER_DROPS
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.item.ItemPlacementContext
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*

class Dehumidifier: BlockWithEntity(FabricBlockSettings.copyOf(Blocks.COBBLESTONE)) {

    override fun createBlockEntity(world: BlockView?) = DehumidifierBlockEntity(this)

    init {
        defaultState = stateManager.defaultState.with(Properties.ENABLED, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.ENABLED)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.ENABLED, ctx.world.isReceivingRedstonePower(ctx.blockPos))
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if(newState.isOf(this)) {
            (world.getBlockEntity(pos) as? DehumidifierBlockEntity)?.markDirty()
        }
        super.onStateReplaced(state, world, pos, newState, moved)
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
        val vecPos = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        if(state[Properties.ENABLED]) {
            repeat(4) {
                val x = vecPos.x + (random.nextDouble()*4)-2
                val y = vecPos.y + (random.nextDouble()*4)-2
                val z = vecPos.z + (random.nextDouble()*4)-2
                val vel = Vec3d(x, y, z).reverseSubtract(vecPos).normalize().multiply(0.1)
                world.addParticle(WATER_DROPS, x, y, z, vel.x, vel.y, vel.z)
            }
        }
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}