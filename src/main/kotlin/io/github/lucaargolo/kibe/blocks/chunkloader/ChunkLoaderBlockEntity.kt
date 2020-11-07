package io.github.lucaargolo.kibe.blocks.chunkloader

import io.github.lucaargolo.kibe.MOD_CONFIG
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity
import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

class ChunkLoaderBlockEntity(val block: Block, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(block), pos, state), BlockEntityClientSerializable {

    var ownerUUID = ""

    enum class DisabledReason {
        NONE,
        OWNER_OFFLINE,
        OWNER_LONG_GONE,
        TOO_MANY_LOADERS
    }

    var disabledReason = DisabledReason.NONE
    private var ownerLastSeen = System.currentTimeMillis()
    private var delay = 0

    var enabledChunks = mutableListOf(
        Pair(-1, -1), Pair(0, -1), Pair(1, -1),
        Pair(-1, 0), Pair(0, 0), Pair(1, 0),
        Pair(-1, 1), Pair(0, 1), Pair(1, 1)
    )

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putString("ownerUUID", ownerUUID)
        tag.putLong("ownerLastSeen", ownerLastSeen)
        tag.putString("disabledReason", disabledReason.name)
        val list = ListTag()
        enabledChunks.forEach {
            val innerTag = CompoundTag()
            innerTag.putInt("x", it.first)
            innerTag.putInt("z", it.second)
            list.add(innerTag)
        }
        tag.put("enabledChunks", list)
        return super.toTag(tag)
    }

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        ownerUUID = tag.getString("ownerUUID")
        ownerLastSeen = tag.getLong("ownerLastSeen")
        disabledReason = try { DisabledReason.valueOf(tag.getString("disabledReason")) } catch (ignored: Exception) { ignored.printStackTrace(); DisabledReason.NONE }
        val list = tag.get("enabledChunks") as? ListTag
        list?.let {
            enabledChunks = mutableListOf()
            list.forEach {
                val ct = it as CompoundTag
                enabledChunks.add(Pair(ct.getInt("x"), ct.getInt("z")))
            }
        }
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        return toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag) {
        fromTag(tag)
    }

    fun markDirtyAndSync() {
        markDirty()
        (world as? ServerWorld)?.let { sync() }
    }

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: ChunkLoaderBlockEntity) {
            if(blockEntity.delay >= 20) {
                val playerUUID = try { UUID.fromString(blockEntity.ownerUUID) } catch (ignored: Exception) { null }
                if (!state[Properties.ENABLED]) {
                    when (blockEntity.disabledReason) {
                        DisabledReason.NONE -> {
                            world.setBlockState(pos, state.with(Properties.ENABLED, true))
                            blockEntity.disabledReason = DisabledReason.NONE
                            blockEntity.markDirtyAndSync()
                        }
                        DisabledReason.OWNER_OFFLINE, DisabledReason.OWNER_LONG_GONE -> {
                            playerUUID?.let { validUUID ->
                                (world as? ServerWorld)?.server?.playerManager?.getPlayer(validUUID)?.let {
                                    world.setBlockState(pos, state.with(Properties.ENABLED, true))
                                    blockEntity.ownerLastSeen = System.currentTimeMillis()
                                    blockEntity.disabledReason = DisabledReason.NONE
                                    blockEntity.markDirtyAndSync()
                                }
                            }
                        }
                        DisabledReason.TOO_MANY_LOADERS -> {
                            playerUUID?.let { validUUID ->
                                (world as? ServerWorld)?.let { world ->
                                    val chunkLoaderState = world.server.overworld.persistentStateManager.getOrCreate({ ChunkLoaderState(world.server, "kibe_chunk_loaders") }, "kibe_chunk_loaders")
                                    if(chunkLoaderState.getLoaded(validUUID) < MOD_CONFIG.chunkLoaderModule.maxPerPlayer || MOD_CONFIG.chunkLoaderModule.maxPerPlayer < 0) {
                                        world.setBlockState(pos, state.with(Properties.ENABLED, true))
                                        blockEntity.disabledReason = DisabledReason.NONE
                                        blockEntity.markDirtyAndSync()
                                    }
                                }
                            }
                        }
                    }
                }
                if (state[Properties.ENABLED]) {
                    if(blockEntity.disabledReason != DisabledReason.NONE) {
                        world.setBlockState(pos, state.with(Properties.ENABLED, false))
                        return
                    }

                    (world as? ServerWorld)?.let { serverWorld ->
                        val chunkLoaderState = serverWorld.server.overworld.persistentStateManager.getOrCreate({ ChunkLoaderState(serverWorld.server, "kibe_chunk_loaders") }, "kibe_chunk_loaders")

                        playerUUID?.let { validUUID ->
                            val player = serverWorld.server.playerManager.getPlayer(validUUID)
                            if(player != null) {
                                blockEntity.ownerLastSeen = System.currentTimeMillis()
                            }else if (MOD_CONFIG.chunkLoaderModule.checkForPlayer) {
                                serverWorld.setBlockState(pos, state.with(Properties.ENABLED, false))
                                blockEntity.disabledReason = DisabledReason.OWNER_OFFLINE
                                blockEntity.markDirtyAndSync()
                                return
                            }
                            if (MOD_CONFIG.chunkLoaderModule.maxPerPlayer > -1 && chunkLoaderState.getLoaded(validUUID) > MOD_CONFIG.chunkLoaderModule.maxPerPlayer) {
                                serverWorld.setBlockState(pos, state.with(Properties.ENABLED, false))
                                blockEntity.disabledReason = DisabledReason.TOO_MANY_LOADERS
                                blockEntity.markDirtyAndSync()
                                return
                            }
                        }

                        if (MOD_CONFIG.chunkLoaderModule.maxOfflineTime > -1 && ((System.currentTimeMillis() - blockEntity.ownerLastSeen)/1000) > MOD_CONFIG.chunkLoaderModule.maxOfflineTime) {
                            serverWorld.setBlockState(pos, state.with(Properties.ENABLED, false))
                            blockEntity.disabledReason = DisabledReason.OWNER_LONG_GONE
                            blockEntity.markDirtyAndSync()
                            return
                        }

                        chunkLoaderState.addPos(pos, serverWorld)
                    }

                }
                blockEntity.delay = 0
            }
            blockEntity.delay++
        }
    }


}