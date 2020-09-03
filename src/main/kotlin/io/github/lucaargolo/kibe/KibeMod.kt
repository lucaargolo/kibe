@file:Suppress("unused")

package io.github.lucaargolo.kibe

import io.github.lucaargolo.kibe.blocks.*
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderBlockEntity
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderState
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import io.github.lucaargolo.kibe.blocks.tank.TankCustomModel
import io.github.lucaargolo.kibe.blocks.vacuum.VacuumHopperScreen
import io.github.lucaargolo.kibe.effects.CURSED_EFFECT
import io.github.lucaargolo.kibe.effects.initEffects
import io.github.lucaargolo.kibe.fluids.initFluids
import io.github.lucaargolo.kibe.fluids.initFluidsClient
import io.github.lucaargolo.kibe.items.CURSED_DROPLETS
import io.github.lucaargolo.kibe.items.initItems
import io.github.lucaargolo.kibe.items.initItemsClient
import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_SERIALIZER
import io.github.lucaargolo.kibe.recipes.initRecipeSerializers
import io.github.lucaargolo.kibe.recipes.initRecipeTypes
import io.github.lucaargolo.kibe.utils.EntangledTankCache
import io.github.lucaargolo.kibe.utils.initCreativeTab
import io.github.lucaargolo.kibe.utils.initTooltip
import io.netty.buffer.Unpooled
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.event.server.ServerStartCallback
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.loader.launch.common.FabricLauncherBase
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.ModelIdentifier
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
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.WorldAccess
import java.util.*
import java.util.function.Consumer
import kotlin.collections.LinkedHashMap

const val MOD_ID = "kibe"
val FAKE_PLAYER_UUID: UUID = UUID.randomUUID()
val BIG_TORCH_MAP: LinkedHashMap<WorldAccess?, MutableList<BigTorchBlockEntity>> = linkedMapOf()
val CHUNK_MAP_CLICK = Identifier(MOD_ID, "chunk_map_click")
val MARK_ENTANGLED_TANK_DIRTY_S2C = Identifier(MOD_ID, "mark_entangled_tank_dirty")
val SYNC_ENTANGLED_TANK_S2C = Identifier(MOD_ID, "sync_entangled_tank")
val REQUEST_ENTANGLED_TANK_SYNC_C2S = Identifier(MOD_ID, "request_entangled_tank_sync")
val SYNCHRONIZE_LAST_RECIPE_PACKET = Identifier(MOD_ID, "synchronize_last_recipe")
val CLIENT = FabricLauncherBase.getLauncher().environmentType == EnvType.CLIENT
val TANK_CUSTOM_MODEL = TankCustomModel()

fun Boolean.toInt() = if (this) 1 else 0

fun init() {
    initRecipeSerializers()
    initRecipeTypes()
    initTooltip()
    initBlocks()
    initItems()
    initEffects()
    initLootTables()
    initFluids()
    initCreativeTab()
    initPackets()
    initExtras()
}

fun initClient() {
    initBlocksClient()
    initItemsClient()
    initFluidsClient()
    initExtrasClient()
    initPacketsClient()
}

