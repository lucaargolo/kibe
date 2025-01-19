package io.github.lucaargolo.kibe.block

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.lucaargolo.kibe.blockentity.BlockEntityCompendium
import io.github.lucaargolo.kibe.blockentity.BlockGeneratorBlockEntity
import io.github.lucaargolo.kibe.menu.BlockGeneratorScreenHandler
import io.github.lucaargolo.kibe.utils.menu.BlockScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.screen.ScreenHandler
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockGenerator(settings: Settings, private var block: Block, private var rate: Float): BlockWithEntity(settings) {

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.LEVEL_8)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return BlockGeneratorBlockEntity(this, block, rate, pos, state)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, blockState: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return validateTicker(blockEntityType, BlockEntityCompendium.BLOCK_GENERATOR, BlockGeneratorBlockEntity::tick)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.LEVEL_8, 0)
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos, ::BlockGeneratorScreenHandler))
        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? Inventory)?.let {
                ItemScatterer.spawn(world, pos, it)
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, notify)
        }
    }

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos): Int {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos))
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getCodec(): MapCodec<BlockGenerator> = CODEC

    companion object {
        private val CODEC: MapCodec<BlockGenerator> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(createSettingsCodec(), Block.CODEC.fieldOf("block").forGetter(BlockGenerator::block), Codec.FLOAT.fieldOf("rate").forGetter(BlockGenerator::rate))
                .apply(instance, ::BlockGenerator)
        }
    }

}