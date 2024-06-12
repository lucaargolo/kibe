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

    val REDSTONE_TIMER = register("redstone_timer", ::RedstoneTimerEntity, BlockCompendium.REDSTONE_TIMER)
    val ENTANGLED_TANK = register("entangled_tank", ::EntangledTankEntity, BlockCompendium.ENTANGLED_TANK)
    val ENTANGLED_CHEST = register("entangled_chest", ::EntangledChestEntity, BlockCompendium.ENTANGLED_CHEST)
    val TRASH_CAN = register("trash_can", ::TrashCanEntity, BlockCompendium.TRASH_CAN)
    val VACUUM_HOPPER = register("vacuum_hopper", ::VacuumHopperEntity, BlockCompendium.VACUUM_HOPPER)
    val BIG_TORCH = register("big_torch", ::BigTorchBlockEntity, BlockCompendium.BIG_TORCH)
    val COOLER = register("cooler", ::CoolerBlockEntity, BlockCompendium.COOLER)
    val DRAWBRIDGE = register("drawbridge", ::DrawbridgeBlockEntity, BlockCompendium.DRAWBRIDGE)
    val WITHER_BUILDER = register("wither_builder", ::WitherBuilderBlockEntity, BlockCompendium.WITHER_BUILDER)
    val PLACER = register("placer", ::PlacerBlockEntity, BlockCompendium.PLACER)
    val BREAKER = register("breaker", ::BreakerBlockEntity, BlockCompendium.BREAKER)
    val HEATER = register("heater", ::HeaterBlockEntity, BlockCompendium.HEATER)
    val DEHUMIDIFIER = register("dehumidifier", ::DehumidifierBlockEntity, BlockCompendium.DEHUMIDIFIER)
    val BLOCK_GENERATOR = register("block_generator", ::BlockGeneratorBlockEntity, *BlockCompendium.BLOCK_GENERATORS)
    val CHUNK_LOADER = register("chunk_loader", ::ChunkLoaderBlockEntity, BlockCompendium.CHUNK_LOADER)
    val TANK = register("tank", ::TankBlockEntity, BlockCompendium.TANK)
    val XP_SHOWER = register("xp_shower", ::XpShowerBlockEntity, BlockCompendium.XP_SHOWER)
    val FLUID_HOPPER = register("fluid_hopper", ::FluidHopperBlockEntity, BlockCompendium.FLUID_HOPPER)

    fun <B: BlockEntity> register(identifier: String, factory: BlockEntityType.BlockEntityFactory<B>, vararg blocks: Block): BlockEntityType<B> {
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