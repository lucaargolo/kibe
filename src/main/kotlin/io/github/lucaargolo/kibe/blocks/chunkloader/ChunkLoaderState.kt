package io.github.lucaargolo.kibe.blocks.chunkloader

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.PersistentState
import net.minecraft.world.dimension.DimensionType

class ChunkLoaderState(val server: MinecraftServer, val key: String): PersistentState(key){

    private var loadedChunkMap: MutableMap<DimensionType, MutableList<BlockPos>> = mutableMapOf();

    fun isItBeingChunkLoaded(world: ServerWorld, chunkPos: ChunkPos): Boolean {
        loadedChunkMap.forEach { (dimensionType, list) ->
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
                if(chunkList.contains(chunkPos) && world.dimension.type == dimensionType)  {
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
        if(loadedChunkMap[world.dimension.type] != null && loadedChunkMap[world.dimension.type]!!.contains(pos)) {
            !loadedChunkMap[world.dimension.type]!!.remove(pos)
            setChunksForced(world, ChunkPos(pos), false)
            markDirty()
        }
    }

    fun addPos(pos: BlockPos, world: ServerWorld) {
        if(loadedChunkMap[world.dimension.type] == null) {
            loadedChunkMap[world.dimension.type] = mutableListOf(pos)
            setChunksForced(world, ChunkPos(pos), true)
            markDirty()
        }else{
            if(!loadedChunkMap[world.dimension.type]!!.contains(pos)) {
                !loadedChunkMap[world.dimension.type]!!.add(pos)
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
            tag.putLongArray(dim.toString(), longArray)
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
        tag.keys.forEach{ key ->
            val dimensionType = DimensionType.byId(Identifier(key))
            val world = server.getWorld(dimensionType)
            world?.let { world ->
                loadedChunkMap[dimensionType!!] = mutableListOf()
                val listTag = tag.getLongArray(key)
                listTag.forEach {
                    val blockPos = BlockPos.fromLong(it)
                    val blockState = world.getBlockState(blockPos)
                    if(blockState.block is ChunkLoader) {
                        loadedChunkMap[dimensionType]!!.add(blockPos)
                        val chunkPos = ChunkPos(blockPos)
                        setChunksForced(world, chunkPos, true)
                    }
                }
            }
        }
    }

}