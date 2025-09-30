package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.menu.EntangledBagScreenHandler
import io.github.lucaargolo.kibe.menu.EntangledChestScreenHandler
import io.github.lucaargolo.kibe.network.EntangledChestAnimationStatePacket
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

object EntangledChestOpenState {

    private val map = mutableMapOf<Pair<String, String>, MutableSet<UUID>>()

    fun open(player: ServerPlayerEntity, key: String, colorCode: String) {
        map.getOrPut(Pair(key, colorCode)) { mutableSetOf() }.add(player.uuid)
        syncAll(player.server)
    }

    fun close(player: ServerPlayerEntity, key: String, colorCode: String) {
        map.getOrPut(Pair(key, colorCode)) { mutableSetOf() }.remove(player.uuid)
        syncAll(player.server)
    }

    fun initialize() {
        ServerTickEvents.END_SERVER_TICK.register { server ->
            tick(server)
        }
        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            val payload = EntangledChestAnimationStatePacket(map.keys)
            ServerPlayNetworking.send(handler.player, payload)
        }
    }

    private fun syncAll(server: MinecraftServer) {
        val payload = EntangledChestAnimationStatePacket(map.keys)
        server.playerManager.playerList.forEach { player ->
            ServerPlayNetworking.send(player, payload)
        }
    }

    private fun tick(server: MinecraftServer) {
        val iterator = map.iterator()
        var isDirty = false
        while (iterator.hasNext()) {
            val next = iterator.next()
            var isValid = false
            next.value.forEach { uuid ->
                val player = server.playerManager.getPlayer(uuid)
                if(player != null) {
                    val menu = player.currentScreenHandler
                    isValid = isValid || (menu is EntangledChestScreenHandler && menu.entity.key == next.key.first && menu.entity.colorCode == next.key.second) || (menu is EntangledBagScreenHandler && menu.key == next.key.first && menu.colorCode == next.key.second)
                    if(isValid) return@forEach
                }
            }
            if(!isValid) {
                iterator.remove()
                isDirty = true
            }
        }
        if(isDirty) {
            syncAll(server)
        }
    }


}