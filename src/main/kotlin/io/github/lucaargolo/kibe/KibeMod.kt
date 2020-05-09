package io.github.lucaargolo.kibe

import io.github.lucaargolo.kibe.blocks.initBlocks
import io.github.lucaargolo.kibe.blocks.initBlocksClient
import io.github.lucaargolo.kibe.effects.initEffects
import io.github.lucaargolo.kibe.items.initItems
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.container.PlayerContainer
import net.minecraft.util.Identifier

const val MOD_ID = "kibe"

@Suppress("unused")
fun init() {
    initBlocks()
    initItems()
    initEffects()
}

@Suppress("unused")
fun initClient() {
    initBlocksClient()
    initTexturesClient()
}

fun initTexturesClient() {
    @Suppress("deprecated")
    ClientSpriteRegistryCallback.event(PlayerContainer.BLOCK_ATLAS_TEXTURE).register(ClientSpriteRegistryCallback { _, registry ->
        (0..15).forEach{
            registry.register(Identifier(MOD_ID, "block/redstone_timer_$it"))
        }
    })
}

