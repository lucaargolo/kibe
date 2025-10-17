package io.github.lucaargolo.kibe.blockentity

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.client.blockentity.*
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.registry.Registries
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.ClientHooks
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

object BlockEntityCompendium : RegistryCompendium<BlockEntityType<*>>(Registries.BLOCK_ENTITY_TYPE) {

    val REDSTONE_TIMER by register("redstone_timer", ::RedstoneTimerEntity, { BlockCompendium.REDSTONE_TIMER })
    val ENTANGLED_TANK by register("entangled_tank", ::EntangledTankEntity, { BlockCompendium.ENTANGLED_TANK })
    val ENTANGLED_CHEST by register("entangled_chest", ::EntangledChestEntity, { BlockCompendium.ENTANGLED_CHEST })
    val TRASH_CAN by register("trash_can", ::TrashCanEntity, { BlockCompendium.TRASH_CAN })
    val VACUUM_HOPPER by register("vacuum_hopper", ::VacuumHopperEntity, { BlockCompendium.VACUUM_HOPPER })
    val BIG_TORCH by register("big_torch", ::BigTorchBlockEntity, { BlockCompendium.BIG_TORCH })
    val COOLER by register("cooler", ::CoolerBlockEntity, { BlockCompendium.COOLER })
    val DRAWBRIDGE by register("drawbridge", ::DrawbridgeBlockEntity, { BlockCompendium.DRAWBRIDGE })
    val WITHER_BUILDER by register("wither_builder", ::WitherBuilderBlockEntity, { BlockCompendium.WITHER_BUILDER })
    val PLACER by register("placer", ::PlacerBlockEntity, { BlockCompendium.PLACER })
    val BREAKER by register("breaker", ::BreakerBlockEntity, { BlockCompendium.BREAKER })
    val HEATER by register("heater", ::HeaterBlockEntity, { BlockCompendium.HEATER })
    val DEHUMIDIFIER by register("dehumidifier", ::DehumidifierBlockEntity, { BlockCompendium.DEHUMIDIFIER })
    val BLOCK_GENERATOR by register("block_generator", ::BlockGeneratorBlockEntity, *BlockCompendium.BLOCK_GENERATORS.values.map { Supplier{ it.value } }.toTypedArray())
    val CHUNK_LOADER by register("chunk_loader", ::ChunkLoaderBlockEntity, { BlockCompendium.CHUNK_LOADER })
    val TANK by register("tank", ::TankBlockEntity, { BlockCompendium.TANK })
    val XP_SHOWER by register("xp_shower", ::XpShowerBlockEntity, { BlockCompendium.XP_SHOWER })
    val FLUID_HOPPER by register("fluid_hopper", ::FluidHopperBlockEntity, { BlockCompendium.FLUID_HOPPER })

    fun <B: BlockEntity> register(string: String, factory: BlockEntityType.BlockEntityFactory<B>, vararg blocks: Supplier<Block>): Lazy<BlockEntityType<B>> {
        return register(string) {
            BlockEntityType.Builder.create(factory, *blocks.map { it.get() }.toTypedArray()).build(null)
        }
    }

    override fun initializeClient() {
        super.initializeClient()
        EntangledChestEntityRenderer.helper.getEntries().forEach { (entityLayer, texturedModelData) ->
            ClientHooks.registerLayerDefinition(entityLayer) { texturedModelData }
        }
        EntangledTankEntityRenderer.helper.getEntries().forEach { (entityLayer, texturedModelData) ->
            ClientHooks.registerLayerDefinition(entityLayer) { texturedModelData }
        }
        RedstoneTimerEntityRenderer.selectorModelLayers.forEachIndexed{ index, entityModelLayer ->
            ClientHooks.registerLayerDefinition(entityModelLayer) { RedstoneTimerEntityRenderer.setupSelectorModel(index) }
        }
        MOD_BUS.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        BlockEntityRendererFactories.register(ENTANGLED_CHEST, ::EntangledChestEntityRenderer)
        BlockEntityRendererFactories.register(ENTANGLED_TANK, ::EntangledTankEntityRenderer)
        BlockEntityRendererFactories.register(VACUUM_HOPPER, ::VacuumHopperEntityRenderer)
        BlockEntityRendererFactories.register(REDSTONE_TIMER, ::RedstoneTimerEntityRenderer)
        BlockEntityRendererFactories.register(TANK, ::TankBlockEntityRenderer)
    }

}