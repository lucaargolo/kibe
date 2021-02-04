package io.github.lucaargolo.kibe.blocks.bigtorch

import io.github.lucaargolo.kibe.utils.BlockScreenHandlerFactory
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
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
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class BigTorch: BlockWithEntity(Settings.of(Material.DECORATION).strength(0.5f).luminance{15}.sounds(BlockSoundGroup.WOOD)) {

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.LEVEL_8)
    }

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return BigTorchBlockEntity(this, blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, state: BlockState?, type: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return BlockEntityTicker { wrld, pos, stt, blockEntity -> BigTorchBlockEntity.tick(wrld, pos, stt, blockEntity as BigTorchBlockEntity) }
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.LEVEL_8, 0)
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        (0..state[Properties.LEVEL_8]).forEach { radius ->
            (1..radius*9).forEach {
                val x = (cos(it * 180/(radius*9) * Math.PI / 90))
                val z = (sin(it * 180/(radius*9) * Math.PI / 90))
                val i = (radius/4.0)
                world.addParticle(ParticleTypes.FLAME, pos.x+(x*i)+0.5, pos.y.toDouble(), pos.z+(z*i)+0.5, 0.0, 0.0, 0.0)
            }
        }
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand?, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos))
        return ActionResult.SUCCESS
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? Inventory)?.let {
                ItemScatterer.spawn(world, pos, it)
            }
            super.onStateReplaced(state, world, pos, newState, notify)
        }
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getCollisionShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape = SHAPE

    override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape = SHAPE

    companion object {
        private val SHAPE = createCuboidShape(6.0, 0.0, 6.0, 10.0, 14.0, 10.0)
    }

}