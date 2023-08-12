@file:Suppress("UNCHECKED_CAST", "DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.kibe.blocks

import io.github.lucaargolo.kibe.CLIENT
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorch
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchScreen
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchScreenHandler
import io.github.lucaargolo.kibe.blocks.breaker.Breaker
import io.github.lucaargolo.kibe.blocks.breaker.BreakerBlockEntity
import io.github.lucaargolo.kibe.blocks.breaker.BreakerScreen
import io.github.lucaargolo.kibe.blocks.breaker.BreakerScreenHandler
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoader
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderBlockEntity
import io.github.lucaargolo.kibe.blocks.cooler.Cooler
import io.github.lucaargolo.kibe.blocks.cooler.CoolerBlockEntity
import io.github.lucaargolo.kibe.blocks.cooler.CoolerScreen
import io.github.lucaargolo.kibe.blocks.cooler.CoolerScreenHandler
import io.github.lucaargolo.kibe.blocks.drawbridge.Drawbridge
import io.github.lucaargolo.kibe.blocks.drawbridge.DrawbridgeBlockEntity
import io.github.lucaargolo.kibe.blocks.drawbridge.DrawbridgeScreen
import io.github.lucaargolo.kibe.blocks.drawbridge.DrawbridgeScreenHandler
import io.github.lucaargolo.kibe.blocks.entangledchest.*
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTank
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntity
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntityRenderer
import io.github.lucaargolo.kibe.blocks.miscellaneous.*
import io.github.lucaargolo.kibe.blocks.placer.Placer
import io.github.lucaargolo.kibe.blocks.placer.PlacerBlockEntity
import io.github.lucaargolo.kibe.blocks.placer.PlacerScreen
import io.github.lucaargolo.kibe.blocks.placer.PlacerScreenHandler
import io.github.lucaargolo.kibe.blocks.tank.Tank
import io.github.lucaargolo.kibe.blocks.tank.TankBlockEntity
import io.github.lucaargolo.kibe.blocks.tank.TankBlockEntityRenderer
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCan
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCanEntity
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCanScreen
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCanScreenHandler
import io.github.lucaargolo.kibe.blocks.vacuum.*
import io.github.lucaargolo.kibe.blocks.witherbuilder.WitherBuilder
import io.github.lucaargolo.kibe.blocks.witherbuilder.WitherBuilderBlockEntity
import io.github.lucaargolo.kibe.blocks.witherbuilder.WitherBuilderScreen
import io.github.lucaargolo.kibe.blocks.witherbuilder.WitherBuilderScreenHandler
import io.github.lucaargolo.kibe.utils.CREATIVE_TAB
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier
import kotlin.reflect.KClass

class ContainerInfo<T: ScreenHandler>(
    handlerClass: KClass<*>,
    screenClass: Supplier<KClass<*>>,
    val identifier: Identifier? = null
){

    val handlerClass = handlerClass as KClass<T>
    val screenClass = screenClass as Supplier<KClass<HandledScreen<T>>>

    var handlerType: ScreenHandlerType<T>? = null
    var handler: T? = null

    var title: Text = Text.literal("")

    fun init(blockIdentifier: Identifier) {
        val id = identifier ?: blockIdentifier
        title = Text.translatable("screen.$MOD_ID.${id.path}")
        handlerType = ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
            val pos = packetByteBuf.readBlockPos()
            val player = playerInventory.player
            val world = player.world
            val be = world.getBlockEntity(pos)
            handler = handlerClass.java.constructors[0].newInstance(i, playerInventory, be, ScreenHandlerContext.create(world, pos)) as T
            handler
        }
        Registry.register(Registry.SCREEN_HANDLER, id, handlerType)
    }

    fun initClient() {
        HandledScreens.register(handlerType) { handler, playerInventory, title -> screenClass.get().java.constructors[0].newInstance(handler, playerInventory, title) as HandledScreen<T>  }
    }

}

