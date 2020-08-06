package io.github.lucaargolo.kibe.blocks.chunkloader

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Tickable

class ChunkLoaderBlockEntity(val block: Block): BlockEntity(getEntityType(block)), Tickable, BlockEntityClientSerializable {

    private var delay = 0

    var enabledChunks = mutableListOf(
        Pair(-1, -1), Pair(0, -1), Pair(1, -1),
        Pair(-1, 0), Pair(0, 0), Pair(1, 0),
        Pair(-1, 1), Pair(0, 1), Pair(1, 1)
    )

    override fun tick() {
        if(delay >= 40) {
            world?.let { world ->
                if(!world.isClient) {
                    val chunkLoaderState = (world as ServerWorld).server.overworld.persistentStateManager.getOrCreate({ ChunkLoaderState(world.server, "kibe_chunk_loaders") }, "kibe_chunk_loaders")
                    chunkLoaderState.addPos(pos, world)
                }
            }
            delay = 0;
        }
        delay++
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
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

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
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
        fromTag(block.defaultState, tag)
    }


}