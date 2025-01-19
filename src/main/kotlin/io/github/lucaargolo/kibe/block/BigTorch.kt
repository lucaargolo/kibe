package io.github.lucaargolo.kibe.block

import com.mojang.serialization.MapCodec
import io.github.lucaargolo.kibe.blockentity.BigTorchBlockEntity
import io.github.lucaargolo.kibe.blockentity.BlockEntityCompendium
import io.github.lucaargolo.kibe.menu.BigTorchScreenHandler
import io.github.lucaargolo.kibe.utils.menu.BlockScreenHandlerFactory
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.particle.ParticleTypes
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import kotlin.math.cos
import kotlin.math.sin

class BigTorch(settings: Settings): BlockWithEntity(settings) {

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.ENABLED, Properties.LEVEL_8)
    }

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return BigTorchBlockEntity(blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, state: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return validateTicker(blockEntityType, BlockEntityCompendium.BIG_TORCH, BigTorchBlockEntity::tick)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.ENABLED, !ctx.world.isReceivingRedstonePower(ctx.blockPos)).with(Properties.LEVEL_8, 0)
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if(state[Properties.ENABLED]) {
            (0..state[Properties.LEVEL_8]).forEach { radius ->
                (1..radius * 9).forEach {
                    val x = (cos(it * 180 / (radius * 9) * Math.PI / 90))
                    val z = (sin(it * 180 / (radius * 9) * Math.PI / 90))
                    val i = (radius / 4.0)
                    world.addParticle(ParticleTypes.FLAME, pos.x + (x * i) + 0.5, pos.y.toDouble(), pos.z + (z * i) + 0.5, 0.0, 0.0, 0.0)
                }
            }
        }
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos, ::BigTorchScreenHandler))
        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? Inventory)?.let {
                ItemScatterer.spawn(world, pos, it)
                world.updateComparators(pos, this)
            }
        }else{
            (world.getBlockEntity(pos) as? BigTorchBlockEntity)?.updateValues()
        }
        super.onStateReplaced(state, world, pos, newState, notify)
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos?, block: Block?, fromPos: BlockPos?, notify: Boolean) {
        if (!world.isClient) {
            val isEnabled = state[Properties.ENABLED]
            if (isEnabled == world.isReceivingRedstonePower(pos)) {
                if (isEnabled) world.scheduleBlockTick(pos, this, 4)
                else world.setBlockState(pos, state.cycle(Properties.ENABLED), 2)
            }
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos?, random: Random?) {
        if (state[Properties.ENABLED] && world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.cycle(Properties.ENABLED), 2)
        }
    }

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos): Int {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos))
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getCollisionShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape = SHAPE

    override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape = SHAPE

    override fun getCodec(): MapCodec<BigTorch> = CODEC

    companion object {
        private val CODEC: MapCodec<BigTorch> = createCodec(::BigTorch)
        private val SHAPE = createCuboidShape(6.0, 0.0, 6.0, 10.0, 14.0, 10.0)
    }

}