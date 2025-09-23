package io.github.lucaargolo.kibe.blockentity

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.client.blockentity.*
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.registry.Registries
import net.neoforged.neoforge.client.ClientHooks
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object BlockEntityCompendium : RegistryCompendium<BlockEntityType<*>>(Registries.BLOCK_ENTITY_TYPE) {

    val REDSTONE_TIMER by register("redstone_timer", { BlockEntityType.Builder.create(::RedstoneTimerEntity, BlockCompendium.REDSTONE_TIMER).build(null) })
    val ENTANGLED_TANK by register("entangled_tank", { BlockEntityType.Builder.create(::EntangledTankEntity, BlockCompendium.ENTANGLED_TANK).build(null) })
    val ENTANGLED_CHEST by register("entangled_chest", { BlockEntityType.Builder.create(::EntangledChestEntity, BlockCompendium.ENTANGLED_CHEST).build(null) })
    val TRASH_CAN by register("trash_can", { BlockEntityType.Builder.create(::TrashCanEntity, BlockCompendium.TRASH_CAN).build(null) })
    val VACUUM_HOPPER by register("vacuum_hopper", { BlockEntityType.Builder.create(::VacuumHopperEntity, BlockCompendium.VACUUM_HOPPER).build(null) })
    val BIG_TORCH by register("big_torch", { BlockEntityType.Builder.create(::BigTorchBlockEntity, BlockCompendium.BIG_TORCH).build(null) })
    val COOLER by register("cooler", { BlockEntityType.Builder.create(::CoolerBlockEntity, BlockCompendium.COOLER).build(null) })
    val DRAWBRIDGE by register("drawbridge", { BlockEntityType.Builder.create(::DrawbridgeBlockEntity, BlockCompendium.DRAWBRIDGE).build(null) })
    val WITHER_BUILDER by register("wither_builder", { BlockEntityType.Builder.create(::WitherBuilderBlockEntity, BlockCompendium.WITHER_BUILDER).build(null) })
    val PLACER by register("placer", { BlockEntityType.Builder.create(::PlacerBlockEntity, BlockCompendium.PLACER).build(null) })
    val BREAKER by register("breaker", { BlockEntityType.Builder.create(::BreakerBlockEntity, BlockCompendium.BREAKER).build(null) })
    val HEATER by register("heater", { BlockEntityType.Builder.create(::HeaterBlockEntity, BlockCompendium.HEATER).build(null) })
    val DEHUMIDIFIER by register("dehumidifier", { BlockEntityType.Builder.create(::DehumidifierBlockEntity, BlockCompendium.DEHUMIDIFIER).build(null) })
    val BLOCK_GENERATOR by register("block_generator", { BlockEntityType.Builder.create(::BlockGeneratorBlockEntity, *BlockCompendium.BLOCK_GENERATORS).build(null) })
    val CHUNK_LOADER by register("chunk_loader", { BlockEntityType.Builder.create(::ChunkLoaderBlockEntity, BlockCompendium.CHUNK_LOADER).build(null) })
    val TANK by register("tank", { BlockEntityType.Builder.create(::TankBlockEntity, BlockCompendium.TANK).build(null) })
    val XP_SHOWER by register("xp_shower", { BlockEntityType.Builder.create(::XpShowerBlockEntity, BlockCompendium.XP_SHOWER).build(null) })
    val FLUID_HOPPER by register("fluid_hopper", { BlockEntityType.Builder.create(::FluidHopperBlockEntity, BlockCompendium.FLUID_HOPPER).build(null) })

    override fun initializeClient() {
        super.initializeClient()
        BlockEntityRendererFactories.register(ENTANGLED_CHEST, ::EntangledChestEntityRenderer)
        BlockEntityRendererFactories.register(ENTANGLED_TANK, ::EntangledTankEntityRenderer)
        BlockEntityRendererFactories.register(VACUUM_HOPPER, ::VacuumHopperEntityRenderer)
        BlockEntityRendererFactories.register(REDSTONE_TIMER, ::RedstoneTimerEntityRenderer)
        BlockEntityRendererFactories.register(TANK, ::TankBlockEntityRenderer)

        EntangledChestEntityRenderer.helper.getEntries().forEach { (entityLayer, texturedModelData) ->
            ClientHooks.registerLayerDefinition(entityLayer) { texturedModelData }
        }
        EntangledTankEntityRenderer.helper.getEntries().forEach { (entityLayer, texturedModelData) ->
            ClientHooks.registerLayerDefinition(entityLayer) { texturedModelData }
        }
        RedstoneTimerEntityRenderer.selectorModelLayers.forEachIndexed{ index, entityModelLayer ->
            ClientHooks.registerLayerDefinition(entityModelLayer) { RedstoneTimerEntityRenderer.setupSelectorModel(index) }
        }
    }

}