class BlockInfo<T: BlockEntity> (
    val identifier: Identifier,
    private val block: Block,
    private val hasBlockItem: Boolean,
    private val blockItem: KClass<BlockItem>?,
    var entity: BlockEntityType<T>?,
    var renderer: KClass<BlockEntityRenderer<T>>?,
    var containers: List<ContainerInfo<*>>
){

    fun init() {
        Registry.register(Registry.BLOCK, identifier, block)
        if(hasBlockItem) {
            if(blockItem != null)
                Registry.register(Registry.ITEM, identifier, blockItem.java.constructors[0].newInstance(block, Item.Settings().group(CREATIVE_TAB)) as BlockItem)
            else
                Registry.register(Registry.ITEM, identifier, BlockItem(block, Item.Settings().group(CREATIVE_TAB)))
        }
        if(entity != null) Registry.register(Registry.BLOCK_ENTITY_TYPE, identifier, entity)
        containers.forEach { it.init(identifier) }
    }

    fun initClient() {
        containers.forEach { it.initClient() }
        if(renderer != null) {
            BlockEntityRendererRegistry.register(entity) { it2 ->
                renderer!!.java.constructors[0].newInstance(it2) as BlockEntityRenderer<T>
            }
        }
    }

}

val blockRegistry = linkedMapOf<Block, BlockInfo<*>>()

fun getBlockId(block: Block) = blockRegistry[block]?.identifier
fun getEntityType(block: Block) = blockRegistry[block]?.entity as BlockEntityType<BlockEntity>
fun getContainerInfo(block: Block) = blockRegistry[block]?.containers?.get(0)
fun getContainerInfo(block: Block, identifier: Identifier): ContainerInfo<*>? {
    blockRegistry[block]?.containers?.forEach {
        if(it.identifier == identifier)
            return it
    }
    return null
}

fun register(identifier: Identifier, block: Block, hasModBlock: Boolean = true): Block {
    val info = BlockInfo<BlockEntity>(identifier, block, hasModBlock, null, null, null, listOf())
    blockRegistry[block] = info
    return block
}

fun <T : BlockEntity> registerWithEntity(identifier: Identifier, block: Block, hasBlockItem: Boolean = true, blockItem: KClass<*>? = null, renderer: Supplier<KClass<*>>? = null, containers: List<ContainerInfo<*>> = listOf(), apiRegistrations: (BlockEntityType<T>) -> Unit = {}): Block {
    val bli = blockItem as? KClass<BlockItem>
    val ent = (block as? BlockEntityProvider)?.let { BlockEntityType.Builder.create({ blockPos, blockState -> block.createBlockEntity(blockPos, blockState) } , block).build(null) as BlockEntityType<T> }
    ent?.let { apiRegistrations(it) }
    val rnd = if(CLIENT) renderer?.let { it.get() as KClass<BlockEntityRenderer<T>> } else null
    val info = BlockInfo(identifier, block, hasBlockItem, bli, ent, rnd, containers)
    blockRegistry[block] = info
    return block
}

val CURSED_DIRT = register(Identifier(MOD_ID, "cursed_dirt"), CursedDirt())
val REDSTONE_TIMER = registerWithEntity<RedstoneTimerEntity>(Identifier(MOD_ID, "redstone_timer"), RedstoneTimer(), renderer = { RedstoneTimerEntityRenderer::class })

val STONE_SPIKES = register(Identifier(MOD_ID, "stone_spikes"), Spikes(Spikes.Type.STONE, FabricBlockSettings.copyOf(Blocks.STONE)))
val IRON_SPIKES = register(Identifier(MOD_ID, "iron_spikes"), Spikes(Spikes.Type.IRON, FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
val GOLD_SPIKES = register(Identifier(MOD_ID, "gold_spikes"), Spikes(Spikes.Type.GOLD, FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK)))
val DIAMOND_SPIKES = register(Identifier(MOD_ID, "diamond_spikes"), Spikes(Spikes.Type.DIAMOND, FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK)))
val NETHERITE_SPIKES = register(Identifier(MOD_ID, "netherite_spikes"), Spikes(Spikes.Type.NETHERITE, FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK)))

