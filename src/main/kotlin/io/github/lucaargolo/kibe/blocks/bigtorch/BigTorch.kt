package io.github.lucaargolo.kibe.blocks.bigtorch

import io.github.lucaargolo.kibe.utils.ModHandlerFactory
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class BigTorch: BlockWithEntity(Settings.of(Material.SUPPORTED).noCollision().strength(0.5f).lightLevel{15}.sounds(BlockSoundGroup.WOOD)) {

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.LEVEL_8)
    }

    override fun createBlockEntity(world: BlockView?): BlockEntity? {
        return BigTorchBlockEntity(this)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.LEVEL_8, 0)
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        (0..state[Properties.LEVEL_8]).forEach { radius ->
            if(radius == 0) return@forEach
            (1..radius*9).forEach {
                val x = (cos(it * 180/(radius*9) * Math.PI / 90))
                val z = (sin(it * 180/(radius*9) * Math.PI / 90))
                val i = (radius/4.0)
                world.addParticle(ParticleTypes.FLAME, pos.x+(x*i)+0.5, pos.y.toDouble(), pos.z+(z*i)+0.5, 0.0, 0.0, 0.0)
            }
        }
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand?, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(ModHandlerFactory(this, pos))
        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? Inventory)?.let {
                ItemScatterer.spawn(world, pos, it)
            }
            super.onStateReplaced(state, world, pos, newState, notify)
        }
    }

    override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape {
        return createCuboidShape(7.0, 0.0, 7.0, 9.0, 10.0, 9.0)
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}