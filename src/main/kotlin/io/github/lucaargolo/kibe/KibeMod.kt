@file:Suppress("unused")

package io.github.lucaargolo.kibe

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidContainerRegistry
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import io.github.lucaargolo.kibe.blocks.*
import io.github.lucaargolo.kibe.blocks.COOLER
import io.github.lucaargolo.kibe.blocks.ENTANGLED_CHEST
import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderBlockEntity
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderState
import io.github.lucaargolo.kibe.blocks.drawbridge.DrawbridgeCustomModel
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import io.github.lucaargolo.kibe.blocks.tank.TankCustomModel
import io.github.lucaargolo.kibe.blocks.vacuum.VacuumHopperScreen
import io.github.lucaargolo.kibe.compat.initTrinketsCompat
import io.github.lucaargolo.kibe.effects.CURSED_EFFECT
import io.github.lucaargolo.kibe.effects.initEffects
import io.github.lucaargolo.kibe.entities.initEntities
import io.github.lucaargolo.kibe.entities.initEntitiesClient
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import io.github.lucaargolo.kibe.fluids.initFluids
import io.github.lucaargolo.kibe.fluids.initFluidsClient
import io.github.lucaargolo.kibe.items.*
import io.github.lucaargolo.kibe.items.entangledchest.EntangledChestBlockItemDynamicRenderer
import io.github.lucaargolo.kibe.items.entangledtank.EntangledTankBlockItemDynamicRenderer
import io.github.lucaargolo.kibe.items.miscellaneous.GliderDynamicRenderer
import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_SERIALIZER
import io.github.lucaargolo.kibe.recipes.initRecipeSerializers
import io.github.lucaargolo.kibe.recipes.initRecipeTypes
import io.github.lucaargolo.kibe.utils.ModConfig
import io.github.lucaargolo.kibe.utils.initCreativeTab
import io.github.lucaargolo.kibe.utils.initTooltip
import io.netty.buffer.Unpooled
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.launch.common.FabricLauncherBase
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.Items
import net.minecraft.loot.ConstantLootTableRange
import net.minecraft.loot.UniformLootTableRange
import net.minecraft.loot.condition.EntityPropertiesLootCondition
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.LootingEnchantLootFunction
import net.minecraft.network.PacketByteBuf
import net.minecraft.predicate.entity.EntityEffectPredicate
import net.minecraft.predicate.entity.EntityPredicate
import net.minecraft.resource.ResourceManager
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.ChunkPos
import java.util.*
import java.util.function.Consumer

const val MOD_ID = "kibe"
val FAKE_PLAYER_UUID: UUID = UUID.randomUUID()
val CHUNK_MAP_CLICK = Identifier(MOD_ID, "chunk_map_click")
val REQUEST_DIRTY_TANK_STATES = Identifier(MOD_ID, "request_dirty_tank_states")
val SYNCHRONIZE_DIRTY_TANK_STATES = Identifier(MOD_ID, "synchronize_dirty_tank_states")
val SYNCHRONIZE_LAST_RECIPE_PACKET = Identifier(MOD_ID, "synchronize_last_recipe")
val CLIENT = FabricLauncherBase.getLauncher().environmentType == EnvType.CLIENT
val TRINKET = FabricLauncherBase.getLauncher().isClassLoaded("dev.emi.trinkets.api.Trinket")
var TANK_CUSTOM_MODEL: Any? = null
var MOD_CONFIG: ModConfig = ModConfig()

fun Boolean.toInt() = if (this) 1 else 0

fun init() {
    initRecipeSerializers()
    initRecipeTypes()
    initTooltip()
    initBlocks()
    initItems()
    initEntities()
    initEffects()
    initLootTables()
    initFluids()
    initCreativeTab()
    initPackets()
    initExtras()
    if(TRINKET) {
        initTrinketsCompat()
    }
}

fun initClient() {
    initBlocksClient()
    initItemsClient()
    initEntitiesClient()
    initFluidsClient()
    initExtrasClient()
    initPacketsClient()
}

fun initPackets() {
    ServerPlayNetworking.registerGlobalReceiver(CHUNK_MAP_CLICK) { server, player, _, attachedData, _ ->
        val x = attachedData.readInt()
        val z = attachedData.readInt()
        val pos = attachedData.readBlockPos()
        if(x in (-2..2) || z in (-2..2)) {
            server.execute {
                val world = player.world
                val be = world.getBlockEntity(pos) as? ChunkLoaderBlockEntity
                be?.let {
                    if(be.enabledChunks.contains(Pair(x, z))) {
                        world.chunkManager.setChunkForced(ChunkPos(ChunkPos(be.pos).x, ChunkPos(be.pos).z), false)
                        be.enabledChunks.remove(Pair(x, z))
                    } else {
                        world.chunkManager.setChunkForced(ChunkPos(ChunkPos(be.pos).x, ChunkPos(be.pos).z), true)
                        be.enabledChunks.add(Pair(x, z))
                    }
                    be.markDirty()
                    be.sync()
                }
            }
        }
    }

    ServerPlayNetworking.registerGlobalReceiver(REQUEST_DIRTY_TANK_STATES)  { server, player, _, attachedData, _ ->
        val list = linkedSetOf<Pair<String, String>>()
        val qnt = attachedData.readInt()
        repeat(qnt) {
            val first = attachedData.readString(32767)
            val second = attachedData.readString(32767)
            list.add(Pair(first, second))
        }
        server.execute {
            EntangledTankState.SERVER_PLAYER_REQUESTS[player] = list
        }
    }

}