val REGULAR_CONVEYOR_BELT = register(Identifier(MOD_ID, "regular_conveyor_belt"), ConveyorBelt(0.050))
val FAST_CONVEYOR_BELT = register(Identifier(MOD_ID, "fast_conveyor_belt"), ConveyorBelt(0.1))
val EXPRESS_CONVEYOR_BELT = register(Identifier(MOD_ID, "express_conveyor_belt"), ConveyorBelt(0.2))

val ENTANGLED_TANK = registerWithEntity<EntangledTankEntity>(Identifier(MOD_ID, "entangled_tank"), EntangledTank(), renderer = { EntangledTankEntityRenderer::class }, hasBlockItem = false, apiRegistrations = { FluidStorage.SIDED.registerForBlockEntity(EntangledTankEntity.Companion::getFluidStorage, it) })
val ENTANGLED_CHEST = registerWithEntity<EntangledChestEntity>(Identifier(MOD_ID, "entangled_chest"), EntangledChest(), renderer = { EntangledChestEntityRenderer::class }, hasBlockItem = false, containers = listOf(ContainerInfo<EntangledChestScreenHandler>(EntangledChestScreenHandler::class, { EntangledChestScreen::class })), apiRegistrations = { ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, it) })
val TRASH_CAN = registerWithEntity<TrashCanEntity>(Identifier(MOD_ID, "trash_can"), TrashCan(), containers = listOf(ContainerInfo<TrashCanScreenHandler>(TrashCanScreenHandler::class, {  TrashCanScreen::class })))
val VACUUM_HOPPER = registerWithEntity<VacuumHopperEntity>(Identifier(MOD_ID, "vacuum_hopper"), VacuumHopper(), renderer = { VacuumHopperEntityRenderer::class }, containers = listOf(ContainerInfo<VacuumHopperScreenHandler>(VacuumHopperScreenHandler::class, {  VacuumHopperScreen::class })), apiRegistrations = { FluidStorage.SIDED.registerForBlockEntity(VacuumHopperEntity.Companion::getFluidStorage, it) })
val BIG_TORCH = registerWithEntity<BigTorchBlockEntity>(Identifier(MOD_ID, "big_torch"), BigTorch(), containers = listOf(ContainerInfo<BigTorchScreenHandler>(BigTorchScreenHandler::class, { BigTorchScreen::class })))
val COOLER = registerWithEntity<CoolerBlockEntity>(Identifier(MOD_ID, "cooler"), Cooler(), hasBlockItem = false, containers = listOf(ContainerInfo<CoolerScreenHandler>(CoolerScreenHandler::class, { CoolerScreen::class })))
val DRAWBRIDGE = registerWithEntity<DrawbridgeBlockEntity>(Identifier(MOD_ID, "drawbridge"), Drawbridge(), containers = listOf(ContainerInfo<DrawbridgeScreenHandler>(DrawbridgeScreenHandler::class, { DrawbridgeScreen::class })))

val OBSIDIAN_SAND = register(Identifier(MOD_ID, "obsidian_sand"), FallingBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)))
val WITHER_PROOF_BLOCK = register(Identifier(MOD_ID, "wither_proof_block"), Block(FabricBlockSettings.copyOf(Blocks.OBSIDIAN)))
val WITHER_PROOF_SAND = register(Identifier(MOD_ID, "wither_proof_sand"), FallingBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)))
val WITHER_PROOF_GLASS = register(Identifier(MOD_ID, "wither_proof_glass"), GlassBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).nonOpaque()))
val WITHER_BUILDER = registerWithEntity<WitherBuilderBlockEntity>(Identifier(MOD_ID, "wither_builder"), WitherBuilder(), containers = listOf(ContainerInfo<WitherBuilderScreenHandler>(WitherBuilderScreenHandler::class, { WitherBuilderScreen::class })))

