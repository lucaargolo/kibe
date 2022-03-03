package io.github.lucaargolo.kibe.blocks.chunkloader

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.*

class ChunkLoaderState(val server: MinecraftServer): PersistentState(){

    private var chunkReferenceMap: MutableMap<RegistryKey<World>, MutableMap<ChunkPos, ArrayList<BlockPos>>> = mutableMapOf()

    var lastCacheReset: MutableMap<RegistryKey<World>, Long> = mutableMapOf()
    var chunkLoaderCache: MutableMap<RegistryKey<World>, MutableSet<ChunkPos>> = mutableMapOf()
    var loadedChunkMap: MutableMap<RegistryKey<World>, MutableList<BlockPos>> = mutableMapOf()
    var loadersPerUUID: MutableMap<String, Int> = mutableMapOf()

    fun getLoaded(uuid: UUID) = loadersPerUUID.getOrDefault(uuid.toString(), 0)

    fun isItBeingChunkLoaded(world: ServerWorld, chunkPos: ChunkPos): Boolean {
        if(world.time - (lastCacheReset[world.registryKey] ?: 0L) >= 100) {
            lastCacheReset.clear()
            lastCacheReset[world.registryKey] = world.time
        }
        if(chunkLoaderCache[world.registryKey]?.contains(chunkPos) == true) {
            return true
        }
        loadedChunkMap[world.registryKey]?.filter { Vec3d(chunkPos.centerX+0.5, 0.0, chunkPos.centerZ+0.5).distanceTo(Vec3d(it.x+0.5, 0.0, it.z+0.5)) <= 160.0 }?.forEach { pos ->
            val chunkList = mutableListOf<ChunkPos>()
            val blockEntity = world.getBlockEntity(pos) as? ChunkLoaderBlockEntity
            blockEntity?.enabledChunks?.forEach {
                val centerChunkPos = ChunkPos(blockEntity.pos)
                chunkList.add(ChunkPos(centerChunkPos.x+it.first, centerChunkPos.z+it.second))
            }
            if(chunkList.contains(chunkPos))  {
                chunkLoaderCache.getOrPut(world.registryKey) { mutableSetOf() }.add(chunkPos)
                return true
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
        blockEntity.enabledChunks.forEach { chunkOffset ->
            val chunkPos = ChunkPos(centerChunkPos.x+chunkOffset.first, centerChunkPos.z+chunkOffset.second)
            val worldChunkReferenceMap = chunkReferenceMap.getOrPut(world.registryKey) { mutableMapOf() }
            val chunkReference = worldChunkReferenceMap.getOrPut(chunkPos) { arrayListOf() }
            if(bool) {
                chunkReference.add(blockEntity.pos)
                world.chunkManager.setChunkForced(chunkPos, true)
            }else{
                val chunkReferenceIterator = chunkReference.iterator()
                var foundOtherLoader = false
                while(chunkReferenceIterator.hasNext()) {
                    val referencePos = chunkReferenceIterator.next()
                    val referenceBlockEntity = (world.getBlockEntity(referencePos) as? ChunkLoaderBlockEntity)
                    if(referenceBlockEntity?.let { !it.isRemoved && it != blockEntity } == true) {
                        foundOtherLoader = true
                    }else{
                        chunkReferenceIterator.remove()
                    }
                }
                if(!foundOtherLoader) {
                    world.chunkManager.setChunkForced(chunkPos, false)
                }
            }
        }
    }

    fun removePos(pos: BlockPos, world: ServerWorld) {
        val blockEntity = world.getBlockEntity(pos) as? ChunkLoaderBlockEntity
        blockEntity?.let {
            loadedChunkMap[world.registryKey]?.let{
                if(it.contains(pos)) {
                    it.remove(pos)
                    setChunksForced(world, blockEntity, false)
                    markDirty()
                }
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
            }
            loadedChunkMap[world.registryKey]?.let {
                if(!it.contains(pos)) {
                    it.add(pos)
                    setChunksForced(world, blockEntity, true)
                    markDirty()
                }
            }
        }
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        loadedChunkMap.keys.forEach { dim ->
            val longArray = LongArray(loadedChunkMap[dim]!!.size)
            loadedChunkMap[dim]!!.forEachIndexed { idx, pos ->
                longArray[idx] = pos.asLong()
            }
            tag.putLongArray(dim.value.toString(), longArray)
        }
        return tag
    }

    companion object {
        fun createFromTag(tag: NbtCompound, server: MinecraftServer): ChunkLoaderState {
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
                val registryKey = RegistryKey.of(Registry.WORLD_KEY, Identifier(key))
                val world = server.getWorld(registryKey)
                world?.let { _ ->
                    state.loadedChunkMap[registryKey] = mutableListOf()
                    val NbtList = tag.getLongArray(key)
                    NbtList.forEach {
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
            return state
        }
    }

}