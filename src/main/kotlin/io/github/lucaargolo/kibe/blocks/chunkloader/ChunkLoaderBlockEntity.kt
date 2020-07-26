package io.github.lucaargolo.kibe.blocks.chunkloader

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Tickable

class ChunkLoaderBlockEntity(block: Block): BlockEntity(getEntityType(block)), Tickable {

    private var delay = 0;

    override fun tick() {
        if(delay >= 40) {
            world?.let { world ->
                if(!world.isClient) {
                    //println("hello there, im ticking at ${world.registryKey}, (x: ${pos.x}, y: ${pos.y}, z: ${pos.z})")
                    val chunkLoaderState = (world as ServerWorld).server.overworld.persistentStateManager.getOrCreate({ ChunkLoaderState(world.server, "kibe_chunk_loaders") }, "kibe_chunk_loaders")
                    chunkLoaderState.addPos(pos, world)
                }
            }
            delay = 0;
        }
        delay++
    }


}