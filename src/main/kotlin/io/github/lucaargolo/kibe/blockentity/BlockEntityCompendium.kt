package io.github.lucaargolo.kibe.blockentity

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.client.blockentity.*
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.registry.Registries

object BlockEntityCompendium : RegistryCompendium<BlockEntityType<*>>(Registries.BLOCK_ENTITY_TYPE) {

    val REDSTONE_TIMER = registerBlockEntity("redstone_timer", ::RedstoneTimerEntity, BlockCompendium.REDSTONE_TIMER)
    val ENTANGLED_TANK = registerBlockEntity("entangled_tank", ::EntangledTankEntity, BlockCompendium.ENTANGLED_TANK)
    val ENTANGLED_CHEST = registerBlockEntity("entangled_chest", ::EntangledChestEntity, BlockCompendium.ENTANGLED_CHEST)
    val TRASH_CAN = registerBlockEntity("trash_can", ::TrashCanEntity, BlockCompendium.TRASH_CAN)
    val VACUUM_HOPPER = registerBlockEntity("vacuum_hopper", ::VacuumHopperEntity, BlockCompendium.VACUUM_HOPPER)
    val BIG_TORCH = registerBlockEntity("big_torch", ::BigTorchBlockEntity, BlockCompendium.BIG_TORCH)
    val COOLER = registerBlockEntity("cooler", ::CoolerBlockEntity, BlockCompendium.COOLER)
    val DRAWBRIDGE = registerBlockEntity("drawbridge", ::DrawbridgeBlockEntity, BlockCompendium.DRAWBRIDGE)
    val WITHER_BUILDER = registerBlockEntity("wither_builder", ::WitherBuilderBlockEntity, BlockCompendium.WITHER_BUILDER)
    val PLACER = registerBlockEntity("placer", ::PlacerBlockEntity, BlockCompendium.PLACER)
    val BREAKER = registerBlockEntity("breaker", ::BreakerBlockEntity, BlockCompendium.BREAKER)
    val HEATER = registerBlockEntity("heater", ::HeaterBlockEntity, BlockCompendium.HEATER)
    val DEHUMIDIFIER = registerBlockEntity("dehumidifier", ::DehumidifierBlockEntity, BlockCompendium.DEHUMIDIFIER)
    val BLOCK_GENERATOR = registerBlockEntity("block_generator", ::BlockGeneratorBlockEntity, *BlockCompendium.BLOCK_GENERATORS.values.toTypedArray())
    val CHUNK_LOADER = registerBlockEntity("chunk_loader", ::ChunkLoaderBlockEntity, BlockCompendium.CHUNK_LOADER)
    val TANK = registerBlockEntity("tank", ::TankBlockEntity, BlockCompendium.TANK)
    val XP_SHOWER = registerBlockEntity("xp_shower", ::XpShowerBlockEntity, BlockCompendium.XP_SHOWER)
    val FLUID_HOPPER = registerBlockEntity("fluid_hopper", ::FluidHopperBlockEntity, BlockCompendium.FLUID_HOPPER)

    fun <B: BlockEntity> registerBlockEntity(identifier: String, factory: BlockEntityType.BlockEntityFactory<B>, vararg blocks: Block): BlockEntityType<B> {
        return register(identifier, BlockEntityType.Builder.create(factory, *blocks).build(null))
    }

    override fun initializeClient() {
        super.initializeClient()
        BlockEntityRendererFactories.register(ENTANGLED_CHEST, ::EntangledChestEntityRenderer)
        BlockEntityRendererFactories.register(ENTANGLED_TANK, ::EntangledTankEntityRenderer)
        BlockEntityRendererFactories.register(VACUUM_HOPPER, ::VacuumHopperEntityRenderer)
        BlockEntityRendererFactories.register(REDSTONE_TIMER, ::RedstoneTimerEntityRenderer)
        BlockEntityRendererFactories.register(TANK, ::TankBlockEntityRenderer)
        EntangledChestEntityRenderer.helper.getEntries().forEach { (entityLayer, texturedModelData) ->
            EntityModelLayerRegistry.registerModelLayer(entityLayer) { texturedModelData }
        }
        EntangledTankEntityRenderer.helper.getEntries().forEach { (entityLayer, texturedModelData) ->
            EntityModelLayerRegistry.registerModelLayer(entityLayer) { texturedModelData }
        }
        RedstoneTimerEntityRenderer.selectorModelLayers.forEachIndexed{ index, entityModelLayer ->
            EntityModelLayerRegistry.registerModelLayer(entityModelLayer) { RedstoneTimerEntityRenderer.setupSelectorModel(index) }
        }
    }

}