val PLACER = registerWithEntity<PlacerBlockEntity>(Identifier(MOD_ID, "placer"), Placer(), containers = listOf(ContainerInfo<PlacerScreenHandler>(PlacerScreenHandler::class, { PlacerScreen::class })))
val BREAKER = registerWithEntity<BreakerBlockEntity>(Identifier(MOD_ID, "breaker"), Breaker(), containers = listOf(ContainerInfo<BreakerScreenHandler>(BreakerScreenHandler::class, { BreakerScreen::class })))

val HEATER = registerWithEntity<HeaterBlockEntity>(Identifier(MOD_ID, "heater"), Heater())
val DEHUMIDIFIER = registerWithEntity<DehumidifierBlockEntity>(Identifier(MOD_ID, "dehumidifier"), Dehumidifier())

val COBBLESTONE_GENERATOR_MK1 = registerWithEntity<BlockGeneratorBlockEntity>(Identifier(MOD_ID, "cobblestone_generator_mk1"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.01f), containers = listOf(ContainerInfo<BlockGeneratorScreenHandler>(BlockGeneratorScreenHandler::class, { BlockGeneratorScreen::class })))
val COBBLESTONE_GENERATOR_MK2 = registerWithEntity<BlockGeneratorBlockEntity>(Identifier(MOD_ID, "cobblestone_generator_mk2"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.04f), containers = listOf(ContainerInfo<BlockGeneratorScreenHandler>(BlockGeneratorScreenHandler::class, { BlockGeneratorScreen::class })))
val COBBLESTONE_GENERATOR_MK3 = registerWithEntity<BlockGeneratorBlockEntity>(Identifier(MOD_ID, "cobblestone_generator_mk3"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.16f), containers = listOf(ContainerInfo<BlockGeneratorScreenHandler>(BlockGeneratorScreenHandler::class, { BlockGeneratorScreen::class })))
val COBBLESTONE_GENERATOR_MK4 = registerWithEntity<BlockGeneratorBlockEntity>(Identifier(MOD_ID, "cobblestone_generator_mk4"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.EMERALD_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.64f), containers = listOf(ContainerInfo<BlockGeneratorScreenHandler>(BlockGeneratorScreenHandler::class, { BlockGeneratorScreen::class })))
val COBBLESTONE_GENERATOR_MK5 = registerWithEntity<BlockGeneratorBlockEntity>(Identifier(MOD_ID, "cobblestone_generator_mk5"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK).luminance(4), Blocks.COBBLESTONE, 2.56f), containers = listOf(ContainerInfo<BlockGeneratorScreenHandler>(BlockGeneratorScreenHandler::class, { BlockGeneratorScreen::class })))

val BASALT_GENERATOR_MK1 = registerWithEntity<BlockGeneratorBlockEntity>(Identifier(MOD_ID, "basalt_generator_mk1"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).luminance(4), Blocks.BASALT, 0.01f), containers = listOf(ContainerInfo<BlockGeneratorScreenHandler>(BlockGeneratorScreenHandler::class, { BlockGeneratorScreen::class })))
val BASALT_GENERATOR_MK2 = registerWithEntity<BlockGeneratorBlockEntity>(Identifier(MOD_ID, "basalt_generator_mk2"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).luminance(4), Blocks.BASALT, 0.04f), containers = listOf(ContainerInfo<BlockGeneratorScreenHandler>(BlockGeneratorScreenHandler::class, { BlockGeneratorScreen::class })))
val BASALT_GENERATOR_MK3 = registerWithEntity<BlockGeneratorBlockEntity>(Identifier(MOD_ID, "basalt_generator_mk3"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).luminance(4), Blocks.BASALT, 0.16f), containers = listOf(ContainerInfo<BlockGeneratorScreenHandler>(BlockGeneratorScreenHandler::class, { BlockGeneratorScreen::class })))
val BASALT_GENERATOR_MK4 = registerWithEntity<BlockGeneratorBlockEntity>(Identifier(MOD_ID, "basalt_generator_mk4"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.EMERALD_BLOCK).luminance(4), Blocks.BASALT, 0.64f), containers = listOf(ContainerInfo<BlockGeneratorScreenHandler>(BlockGeneratorScreenHandler::class, { BlockGeneratorScreen::class })))
val BASALT_GENERATOR_MK5 = registerWithEntity<BlockGeneratorBlockEntity>(Identifier(MOD_ID, "basalt_generator_mk5"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK).luminance(4), Blocks.BASALT, 2.56f), containers = listOf(ContainerInfo<BlockGeneratorScreenHandler>(BlockGeneratorScreenHandler::class, { BlockGeneratorScreen::class })))

