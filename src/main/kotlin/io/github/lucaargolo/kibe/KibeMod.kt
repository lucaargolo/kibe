@file:Suppress("unused", "UnstableApiUsage", "DEPRECATION")

package io.github.lucaargolo.kibe

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderBlockEntity
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderState
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import io.github.lucaargolo.kibe.blocks.initBlocks
import io.github.lucaargolo.kibe.effects.CURSED_EFFECT
import io.github.lucaargolo.kibe.effects.initEffects
import io.github.lucaargolo.kibe.entities.initEntities
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import io.github.lucaargolo.kibe.fluids.initFluids
import io.github.lucaargolo.kibe.items.*
import io.github.lucaargolo.kibe.items.entangledbucket.EntangledBucket
import io.github.lucaargolo.kibe.items.miscellaneous.WoodenBucket
import io.github.lucaargolo.kibe.items.tank.TankBlockItem
import io.github.lucaargolo.kibe.mixin.PersistentStateManagerAccessor
import io.github.lucaargolo.kibe.recipes.initRecipeSerializers
import io.github.lucaargolo.kibe.recipes.initRecipeTypes
import io.github.lucaargolo.kibe.utils.ModConfig
import io.github.lucaargolo.kibe.utils.initCreativeTab
import io.github.lucaargolo.kibe.utils.initTooltip
import io.netty.buffer.Unpooled
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.loot.v2.FabricLootPoolBuilder
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.fluid.Fluids
import net.minecraft.item.ExperienceBottleItem
import net.minecraft.item.Items
import net.minecraft.loot.LootPool
import net.minecraft.loot.condition.EntityPropertiesLootCondition
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.LootingEnchantLootFunction
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.loot.provider.number.UniformLootNumberProvider
import net.minecraft.network.PacketByteBuf
import net.minecraft.particle.DefaultParticleType
import net.minecraft.predicate.entity.EntityEffectPredicate
import net.minecraft.predicate.entity.EntityPredicate
import net.minecraft.server.MinecraftServer
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.ChunkPos
import net.minecraft.registry.Registry
import net.minecraft.registry.Registries
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.util.*

