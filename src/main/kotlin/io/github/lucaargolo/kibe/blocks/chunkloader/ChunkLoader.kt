package io.github.lucaargolo.kibe.blocks.chunkloader

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ChunkLoader: BlockWithEntity(FabricBlockSettings.of(Material.STONE).requiresTool().strength(22.0F, 600.0F)) {

    init {
        defaultState = stateManager.defaultState.with(Properties.ENABLED, false)
    }

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return ChunkLoaderBlockEntity(this, blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, blockState: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return BlockEntityTicker { wrld, pos, state, blockEntity -> ChunkLoaderBlockEntity.tick(wrld, pos, state, blockEntity as ChunkLoaderBlockEntity) }
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.ENABLED)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return defaultState.with(Properties.ENABLED, true)
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, notify: Boolean) {
        if ((!state.isOf(newState.block) || !newState[Properties.ENABLED]) && !world.isClient) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is ChunkLoaderBlockEntity) {
                val chunkLoaderState = (world as ServerWorld).server.overworld.persistentStateManager.getOrCreate({ ChunkLoaderState.createFromTag(it, world.server) }, {ChunkLoaderState(world.server)}, "kibe_chunk_loaders")
                chunkLoaderState.removePos(pos, world)
            }
        }
        super.onStateReplaced(state, world, pos, newState, notify)
    }

    override fun getRenderType(state: BlockState?) =  BlockRenderType.MODEL

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        (world.getBlockEntity(pos) as? ChunkLoaderBlockEntity)?.let { be ->
            placer?.let { be.ownerUUID = it.uuidAsString }
        }
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if(world.isClient) {
            val be = world.getBlockEntity(pos) as? ChunkLoaderBlockEntity
            be?.let {
                if (state[Properties.ENABLED]) {
                    MinecraftClient.getInstance().openScreen(ChunkLoaderScreen(be))
                } else {
                    player.sendMessage(TranslatableText("chat.kibe.chunk_loader.${be.disabledReason.name.lowercase()}"), false)
                }
            }
        }
        return ActionResult.SUCCESS
    }

    override fun getCollisionShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape = SHAPE

    override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape = SHAPE

    companion object {
        private val SHAPE = VoxelShapes.union(
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
            createCuboidShape(3.0, 12.0, 3.0, 13.0, 13.0, 13.0)
        )
    }
    
}