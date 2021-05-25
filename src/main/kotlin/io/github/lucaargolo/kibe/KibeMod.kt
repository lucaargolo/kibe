@file:Suppress("unused")

package io.github.lucaargolo.kibe

import alexiil.mc.lib.attributes.fluid.FluidContainerRegistry
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.lucaargolo.kibe.blocks.*
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderBlockEntity
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderState
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import io.github.lucaargolo.kibe.compat.initTrinketsCompat
import io.github.lucaargolo.kibe.effects.CURSED_EFFECT
import io.github.lucaargolo.kibe.effects.initEffects
import io.github.lucaargolo.kibe.entities.initEntities
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import io.github.lucaargolo.kibe.fluids.initFluids
import io.github.lucaargolo.kibe.items.*
import io.github.lucaargolo.kibe.mixin.PersistentStateManagerAccessor
import io.github.lucaargolo.kibe.recipes.initRecipeSerializers
import io.github.lucaargolo.kibe.recipes.initRecipeTypes
import io.github.lucaargolo.kibe.utils.ModConfig
import io.github.lucaargolo.kibe.utils.initCreativeTab
import io.github.lucaargolo.kibe.utils.initTooltip
import io.netty.buffer.Unpooled
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.launch.common.FabricLauncherBase
import net.minecraft.client.particle.FlameParticle
import net.minecraft.item.Items
import net.minecraft.loot.ConstantLootTableRange
import net.minecraft.loot.UniformLootTableRange
import net.minecraft.loot.condition.EntityPropertiesLootCondition
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.LootingEnchantLootFunction
import net.minecraft.network.PacketByteBuf
import net.minecraft.particle.DefaultParticleType
import net.minecraft.particle.ParticleType
import net.minecraft.predicate.entity.EntityEffectPredicate
import net.minecraft.predicate.entity.EntityPredicate
import net.minecraft.util.Identifier
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.util.*

const val MOD_ID = "kibe"
val FAKE_PLAYER_UUID: UUID = UUID.randomUUID()
val CHUNK_MAP_CLICK = Identifier(MOD_ID, "chunk_map_click")
val REQUEST_DIRTY_TANK_STATES = Identifier(MOD_ID, "request_dirty_tank_states")
val SYNCHRONIZE_DIRTY_TANK_STATES = Identifier(MOD_ID, "synchronize_dirty_tank_states")
val CLIENT = FabricLauncherBase.getLauncher().environmentType == EnvType.CLIENT
val TRINKET = FabricLauncherBase.getLauncher().isClassLoaded("dev.emi.trinkets.api.Trinket")
val WATER_DROPS: DefaultParticleType by lazy {
    Registry.register(Registry.PARTICLE_TYPE, Identifier(MOD_ID, "water_drops"), FabricParticleTypes.simple())
}
val LOGGER: Logger = LogManager.getLogger("Kibe")
val MOD_CONFIG: ModConfig by lazy {
    val parser = JsonParser()
    val gson = GsonBuilder().setPrettyPrinting().create()
    val configFile = File("${FabricLoader.getInstance().configDir}${File.separator}$MOD_ID.json")
    var finalConfig: ModConfig
    LOGGER.info("Trying to read config file...")
    try {
        if (configFile.createNewFile()) {
            LOGGER.info("No config file found, creating a new one...")
            val json: String = gson.toJson(parser.parse(gson.toJson(ModConfig())))
            PrintWriter(configFile).use { out -> out.println(json) }
            finalConfig = ModConfig()
            LOGGER.info("Successfully created default config file.")
        } else {
            LOGGER.info("A config file was found, loading it..")
            finalConfig = gson.fromJson(String(Files.readAllBytes(configFile.toPath())), ModConfig::class.java)
            if (finalConfig == null) {
                throw NullPointerException("The config file was empty.")
            } else {
                LOGGER.info("Successfully loaded config file.")
            }
        }
    } catch (exception: Exception) {
        LOGGER.error("There was an error creating/loading the config file!", exception)
        finalConfig = ModConfig()
        LOGGER.warn("Defaulting to original config.")
    }
    finalConfig
}

fun Boolean.toInt() = if (this) 1 else 0

fun init() {
    initCreativeTab()
    initRecipeSerializers()
    initRecipeTypes()
    initTooltip()
    initBlocks()
    initItems()
    initEntities()
    initEffects()
    initLootTables()
    initFluids()
    initPackets()
    initExtras()
    if(TRINKET) {
        initTrinketsCompat()
    }
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

fun initExtras() {
    MOD_CONFIG.toString() //Used to init mod config here.
    ParticleFactoryRegistry.getInstance().register(WATER_DROPS) { sprite -> FlameParticle.Factory(sprite) }
    ServerLifecycleEvents.SERVER_STARTED.register { server ->
        server.overworld.persistentStateManager.getOrCreate({ ChunkLoaderState(server, "kibe_chunk_loaders") }, "kibe_chunk_loaders")
    }
    ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
        EntangledTankState.ALL_TIME_PLAYER_REQUESTS.remove(handler.player)
        EntangledTankState.SERVER_PLAYER_REQUESTS.remove(handler.player)
    }
    ServerTickEvents.END_SERVER_TICK.register { server ->
        EntangledTankState.SERVER_PLAYER_REQUESTS.forEach { (player, requests) ->
            val finalMap = mutableMapOf<String, MutableMap<String, FluidVolume>>()
            requests.forEach {
                val key = it.first
                val colorCode = it.second

                val state = server.overworld.persistentStateManager.getOrCreate( { EntangledTankState(server.overworld, key) }, key)
                val allTimeRequests = EntangledTankState.ALL_TIME_PLAYER_REQUESTS.getOrPut(player) { linkedSetOf() }
                if(!allTimeRequests.contains(it) || state.dirtyColors.contains(colorCode)) {
                    allTimeRequests.add(it)
                    val fluidVolume = state.getOrCreateInventory(colorCode).getInvFluid(0)

                    val secondMap = finalMap[key] ?: mutableMapOf()
                    secondMap[colorCode] = fluidVolume
                    finalMap[key] = secondMap
                }
            }
            if(finalMap.isNotEmpty()) {
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
        (server.overworld.persistentStateManager as? PersistentStateManagerAccessor)?.loadedStates?.forEach { (_, state) ->
            (state as? EntangledTankState)?.dirtyColors?.clear()
        }
    }
    FluidContainerRegistry.mapContainer(Items.GLASS_BOTTLE, Items.EXPERIENCE_BOTTLE, LIQUID_XP.key.withAmount(FluidAmount.BOTTLE))
    FluidContainerRegistry.mapContainer(WOODEN_BUCKET, WATER_WOODEN_BUCKET, FluidKeys.WATER.withAmount(FluidAmount.BUCKET))
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
