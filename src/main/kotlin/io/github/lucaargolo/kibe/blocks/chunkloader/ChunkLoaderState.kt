package io.github.lucaargolo.kibe.blocks.chunkloader

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.*

class ChunkLoaderState(val server: MinecraftServer): PersistentState(){

    var loadedChunkMap: MutableMap<RegistryKey<World>, MutableList<BlockPos>> = mutableMapOf()
    var loadersPerUUID: MutableMap<String, Int> = mutableMapOf()

    fun getLoaded(uuid: UUID) = loadersPerUUID.getOrDefault(uuid.toString(), 0)

    fun isItBeingChunkLoaded(world: ServerWorld, chunkPos: ChunkPos): Boolean {
        loadedChunkMap.forEach { (wrldKey, list) ->
            list.forEach { pos ->
                val chunkList = mutableListOf<ChunkPos>()
                val blockEntity = world.getBlockEntity(pos) as? ChunkLoaderBlockEntity
                blockEntity?.enabledChunks?.forEach {
                    val centerChunkPos = ChunkPos(blockEntity.pos)
                    chunkList.add(ChunkPos(centerChunkPos.x+it.first, centerChunkPos.z+it.second))
                }
                if(chunkList.contains(chunkPos) && world.registryKey == wrldKey)  {
                    return true
                }
            }
        }
        return false
    }

    private fun setChunksForced(world: ServerWorld, blockEntity: ChunkLoaderBlockEntity, bool: Boolean) {
        if(bool) {
            loadersPerUUID[blockEntity.ownerUUID] = loadersPerUUID.getOrDefault(blockEntity.ownerUUID, 0)+1
        }else {
            loadersPerUUID[blockEntity.ownerUUID] = loadersPerUUID.getOrDefault(blockEntity.ownerUUID, 1)-1
        }
        val centerChunkPos = ChunkPos(blockEntity.pos)
        blockEntity.enabledChunks.forEach {
            world.chunkManager.setChunkForced(ChunkPos(centerChunkPos.x+it.first, centerChunkPos.z+it.second), bool)
        }
    }

    fun removePos(pos: BlockPos, world: ServerWorld) {
        val blockEntity = world.getBlockEntity(pos) as? ChunkLoaderBlockEntity
        blockEntity?.let {
            if (loadedChunkMap[world.registryKey] != null && loadedChunkMap[world.registryKey]!!.contains(pos)) {
                !loadedChunkMap[world.registryKey]!!.remove(pos)
                setChunksForced(world, blockEntity, false)
                markDirty()
            }
        }
    }

    fun addPos(pos: BlockPos, world: ServerWorld) {
        val blockEntity = world.getBlockEntity(pos) as? ChunkLoaderBlockEntity
        blockEntity?.let {
            if(loadedChunkMap[world.registryKey] == null) {
                loadedChunkMap[world.registryKey] = mutableListOf(pos)
                setChunksForced(world, blockEntity, true)
                markDirty()
            }else{
                if(!loadedChunkMap[world.registryKey]!!.contains(pos)) {
                    !loadedChunkMap[world.registryKey]!!.add(pos)
                    setChunksForced(world, blockEntity, true)
                    markDirty()
                }
            }
        }
    }

    override fun writeNbt(tag: CompoundTag): CompoundTag {
        loadedChunkMap.keys.forEach { dim ->
            val longArray = LongArray(loadedChunkMap[dim]!!.size)
            loadedChunkMap[dim]!!.forEachIndexed { idx, pos ->
                longArray[idx] = pos.asLong()
            }
            tag.putLongArray(dim.value.toString(), longArray)
        }
        return tag;
    }

    companion object {
        fun createFromTag(tag: CompoundTag, server: MinecraftServer): ChunkLoaderState {
            val state = ChunkLoaderState(server)
            if(state.loadedChunkMap.keys.size > 0) {
                state.loadedChunkMap.forEach { (key, pos) ->
                    val world = server.getWorld(key)
                    world?.let {
                        pos.forEach {
                            val blockEntity = world.getBlockEntity(it) as? ChunkLoaderBlockEntity
                            blockEntity?.let { state.setChunksForced(world, blockEntity, false) }
                        }
                    }

                }
            }
            state.loadedChunkMap = mutableMapOf()
            tag.keys.forEach { key ->
                val registryKey = RegistryKey.of(Registry.DIMENSION, Identifier(key))
                val world = server.getWorld(registryKey)
                world?.let { _ ->
                    state.loadedChunkMap[registryKey] = mutableListOf()
                    val listTag = tag.getLongArray(key)
                    listTag.forEach {
                        val blockPos = BlockPos.fromLong(it)
                        val blockState = world.getBlockState(blockPos)
                        if(blockState.block is ChunkLoader) {
                            state.loadedChunkMap[registryKey]!!.add(blockPos)
                            val blockEntity = world.getBlockEntity(blockPos) as? ChunkLoaderBlockEntity
                            blockEntity?.let { state.setChunksForced(world, blockEntity, true) }
                        }
                    }
                }
            }
            return state;
        }
    }

}