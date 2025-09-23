package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.data.state.EntangledTankState
import io.github.lucaargolo.kibe.mixin.PersistentStateManagerAccessor
import io.github.lucaargolo.kibe.network.RequestDirtyTankStatesPacket
import io.github.lucaargolo.kibe.network.SynchronizeDirtyTankStatesPacket
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant

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

                    val state = EntangledTankState.getPersistentState(server.overworld, key)

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
                    ServerPlayNetworking.send(player, SynchronizeDirtyTankStatesPacket(finalMap))
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
                    ClientPlayNetworking.send(RequestDirtyTankStatesPacket(EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS))
                }
                EntangledTankState.PAST_CLIENT_PLAYER_REQUESTS = EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS
                EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS = linkedSetOf()
            }
        }
    }
}