fun initPackets() {
    ServerSidePacketRegistry.INSTANCE.register(CHUNK_MAP_CLICK) { packetContext: PacketContext, attachedData: PacketByteBuf ->
        val x = attachedData.readInt()
        val z = attachedData.readInt()
        val pos = attachedData.readBlockPos()
        if(x in (-2..2) || z in (-2..2)) {
            packetContext.taskQueue.execute {
                val world = packetContext.player.world
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
    ServerSidePacketRegistry.INSTANCE.register(REQUEST_ENTANGLED_TANK_SYNC_C2S) { packetContext: PacketContext, attachedData: PacketByteBuf ->
        val key = attachedData.readString(32767)
        val colorCode = attachedData.readString(32767)
        packetContext.taskQueue.execute {
            val player = packetContext.player as ServerPlayerEntity
            val serverWorld = player.serverWorld
            val state = serverWorld.server.overworld.persistentStateManager.getOrCreate( { EntangledTankState(serverWorld, key) }, key)
            val fluidInv = state.getOrCreateInventory(colorCode)
            val passedData = PacketByteBuf(Unpooled.buffer())
            passedData.writeString(key)
            passedData.writeString(colorCode)
            passedData.writeCompoundTag(fluidInv.toTag())
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, SYNC_ENTANGLED_TANK_S2C, passedData)
        }
    }
}

fun initPacketsClient() {
    ClientSidePacketRegistry.INSTANCE.register(SYNCHRONIZE_LAST_RECIPE_PACKET) { packetContext: PacketContext, attachedData: PacketByteBuf ->
        val id = attachedData.readIdentifier()
        val recipe = VACUUM_HOPPER_RECIPE_SERIALIZER.read(id, attachedData)
        packetContext.taskQueue.execute {
            if(MinecraftClient.getInstance().currentScreen is VacuumHopperScreen) {
                val screen = MinecraftClient.getInstance().currentScreen as VacuumHopperScreen
                screen.screenHandler.lastRecipe = recipe
            }
        }
    }
    ClientSidePacketRegistry.INSTANCE.register(MARK_ENTANGLED_TANK_DIRTY_S2C) { packetContext: PacketContext, attachedData: PacketByteBuf ->
        val key = attachedData.readString()
        val colorCode = attachedData.readString()
        packetContext.taskQueue.execute {
            EntangledTankCache.markDirty(key, colorCode)
        }
    }
    ClientSidePacketRegistry.INSTANCE.register(SYNC_ENTANGLED_TANK_S2C) { packetContext: PacketContext, attachedData: PacketByteBuf ->
        val key = attachedData.readString()
        val colorCode = attachedData.readString()
        val newFluidInvTag = attachedData.readCompoundTag()
        packetContext.taskQueue.execute {
            EntangledTankCache.updateClientFluidInv(key, colorCode, newFluidInvTag!!)
        }
    }
}

fun initExtras() {
    ServerStartCallback.EVENT.register(ServerStartCallback {  server ->
        server.overworld.persistentStateManager.getOrCreate({ ChunkLoaderState(server, "kibe_chunk_loaders") }, "kibe_chunk_loaders")
    })
}

fun initExtrasClient() {
    @Suppress("deprecated")
    ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(ClientSpriteRegistryCallback { _, registry ->
        registry.register(Identifier(MOD_ID, "block/entangled_chest_runes"))
        (0..15).forEach{
            registry.register(Identifier(MOD_ID, "block/redstone_timer_$it"))
        }
        registry.register(Identifier(MOD_ID, "block/tank"))
    })
    ModelLoadingRegistry.INSTANCE.registerAppender { _: ResourceManager?, out: Consumer<ModelIdentifier?> ->
        out.accept(ModelIdentifier(Identifier(MOD_ID, "glider"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "glider_handle"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "glider_active"), "inventory"))
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
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_background"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_ring"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_gold_core"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_diamond_core"), "inventory"))
    }
    BlockRenderLayerMap.INSTANCE.putBlock(VACUUM_HOPPER, RenderLayer.getTranslucent())
    BlockRenderLayerMap.INSTANCE.putBlock(BIG_TORCH, RenderLayer.getCutout())
    BlockRenderLayerMap.INSTANCE.putBlock(COOLER, RenderLayer.getTranslucent())
    BlockRenderLayerMap.INSTANCE.putBlock(ENTANGLED_TANK, RenderLayer.getTranslucent())
    ModelLoadingRegistry.INSTANCE.registerVariantProvider {
        ModelVariantProvider { modelIdentifier, _ ->
            if(modelIdentifier.namespace == MOD_ID && modelIdentifier.path == "tank" && modelIdentifier.variant != "inventory") {
                return@ModelVariantProvider TANK_CUSTOM_MODEL
            }
            return@ModelVariantProvider null
        }
    }
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
                    ))
                .conditionally(RandomChanceLootCondition.builder(0.05F))
                .withFunction(LootingEnchantLootFunction.builder(UniformLootTableRange.between(0f,1.5f)).build())
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
                .withFunction(LootingEnchantLootFunction.builder(UniformLootTableRange.between(0f,1.5f)).build())
            supplier.pool(poolBuilder)
        }
    })
}