fun initPacketsClient() {
    ClientPlayNetworking.registerGlobalReceiver(SYNCHRONIZE_LAST_RECIPE_PACKET) { client, _, buf, _ ->
        val id = buf.readIdentifier()
        val recipe = VACUUM_HOPPER_RECIPE_SERIALIZER.read(id, buf)
        client.execute {
            val screen = client.currentScreen as? VacuumHopperScreen
            screen?.screenHandler?.lastRecipe = recipe
        }
    }

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

fun initExtras() {
    AutoConfig.register(ModConfig::class.java) { cfg: Config, cls: Class<ModConfig> -> JanksonConfigSerializer(cfg, cls) }
    MOD_CONFIG = AutoConfig.getConfigHolder(ModConfig::class.java).config
    ServerLifecycleEvents.SERVER_STARTED.register { server ->
        server.overworld.persistentStateManager.getOrCreate({ ChunkLoaderState(server, "kibe_chunk_loaders") }, "kibe_chunk_loaders")
    }
    ServerTickEvents.END_SERVER_TICK.register { server ->
        EntangledTankState.SERVER_PLAYER_REQUESTS.forEach { (player, requests) ->
            val finalMap = mutableMapOf<String, MutableMap<String, FluidVolume>>()
            requests.forEach {
                val key = it.first
                val state = server.overworld.persistentStateManager.getOrCreate( { EntangledTankState(server.overworld, key) }, key)

                val colorCode = it.second
                val fluidVolume = state.getOrCreateInventory(colorCode).getInvFluid(0)

                val secondMap = finalMap[key] ?: mutableMapOf()
                secondMap[colorCode] = fluidVolume
                finalMap[key] = secondMap
            }
            val passedData = PacketByteBuf(Unpooled.buffer())
            passedData.writeInt(finalMap.size)
            finalMap.forEach { (key, secondMap) ->
                passedData.writeString(key, 32767)
                passedData.writeInt(secondMap.size)
                secondMap.forEach { (colorCode, fluidVolume) ->
                    passedData.writeString(colorCode, 32767)
                    fluidVolume.toMcBuffer(passedData)
                }
            }
            ServerPlayNetworking.send(player, SYNCHRONIZE_DIRTY_TANK_STATES, passedData)
        }
    }
    FluidContainerRegistry.mapContainer(Items.GLASS_BOTTLE, Items.EXPERIENCE_BOTTLE, LIQUID_XP.key.withAmount(FluidAmount.BOTTLE))
    FluidContainerRegistry.mapContainer(WOODEN_BUCKET, WATER_WOODEN_BUCKET, FluidKeys.WATER.withAmount(FluidAmount.BUCKET))
}

fun initExtrasClient() {
    @Suppress("deprecated")
    ClientTickEvents.END_CLIENT_TICK.register { client ->
        client.player?.let { player ->
            val list = EntangledTankState.CLIENT_PLAYER_REQUESTS[player]
            list?.size?.let { qnt ->
                if(qnt > 0) {
                    val passedData = PacketByteBuf(Unpooled.buffer())
                    passedData.writeInt(qnt)
                    list.forEach {
                        passedData.writeString(it.first)
                        passedData.writeString(it.second)
                    }
                    ClientPlayNetworking.send(REQUEST_DIRTY_TANK_STATES, passedData)
                }
            }

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

fun initLootTables() {
    //Add cursed droplets drop to mobs with the cursed effect
    LootTableLoadingCallback.EVENT.register(LootTableLoadingCallback { _, _, id: Identifier, supplier: FabricLootSupplierBuilder, _ ->
        if (id.toString().startsWith("minecraft:entities")) {
            val poolBuilder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(CURSED_DROPLETS))
                .conditionally(
                    EntityPropertiesLootCondition.builder(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.create().effects(EntityEffectPredicate.create().withEffect(CURSED_EFFECT))
                    )
                )
                .conditionally(RandomChanceLootCondition.builder(0.05F))
                .withFunction(LootingEnchantLootFunction.builder(UniformLootTableRange.between(0f, 1.5f)).build())
            supplier.pool(poolBuilder)
        }
    })
    //Add cursed droplets to wither skeletons
    LootTableLoadingCallback.EVENT.register(LootTableLoadingCallback { _, _, id: Identifier, supplier: FabricLootSupplierBuilder, _ ->
        if (id.toString() == "minecraft:entities/wither_skeleton") {
            val poolBuilder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(CURSED_DROPLETS))
                .conditionally(RandomChanceLootCondition.builder(0.1F))
                .withFunction(LootingEnchantLootFunction.builder(UniformLootTableRange.between(0f, 1.5f)).build())
            supplier.pool(poolBuilder)
        }
    })
}
