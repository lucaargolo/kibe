package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.data.EntangledTankState
import io.github.lucaargolo.kibe.mixin.PersistentStateManagerAccessor
import io.github.lucaargolo.kibe.network.PacketCompendium
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.network.PacketByteBuf

object EntangledTankSync {

    fun initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register { handler, server ->
            server.execute {
                EntangledTankState.ALL_TIME_PLAYER_REQUESTS.remove(handler.player)
                EntangledTankState.SERVER_PLAYER_REQUESTS.remove(handler.player)
            }
        }
        ServerTickEvents.END_SERVER_TICK.register { server ->
            EntangledTankState.SERVER_PLAYER_REQUESTS.forEach { (player, requests) ->
                val finalMap = mutableMapOf<String, MutableMap<String, Pair<FluidVariant, Long>>>()
                requests.forEach { pair ->
                    val key = pair.first
                    val colorCode = pair.second

                    val state = server.overworld.persistentStateManager.getOrCreate({
                        EntangledTankState.createFromTag(it, server.overworld, key)
                    }, { EntangledTankState(server.overworld, key) }, key)
                    val allTimeRequests = EntangledTankState.ALL_TIME_PLAYER_REQUESTS.getOrPut(player) { linkedSetOf() }
                    if (!allTimeRequests.contains(pair) || state.dirtyColors.contains(colorCode)) {
                        allTimeRequests.add(pair)
                        val fluidVolume = state.getOrCreateInventory(colorCode).let { Pair(it.variant, it.amount) }

                        val secondMap = finalMap[key] ?: mutableMapOf()
                        secondMap[colorCode] = fluidVolume
                        finalMap[key] = secondMap
                    }
                }
                if (finalMap.isNotEmpty()) {
                    val passedData = PacketByteBuf(Unpooled.buffer())
                    passedData.writeInt(finalMap.size)
                    finalMap.forEach { (key, secondMap) ->
                        passedData.writeString(key, 32767)
                        passedData.writeInt(secondMap.size)
                        secondMap.forEach { (colorCode, fluidVolume) ->
                            passedData.writeString(colorCode, 32767)
                            fluidVolume.first.toPacket(passedData)
                            passedData.writeLong(fluidVolume.second)
                        }
                    }
                    ServerPlayNetworking.send(player, PacketCompendium.SYNCHRONIZE_DIRTY_TANK_STATES, passedData)
                }
            }
            (server.overworld.persistentStateManager as? PersistentStateManagerAccessor)?.loadedStates?.forEach { (_, state) ->
                (state as? EntangledTankState)?.dirtyColors?.clear()
            }
        }
    }

    fun initializeClient() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            EntangledTankState.CLIENT_STATES.clear()
            EntangledTankState.PAST_CLIENT_PLAYER_REQUESTS = linkedSetOf()
            EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS = linkedSetOf()
        }
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            client.world?.let { _ ->
                if (EntangledTankState.PAST_CLIENT_PLAYER_REQUESTS != EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS) {
                    val passedData = PacketByteBuf(Unpooled.buffer())
                    passedData.writeInt(EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.size)
                    EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.forEach {
                        passedData.writeString(it.first)
                        passedData.writeString(it.second)
                    }
                    ClientPlayNetworking.send(PacketCompendium.REQUEST_DIRTY_TANK_STATES, passedData)
                }
                EntangledTankState.PAST_CLIENT_PLAYER_REQUESTS = EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS
                EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS = linkedSetOf()
            }
        }
    }
}