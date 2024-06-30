package io.github.lucaargolo.kibe.network

import io.github.lucaargolo.kibe.blockentity.ChunkLoaderBlockEntity
import io.github.lucaargolo.kibe.data.EntangledTankState
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.util.math.ChunkPos

object PacketCompendium {

    val CHUNK_PLAYER_CHECK = ModIdentifier.of("chunk_player_check")
    val CHUNK_MAP_CLICK = ModIdentifier.of("chunk_map_click")
    val REQUEST_DIRTY_TANK_STATES = ModIdentifier.of("request_dirty_tank_states")

    fun initialize() {
        ServerPlayNetworking.registerGlobalReceiver(CHUNK_PLAYER_CHECK) { server, player, _, attachedData, _ ->
            val pos = attachedData.readBlockPos()
            server.execute {
                val world = player.world
                val be = world.getBlockEntity(pos) as? ChunkLoaderBlockEntity
                be?.let {
                    if (player.uuidAsString == it.ownerUUID) {
                        it.checkForOwner = !it.checkForOwner
                        be.markDirtyAndSync()
                    }
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(CHUNK_MAP_CLICK) { server, player, _, attachedData, _ ->
            val x = attachedData.readInt()
            val z = attachedData.readInt()
            val pos = attachedData.readBlockPos()
            if (x in (-2..2) || z in (-2..2)) {
                server.execute {
                    val world = player.world
                    val be = world.getBlockEntity(pos) as? ChunkLoaderBlockEntity
                    be?.let {
                        if (be.enabledChunks.contains(Pair(x, z))) {
                            world.chunkManager.setChunkForced(ChunkPos(ChunkPos(be.pos).x, ChunkPos(be.pos).z), false)
                            be.enabledChunks.remove(Pair(x, z))
                        } else {
                            world.chunkManager.setChunkForced(ChunkPos(ChunkPos(be.pos).x, ChunkPos(be.pos).z), true)
                            be.enabledChunks.add(Pair(x, z))
                        }
                        be.markDirtyAndSync()
                    }
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(REQUEST_DIRTY_TANK_STATES) { server, player, _, attachedData, _ ->
            val set = linkedSetOf<Pair<String, String>>()
            val qnt = attachedData.readInt()
            repeat(qnt) {
                val first = attachedData.readString(32767)
                val second = attachedData.readString(32767)
                set.add(Pair(first, second))
            }
            server.execute {
                EntangledTankState.SERVER_PLAYER_REQUESTS[player] = set
            }
        }
    }

    val SYNCHRONIZE_DIRTY_TANK_STATES = ModIdentifier.of("synchronize_dirty_tank_states")

    fun initializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SYNCHRONIZE_DIRTY_TANK_STATES) { client, _, buf, _ ->
            val tot = buf.readInt()
            repeat(tot) {
                val key = buf.readString()
                val qnt = buf.readInt()
                val map = mutableMapOf<String, Pair<FluidVariant, Long>>()
                repeat(qnt) {
                    val colorCode = buf.readString()
                    val fluidVolume = Pair(FluidVariant.fromPacket(buf), buf.readLong())
                    map[colorCode] = fluidVolume
                }
                client.execute {
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
    }

}