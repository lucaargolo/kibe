package io.github.lucaargolo.kibe

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.blockentity.BlockEntityCompendium
import io.github.lucaargolo.kibe.client.KibeModClient
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import io.github.lucaargolo.kibe.data.state.ChunkLoaderState
import io.github.lucaargolo.kibe.effect.EffectCompendium
import io.github.lucaargolo.kibe.entity.EntityCompendium
import io.github.lucaargolo.kibe.fluid.FluidCompendium
import io.github.lucaargolo.kibe.item.ArmorMaterialCompendium
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.menu.ScreenHandlerCompendium
import io.github.lucaargolo.kibe.network.PacketCompendium
import io.github.lucaargolo.kibe.particle.ParticleCompendium
import io.github.lucaargolo.kibe.recipes.RecipeSerializerCompendium
import io.github.lucaargolo.kibe.recipes.RecipeTypeCompendium
import io.github.lucaargolo.kibe.utils.CreativeTab
import io.github.lucaargolo.kibe.utils.EntangledTankSync
import io.github.lucaargolo.kibe.utils.ModConfig
import io.github.lucaargolo.kibe.utils.helper.LootHelper
import io.github.lucaargolo.kibe.utils.helper.TooltipHelper
import io.github.lucaargolo.kibe.utils.helper.TransferHelper
import io.netty.buffer.ByteBuf
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.codec.PacketCodec
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runForDist
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.util.*

@Mod(KibeMod.MOD_ID)
object KibeMod {

    const val MOD_ID = "kibe"
    const val MOD_NAME = "Kibe"
    val FAKE_PLAYER_UUID: UUID = UUID.randomUUID()

    val LONG_CODEC: PacketCodec<ByteBuf, Long> = object : PacketCodec<ByteBuf, Long> {
        override fun decode(byteBuf: ByteBuf): Long {
            return byteBuf.readLong()
        }

        override fun encode(byteBuf: ByteBuf, long: Long) {
            byteBuf.writeLong(long)
        }
    }

    val CLIENT: Boolean by lazy { FabricLoader.getInstance().environmentType == EnvType.CLIENT }
    val TRINKET: Boolean by lazy { FabricLoader.getInstance().isModLoaded("curios") }

    val LOGGER: Logger = LogManager.getLogger("Kibe")

    val CONFIG: ModConfig by lazy {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val configFile = File("${FabricLoader.getInstance().configDir}${File.separator}$MOD_ID.json")
        var finalConfig: ModConfig
        LOGGER.info("[$MOD_NAME] Trying to read config file...")
        try {
            if (configFile.createNewFile()) {
                LOGGER.info("[$MOD_NAME] No config file found, creating a new one...")
                val json: String = gson.toJson(JsonParser.parseString(gson.toJson(ModConfig())))
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

    init {
        MOD_BUS.addListener(::onCommonSetup)
        ModConfig.initialize()
        CreativeTab.initialize()
        RecipeSerializerCompendium.initialize()
        RecipeTypeCompendium.initialize()
        ArmorMaterialCompendium.initialize()
        FluidCompendium.initialize()
        BlockCompendium.initialize()
        ItemCompendium.initialize()
        ComponentTypeCompendium.initialize()
        BlockEntityCompendium.initialize()
        ScreenHandlerCompendium.initialize()
        EntityCompendium.initialize()
        EffectCompendium.initialize()
        ParticleCompendium.initialize()
        PacketCompendium.initialize()
        EntangledTankSync.initialize()
        initChunkLoaderData()
        runForDist(clientTarget = { KibeModClient }, serverTarget = { KibeMod })
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        TransferHelper.initialize()
        TooltipHelper.initialize()
        LootHelper.initialize()
    }


    fun initChunkLoaderData() {
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            ChunkLoaderState.getPersistentState(server)
        }
    }

}