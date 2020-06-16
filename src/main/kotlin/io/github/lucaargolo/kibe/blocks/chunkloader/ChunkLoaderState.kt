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

class ChunkLoaderState(val server: MinecraftServer, val key: String): PersistentState(key){

    var loadedChunkMap: MutableMap<RegistryKey<World>, MutableList<BlockPos>> = mutableMapOf();

    fun isItBeingChunkLoaded(world: ServerWorld, chunkPos: ChunkPos): Boolean {
        loadedChunkMap.forEach { (wrldKey, list) ->
            list.forEach {
                val chunk = ChunkPos(it)
                val chunkList = mutableListOf<ChunkPos>()
                chunkList.add(chunkPos)
                chunkList.add(ChunkPos(chunkPos.x+1, chunkPos.z))
                chunkList.add(ChunkPos(chunkPos.x-1, chunkPos.z))
                chunkList.add(ChunkPos(chunkPos.x, chunkPos.z+1))
                chunkList.add(ChunkPos(chunkPos.x, chunkPos.z-1))
                chunkList.add(ChunkPos(chunkPos.x+1, chunkPos.z+1))
                chunkList.add(ChunkPos(chunkPos.x+1, chunkPos.z-1))
                chunkList.add(ChunkPos(chunkPos.x-1, chunkPos.z+1))
                chunkList.add(ChunkPos(chunkPos.x-1, chunkPos.z-1))
                if(chunkList.contains(chunkPos) && world.registryKey == wrldKey)  {
                    return true
                }
            }
        }
        return false
    }

    private fun setChunksForced(world: ServerWorld, chunkPos: ChunkPos, bool: Boolean) {
        world.chunkManager.setChunkForced(chunkPos, bool)
        world.chunkManager.setChunkForced(ChunkPos(chunkPos.x+1, chunkPos.z), bool)
        world.chunkManager.setChunkForced(ChunkPos(chunkPos.x-1, chunkPos.z), bool)
        world.chunkManager.setChunkForced(ChunkPos(chunkPos.x, chunkPos.z+1), bool)
        world.chunkManager.setChunkForced(ChunkPos(chunkPos.x, chunkPos.z-1), bool)
        world.chunkManager.setChunkForced(ChunkPos(chunkPos.x+1, chunkPos.z+1), bool)
        world.chunkManager.setChunkForced(ChunkPos(chunkPos.x+1, chunkPos.z-1), bool)
        world.chunkManager.setChunkForced(ChunkPos(chunkPos.x-1, chunkPos.z+1), bool)
        world.chunkManager.setChunkForced(ChunkPos(chunkPos.x-1, chunkPos.z-1), bool)
    }

    fun removePos(pos: BlockPos, world: ServerWorld) {
        if(loadedChunkMap[world.registryKey] != null && loadedChunkMap[world.registryKey]!!.contains(pos)) {
            !loadedChunkMap[world.registryKey]!!.remove(pos)
            setChunksForced(world, ChunkPos(pos), false)
            markDirty()
        }
    }

    fun addPos(pos: BlockPos, world: ServerWorld) {
        if(loadedChunkMap[world.registryKey] == null) {
            loadedChunkMap[world.registryKey] = mutableListOf(pos)
            setChunksForced(world, ChunkPos(pos), true)
            markDirty()
        }else{
            if(!loadedChunkMap[world.registryKey]!!.contains(pos)) {
                !loadedChunkMap[world.registryKey]!!.add(pos)
                setChunksForced(world, ChunkPos(pos), true)
                markDirty()
            }
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        loadedChunkMap.keys.forEach { dim ->
            val longArray = LongArray(loadedChunkMap[dim]!!.size)
            loadedChunkMap[dim]!!.forEachIndexed { idx, pos ->
                longArray[idx] = pos.asLong()
            }
            tag.putLongArray(dim.value.toString(), longArray)
        }
        return tag;
    }

    override fun fromTag(tag: CompoundTag) {
        if(loadedChunkMap.keys.size > 0) {
            loadedChunkMap.forEach { key, pos ->
                val world = server.getWorld(key)
                world?.let { world ->
                    pos.forEach {
                        val chunkPos = ChunkPos(it)
                        setChunksForced(world, chunkPos, false)
                    }
                }

            }
        }
        loadedChunkMap = mutableMapOf()
        tag.keys.forEach { key ->
            val registryKey = RegistryKey.of(Registry.DIMENSION, Identifier(key))
            val world = server.getWorld(registryKey)
            world?.let { world ->
                loadedChunkMap[registryKey] = mutableListOf()
                val listTag = tag.getLongArray(key)
                listTag.forEach {
                    val blockPos = BlockPos.fromLong(it)
                    val blockState = world.getBlockState(blockPos)
                    if(blockState.block is ChunkLoader) {
                        loadedChunkMap[registryKey]!!.add(blockPos)
                        val chunkPos = ChunkPos(blockPos)
                        setChunksForced(world, chunkPos, true)
                    }
                }
            }
        }
    }

}