val LIGHT_SOURCE = register(Identifier(MOD_ID, "light_source"), LightSource(), false)
val CHUNK_LOADER = registerWithEntity<ChunkLoaderBlockEntity>(Identifier(MOD_ID, "chunk_loader"), ChunkLoader())
val TANK = registerWithEntity<TankBlockEntity>(Identifier(MOD_ID, "tank"), Tank(), hasBlockItem = false, renderer = { TankBlockEntityRenderer::class }, apiRegistrations = { FluidStorage.SIDED.registerForBlockEntity(TankBlockEntity.Companion::getFluidStorage, it) })
val XP_SHOWER = registerWithEntity<XpShowerBlockEntity>(Identifier(MOD_ID, "xp_shower"), XpShower())
val XP_DRAIN = register(Identifier(MOD_ID, "xp_drain"), XpDrain())
val IGNITER = register(Identifier(MOD_ID, "igniter"), Igniter())
val FLUID_HOPPER = registerWithEntity<FluidHopperBlockEntity>(
    Identifier(MOD_ID, "fluid_hopper"),
    FluidHopper(),
    apiRegistrations = { blockEntityType ->
        FluidStorage.SIDED.registerForBlockEntities(
            { blockEntity, _ -> (blockEntity as FluidHopperBlockEntity).tank },
            blockEntityType
        )
    }
)

val WHITE_ELEVATOR = register(Identifier(MOD_ID, "white_elevator"), Elevator())
val ORANGE_ELEVATOR = register(Identifier(MOD_ID, "orange_elevator"), Elevator())
val MAGENTA_ELEVATOR = register(Identifier(MOD_ID, "magenta_elevator"), Elevator())
val LIGHT_BLUE_ELEVATOR = register(Identifier(MOD_ID, "light_blue_elevator"), Elevator())
val YELLOW_ELEVATOR = register(Identifier(MOD_ID, "yellow_elevator"), Elevator())
val LIME_ELEVATOR = register(Identifier(MOD_ID, "lime_elevator"), Elevator())
val PINK_ELEVATOR = register(Identifier(MOD_ID, "pink_elevator"), Elevator())
val GRAY_ELEVATOR = register(Identifier(MOD_ID, "gray_elevator"), Elevator())
val LIGHT_GRAY_ELEVATOR = register(Identifier(MOD_ID, "light_gray_elevator"), Elevator())
val CYAN_ELEVATOR = register(Identifier(MOD_ID, "cyan_elevator"), Elevator())
val BLUE_ELEVATOR = register(Identifier(MOD_ID, "blue_elevator"), Elevator())
val PURPLE_ELEVATOR = register(Identifier(MOD_ID, "purple_elevator"), Elevator())
val GREEN_ELEVATOR = register(Identifier(MOD_ID, "green_elevator"), Elevator())
val BROWN_ELEVATOR = register(Identifier(MOD_ID, "brown_elevator"), Elevator())
val RED_ELEVATOR = register(Identifier(MOD_ID, "red_elevator"), Elevator())
val BLACK_ELEVATOR = register(Identifier(MOD_ID, "black_elevator"), Elevator())

fun initBlocks() {
    blockRegistry.forEach{ it.value.init() }
}

fun initBlocksClient() {
    blockRegistry.forEach{ it.value.initClient() }
}