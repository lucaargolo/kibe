@file:Suppress("unused")

package io.github.lucaargolo.kibe

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import io.github.lucaargolo.kibe.blocks.*
import io.github.lucaargolo.kibe.blocks.COOLER
import io.github.lucaargolo.kibe.blocks.ENTANGLED_CHEST
import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.drawbridge.DrawbridgeCustomModel
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import io.github.lucaargolo.kibe.blocks.tank.TankCustomModel
import io.github.lucaargolo.kibe.entities.initEntitiesClient
import io.github.lucaargolo.kibe.fluids.initFluidsClient
import io.github.lucaargolo.kibe.items.*
import io.github.lucaargolo.kibe.items.entangledchest.EntangledChestBlockItemDynamicRenderer
import io.github.lucaargolo.kibe.items.entangledtank.EntangledTankBlockItemDynamicRenderer
import io.github.lucaargolo.kibe.items.miscellaneous.GliderDynamicRenderer
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.network.PacketByteBuf
import net.minecraft.resource.ResourceManager
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import java.util.function.Consumer

fun initClient() {
    initBlocksClient()
    initItemsClient()
    initEntitiesClient()
    initFluidsClient()
    initExtrasClient()
    initPacketsClient()
}

fun initPacketsClient() {

    ClientPlayNetworking.registerGlobalReceiver(SYNCHRONIZE_DIRTY_TANK_STATES) { client, _, buf, _ ->
        val tot = buf.readInt()
        repeat(tot) {
            val key = buf.readString()
            val qnt = buf.readInt()
            val map = mutableMapOf<String, FluidVolume>()
            repeat(qnt) {
                val colorCode = buf.readString()
                val fluidVolume = FluidVolume.fromMcBuffer(buf)
                map[colorCode] = fluidVolume
            }
            client.execute {
                val state = EntangledTankState.getOrCreateClientState(key)
                map.forEach { (colorCode, fluidVolume) ->
                    state.getOrCreateInventory(colorCode).setInvFluid(0, fluidVolume, Simulation.ACTION)
                }
            }
        }
    }

}

fun initExtrasClient() {
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
                ClientPlayNetworking.send(REQUEST_DIRTY_TANK_STATES, passedData)
            }
            EntangledTankState.PAST_CLIENT_PLAYER_REQUESTS = EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS
            EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS = linkedSetOf()
        }
    }
    ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(ClientSpriteRegistryCallback { _, registry ->
        registry.register(Identifier(MOD_ID, "block/entangled_chest"))
        registry.register(Identifier(MOD_ID, "block/entangled_chest_runes"))
        (0..15).forEach {
            registry.register(Identifier(MOD_ID, "block/redstone_timer_$it"))
        }
        registry.register(Identifier(MOD_ID, "block/tank"))
    })
    ModelLoadingRegistry.INSTANCE.registerModelProvider { _: ResourceManager, out: Consumer<Identifier> ->
        out.accept(ModelIdentifier(Identifier(MOD_ID, "redstone_timer_structure"), ""))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "glider_handle"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "white_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "white_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "orange_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "orange_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "magenta_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "magenta_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "light_blue_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "light_blue_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "yellow_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "yellow_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "lime_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "lime_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "pink_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "pink_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "gray_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "gray_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "light_gray_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "light_gray_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "cyan_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "cyan_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "blue_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "blue_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "purple_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "purple_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "green_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "green_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "brown_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "brown_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "red_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "red_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "black_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "black_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_ring"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_background"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_gold_core"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_diamond_core"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_fluid"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_background"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_foreground"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_gold_core"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_diamond_core"), "inventory"))
    }
    BlockRenderLayerMap.INSTANCE.putBlock(DRAWBRIDGE, RenderLayer.getCutoutMipped())
    BlockRenderLayerMap.INSTANCE.putBlock(VACUUM_HOPPER, RenderLayer.getTranslucent())
    BlockRenderLayerMap.INSTANCE.putBlock(BIG_TORCH, RenderLayer.getCutoutMipped())
    BlockRenderLayerMap.INSTANCE.putBlock(COOLER, RenderLayer.getTranslucent())
    BlockRenderLayerMap.INSTANCE.putBlock(ENTANGLED_TANK, RenderLayer.getCutoutMipped())
    ModelLoadingRegistry.INSTANCE.registerVariantProvider {
        ModelVariantProvider { modelIdentifier, _ ->
            if(modelIdentifier.namespace == MOD_ID && modelIdentifier.path == "drawbridge") {
                return@ModelVariantProvider DrawbridgeCustomModel()
            }
            if(modelIdentifier.namespace == MOD_ID && modelIdentifier.path == "tank" && modelIdentifier.variant != "inventory") {
                val model = TankCustomModel()
                TANK_CUSTOM_MODEL = model
                return@ModelVariantProvider model
            }
            return@ModelVariantProvider null
        }
    }
    BuiltinItemRendererRegistry.INSTANCE.register(ENTANGLED_CHEST, EntangledChestBlockItemDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(ENTANGLED_TANK, EntangledTankBlockItemDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(WHITE_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(ORANGE_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(MAGENTA_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(LIGHT_BLUE_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(YELLOW_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(LIME_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(PINK_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(GRAY_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(LIGHT_GRAY_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(CYAN_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(BLUE_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(PURPLE_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(GREEN_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(BROWN_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(RED_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(BLACK_GLIDER, GliderDynamicRenderer())
}