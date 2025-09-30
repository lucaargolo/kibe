package io.github.lucaargolo.kibe.network

import io.github.lucaargolo.kibe.blockentity.ChunkLoaderBlockEntity
import io.github.lucaargolo.kibe.data.state.EntangledTankState
import io.github.lucaargolo.kibe.utils.EntangledChestAnimationState
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.math.ChunkPos

object PacketCompendium {

    fun initialize() {
        PayloadTypeRegistry.playC2S().register(ChunkPlayerCheckPacket.ID, ChunkPlayerCheckPacket.PACKET_CODEC)
        PayloadTypeRegistry.playC2S().register(ChunkMapClickPacket.ID, ChunkMapClickPacket.PACKET_CODEC)
        PayloadTypeRegistry.playC2S().register(RequestDirtyTankStatesPacket.ID, RequestDirtyTankStatesPacket.PACKET_CODEC)
        PayloadTypeRegistry.playS2C().register(SynchronizeDirtyTankStatesPacket.ID, SynchronizeDirtyTankStatesPacket.PACKET_CODEC)
        PayloadTypeRegistry.playS2C().register(EntangledChestAnimationStatePacket.ID, EntangledChestAnimationStatePacket.PACKET_CODEC)

        ServerPlayNetworking.registerGlobalReceiver(ChunkPlayerCheckPacket.ID) { data, context ->
            context.server().execute {
                val world = context.player().world
                val be = world.getBlockEntity(data.pos) as? ChunkLoaderBlockEntity
                be?.let {
                    if (context.player().uuidAsString == it.ownerUUID) {
                        it.checkForOwner = !it.checkForOwner
                        be.markDirtyAndSync()
                    }
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(ChunkMapClickPacket.ID) { data, context ->
            if (data.x in (-2..2) || data.z in (-2..2)) {
                context.server().execute {
                    val world = context.player().world
                    val be = world.getBlockEntity(data.pos) as? ChunkLoaderBlockEntity
                    be?.let {
                        if (be.enabledChunks.contains(Pair(data.x, data.z))) {
                            world.chunkManager.setChunkForced(ChunkPos(ChunkPos(be.pos).x, ChunkPos(be.pos).z), false)
                            be.enabledChunks.remove(Pair(data.x, data.z))
                        } else {
                            world.chunkManager.setChunkForced(ChunkPos(ChunkPos(be.pos).x, ChunkPos(be.pos).z), true)
                            be.enabledChunks.add(Pair(data.x, data.z))
                        }
                        be.markDirtyAndSync()
                    }
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(RequestDirtyTankStatesPacket.ID) { data, context ->
            context.server().execute {
                EntangledTankState.SERVER_PLAYER_REQUESTS[context.player()] = data.set
            }
        }
    }

    fun initializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SynchronizeDirtyTankStatesPacket.ID) { data, context ->
            context.client().execute {
                data.map.forEach { (key, map) ->
                    val state = EntangledTankState.getOrCreateClientState(key)
                    map.forEach { (colorCode, fluidVolume) ->
                        state.getOrCreateInventory(colorCode).let {
                            it.variant = fluidVolume.first
                            it.amount = fluidVolume.second
                        }
                    }
                }
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(EntangledChestAnimationStatePacket.ID) { data, context ->
            context.client().execute {
                EntangledChestAnimationState.update(data.opened)
            }
        }
    }

}