const val MOD_ID = "kibe"
const val MOD_NAME = "Kibe"
val FAKE_PLAYER_UUID: UUID = UUID.randomUUID()
val CHUNK_PLAYER_CHECK = Identifier(MOD_ID, "chunk_player_check")
val CHUNK_MAP_CLICK = Identifier(MOD_ID, "chunk_map_click")
val REQUEST_DIRTY_TANK_STATES = Identifier(MOD_ID, "request_dirty_tank_states")
val SYNCHRONIZE_DIRTY_TANK_STATES = Identifier(MOD_ID, "synchronize_dirty_tank_states")
val CLIENT: Boolean by lazy { FabricLoader.getInstance().environmentType == EnvType.CLIENT }
val TRINKET: Boolean by lazy { FabricLoader.getInstance().isModLoaded("trinkets") }
val WATER_DROPS: DefaultParticleType by lazy {
    Registry.register(Registries.PARTICLE_TYPE, Identifier(MOD_ID, "water_drops"), FabricParticleTypes.simple())
}
val LOGGER: Logger = LogManager.getLogger("Kibe")
val MOD_CONFIG: ModConfig by lazy {
    val parser = JsonParser()
    val gson = GsonBuilder().setPrettyPrinting().create()
    val configFile = File("${FabricLoader.getInstance().configDir}${File.separator}$MOD_ID.json")
    var finalConfig: ModConfig
    LOGGER.info("[$MOD_NAME] Trying to read config file...")
    try {
        if (configFile.createNewFile()) {
            LOGGER.info("[$MOD_NAME] No config file found, creating a new one...")
            val json: String = gson.toJson(parser.parse(gson.toJson(ModConfig())))
            PrintWriter(configFile).use { out -> out.println(json) }
            finalConfig = ModConfig()
            LOGGER.info("[$MOD_NAME] Successfully created default config file.")
        } else {
            LOGGER.info("[$MOD_NAME] A config file was found, loading it..")
            finalConfig = gson.fromJson(String(Files.readAllBytes(configFile.toPath())), ModConfig::class.java)
            if (finalConfig == null) {
                throw NullPointerException("[$MOD_NAME] The config file was empty.")
            } else {
                LOGGER.info("[$MOD_NAME] Successfully loaded config file.")
            }
        }
    } catch (exception: Exception) {
        LOGGER.error("[$MOD_NAME] There was an error creating/loading the config file!", exception)
        finalConfig = ModConfig()
        LOGGER.warn("[$MOD_NAME] Defaulting to original config.")
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
}

fun initPackets() {
    ServerPlayNetworking.registerGlobalReceiver(CHUNK_PLAYER_CHECK) { server, player, _, attachedData, _ ->
        val pos = attachedData.readBlockPos()
        server.execute {
            val world = player.world
            val be = world.getBlockEntity(pos) as? ChunkLoaderBlockEntity
            be?.let {
                if(player.uuidAsString == it.ownerUUID) {
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
                    be.markDirtyAndSync()
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
    ServerLifecycleEvents.SERVER_STARTED.register { server ->
        server.overworld.persistentStateManager.getOrCreate({ChunkLoaderState.createFromTag(it, server)}, { ChunkLoaderState(server) }, "kibe_chunk_loaders")
    }
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

                val state = server.overworld.persistentStateManager.getOrCreate( { EntangledTankState.createFromTag(it, server.overworld, key) }, { EntangledTankState(server.overworld, key)}, key)
                val allTimeRequests = EntangledTankState.ALL_TIME_PLAYER_REQUESTS.getOrPut(player) { linkedSetOf() }
                if(!allTimeRequests.contains(pair) || state.dirtyColors.contains(colorCode)) {
                    allTimeRequests.add(pair)
                    val fluidVolume = state.getOrCreateInventory(colorCode).let { Pair(it.variant, it.amount) }

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
                        fluidVolume.first.toPacket(passedData)
                        passedData.writeLong(fluidVolume.second)
                    }
                }
                ServerPlayNetworking.send(player, SYNCHRONIZE_DIRTY_TANK_STATES, passedData)
            }
        }
        (server.overworld.persistentStateManager as? PersistentStateManagerAccessor)?.loadedStates?.forEach { (_, state) ->
            (state as? EntangledTankState)?.dirtyColors?.clear()
        }
    }
    FluidStorage.combinedItemApiProvider(WOODEN_BUCKET).register {
        EmptyItemFluidStorage(it, WATER_WOODEN_BUCKET, Fluids.WATER, FluidConstants.BUCKET)
    }
    FluidStorage.GENERAL_COMBINED_PROVIDER.register { context ->
        (context.itemVariant.item as? WoodenBucket)?.let { bucketItem ->
            val bucketFluid = Fluids.WATER
            if (bucketItem == WATER_WOODEN_BUCKET) {
                return@register FullItemFluidStorage(context, WOODEN_BUCKET, FluidVariant.of(bucketFluid), FluidConstants.BUCKET)
            }
        }
        return@register null
    }
    FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register {
        EmptyItemFluidStorage(it, Items.EXPERIENCE_BOTTLE, LIQUID_XP, FluidConstants.BOTTLE)
    }
    FluidStorage.GENERAL_COMBINED_PROVIDER.register { context ->
        (context.itemVariant.item as? ExperienceBottleItem)?.let { bottleItem ->
            val bottleFluid = LIQUID_XP
            if (bottleItem == Items.EXPERIENCE_BOTTLE) {
                return@register FullItemFluidStorage(context, Items.GLASS_BOTTLE, FluidVariant.of(bottleFluid), FluidConstants.BOTTLE)
            }
        }
        return@register null
    }
    FluidStorage.ITEM.registerForItems( {stack, context -> TankBlockItem.getFluidStorage(stack, context) }, TANK)
    FluidStorage.ITEM.registerForItems( {stack, _ ->
        var tag = EntangledBucket.getTag(stack)
        if(tag.contains("BlockEntityTag")) {
            tag = tag.getCompound("BlockEntityTag")
        }
        var colorCode = ""
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE
            colorCode += dc.id.let { int -> Integer.toHexString(int) }
        }
        tag.putString("colorCode", colorCode)

        FabricLoader.getInstance().gameInstance.let {
            if(CLIENT && it is MinecraftClient) {
                if(it.isOnThread) {
                    EntangledBucket.getFluidInv(null, tag)
                }else if(it.isIntegratedServerRunning && it.server?.isOnThread == true) {
                    EntangledBucket.getFluidInv(it.server?.overworld, tag)
                }else{
                    null
                }
            }else if(it is MinecraftServer) {
                EntangledBucket.getFluidInv(it.overworld, tag)
            }else{
                null
            }
        } ?: object: SingleVariantStorage<FluidVariant>() {
            override fun getCapacity(variant: FluidVariant?) = 0L
            override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
        }
    }, ENTANGLED_TANK, ENTANGLED_BUCKET)


}

fun initLootTables() {
    //Add cursed droplets drop to mobs with the cursed effect
    LootTableEvents.MODIFY.register { _, _, id, supplier, _ ->
        if (id.toString().startsWith("minecraft:entities")) {
            val poolBuilder = LootPool.Builder()
                .rolls(ConstantLootNumberProvider.create(1f))
                .with(ItemEntry.builder(CURSED_DROPLETS))
                .conditionally(
                    EntityPropertiesLootCondition.builder(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.create().effects(EntityEffectPredicate.create().withEffect(CURSED_EFFECT))
                    )
                )
                .conditionally(RandomChanceLootCondition.builder(0.05F))
                .apply(LootingEnchantLootFunction.builder(UniformLootNumberProvider.create(0f, 1.5f)).build())
            supplier.pool(poolBuilder)
        }
    }
    //Add cursed droplets to wither skeletons
    LootTableEvents.MODIFY.register { _, _, id, supplier, _ ->
        if (id.toString() == "minecraft:entities/wither_skeleton") {
            val poolBuilder = LootPool.Builder()
                .rolls(ConstantLootNumberProvider.create(1f))
                .with(ItemEntry.builder(CURSED_DROPLETS))
                .conditionally(RandomChanceLootCondition.builder(0.1F))
                .apply(LootingEnchantLootFunction.builder(UniformLootNumberProvider.create(0f, 1.5f)).build())
            supplier.pool(poolBuilder)
        }
    }
}