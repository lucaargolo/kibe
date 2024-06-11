@file:Suppress("UNCHECKED_CAST", "DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.kibe.blocks
import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.blockentities.*
import io.github.lucaargolo.kibe.client.blockentities.*
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import java.util.function.Supplier
import kotlin.reflect.KClass


class BlockInfo<T: BlockEntity> (
    val identifier: Identifier,
    private val block: Block,
    private val hasBlockItem: Boolean,
    private val blockItem: KClass<BlockItem>?,
    var entity: BlockEntityType<T>?,
    var renderer: KClass<BlockEntityRenderer<T>>?,
){

    fun init() {
        Registry.register(Registries.BLOCK, identifier, block)
        if(hasBlockItem) {
            if(blockItem != null)
                Registry.register(Registries.ITEM, identifier, blockItem.java.constructors[0].newInstance(block, Item.Settings()) as BlockItem)
            else
                Registry.register(Registries.ITEM, identifier, BlockItem(block, Item.Settings()))
        }
        if(entity != null) Registry.register(Registries.BLOCK_ENTITY_TYPE, identifier, entity)
    }

    fun initClient() {
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

fun register(identifier: Identifier, block: Block, hasModBlock: Boolean = true): Block {
    val info = BlockInfo<BlockEntity>(identifier, block, hasModBlock, null, null, null)
    blockRegistry[block] = info
    return block
}

fun <T : BlockEntity> registerWithEntity(identifier: Identifier, block: Block, hasBlockItem: Boolean = true, blockItem: KClass<*>? = null, renderer: Supplier<KClass<*>>? = null, apiRegistrations: (BlockEntityType<T>) -> Unit = {}): Block {
    val bli = blockItem as? KClass<BlockItem>
    val ent = (block as? BlockEntityProvider)?.let { BlockEntityType.Builder.create({ blockPos, blockState -> block.createBlockEntity(blockPos, blockState) } , block).build(null) as BlockEntityType<T> }
    ent?.let { apiRegistrations(it) }
    val rnd = if(KibeMod.CLIENT) renderer?.let { it.get() as KClass<BlockEntityRenderer<T>> } else null
    val info = BlockInfo(identifier, block, hasBlockItem, bli, ent, rnd)
    blockRegistry[block] = info
    return block
}

val CURSED_DIRT = register(ModIdentifier("cursed_dirt"), CursedDirt())
val REDSTONE_TIMER = registerWithEntity<RedstoneTimerEntity>(ModIdentifier("redstone_timer"), RedstoneTimer(), renderer = { RedstoneTimerEntityRenderer::class })

val STONE_SPIKES = register(ModIdentifier("stone_spikes"), Spikes(Spikes.Type.STONE, FabricBlockSettings.copyOf(Blocks.STONE)))
val IRON_SPIKES = register(ModIdentifier("iron_spikes"), Spikes(Spikes.Type.IRON, FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
val GOLD_SPIKES = register(ModIdentifier("gold_spikes"), Spikes(Spikes.Type.GOLD, FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK)))
val DIAMOND_SPIKES = register(ModIdentifier("diamond_spikes"), Spikes(Spikes.Type.DIAMOND, FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK)))

val REGULAR_CONVEYOR_BELT = register(ModIdentifier("regular_conveyor_belt"),
    io.github.lucaargolo.kibe.blocks.ConveyorBelt(0.050)
)
val FAST_CONVEYOR_BELT = register(ModIdentifier("fast_conveyor_belt"),
    io.github.lucaargolo.kibe.blocks.ConveyorBelt(0.1)
)
val EXPRESS_CONVEYOR_BELT = register(ModIdentifier("express_conveyor_belt"),
    io.github.lucaargolo.kibe.blocks.ConveyorBelt(0.2)
)

val ENTANGLED_TANK = registerWithEntity<EntangledTankEntity>(ModIdentifier("entangled_tank"), EntangledTank(), renderer = { EntangledTankEntityRenderer::class }, hasBlockItem = false, apiRegistrations = { FluidStorage.SIDED.registerForBlockEntity(
    EntangledTankEntity.Companion::getFluidStorage, it) })
val ENTANGLED_CHEST = registerWithEntity<EntangledChestEntity>(ModIdentifier("entangled_chest"), EntangledChest(), renderer = { EntangledChestEntityRenderer::class }, hasBlockItem = false, apiRegistrations = { ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, it) })
val TRASH_CAN = registerWithEntity<TrashCanEntity>(ModIdentifier("trash_can"), TrashCan())
val VACUUM_HOPPER = registerWithEntity<VacuumHopperEntity>(ModIdentifier("vacuum_hopper"), VacuumHopper(), renderer = { VacuumHopperEntityRenderer::class }, apiRegistrations = { FluidStorage.SIDED.registerForBlockEntity(
    VacuumHopperEntity.Companion::getFluidStorage, it) })
val BIG_TORCH = registerWithEntity<BigTorchBlockEntity>(ModIdentifier("big_torch"), BigTorch())
val COOLER = registerWithEntity<CoolerBlockEntity>(ModIdentifier("cooler"), Cooler(), hasBlockItem = false)
val DRAWBRIDGE = registerWithEntity<DrawbridgeBlockEntity>(ModIdentifier("drawbridge"), Drawbridge())

val OBSIDIAN_SAND = register(ModIdentifier("obsidian_sand"), FallingBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)))
val WITHER_PROOF_BLOCK = register(ModIdentifier("wither_proof_block"), Block(FabricBlockSettings.copyOf(Blocks.OBSIDIAN)))
val WITHER_PROOF_SAND = register(ModIdentifier("wither_proof_sand"), FallingBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)))
val WITHER_PROOF_GLASS = register(ModIdentifier("wither_proof_glass"), GlassBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).nonOpaque()))
val WITHER_BUILDER = registerWithEntity<WitherBuilderBlockEntity>(ModIdentifier("wither_builder"), WitherBuilder())

val PLACER = registerWithEntity<PlacerBlockEntity>(ModIdentifier("placer"), Placer())
val BREAKER = registerWithEntity<BreakerBlockEntity>(ModIdentifier("breaker"), Breaker())

val HEATER = registerWithEntity<HeaterBlockEntity>(ModIdentifier("heater"), Heater())
val DEHUMIDIFIER = registerWithEntity<DehumidifierBlockEntity>(ModIdentifier("dehumidifier"), Dehumidifier())

val COBBLESTONE_GENERATOR_MK1 = registerWithEntity<BlockGeneratorBlockEntity>(ModIdentifier("cobblestone_generator_mk1"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.01f))
val COBBLESTONE_GENERATOR_MK2 = registerWithEntity<BlockGeneratorBlockEntity>(ModIdentifier("cobblestone_generator_mk2"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.04f))
val COBBLESTONE_GENERATOR_MK3 = registerWithEntity<BlockGeneratorBlockEntity>(ModIdentifier("cobblestone_generator_mk3"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.16f))
val COBBLESTONE_GENERATOR_MK4 = registerWithEntity<BlockGeneratorBlockEntity>(ModIdentifier("cobblestone_generator_mk4"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.EMERALD_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.64f))
val COBBLESTONE_GENERATOR_MK5 = registerWithEntity<BlockGeneratorBlockEntity>(ModIdentifier("cobblestone_generator_mk5"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK).luminance(4), Blocks.COBBLESTONE, 2.56f))

val BASALT_GENERATOR_MK1 = registerWithEntity<BlockGeneratorBlockEntity>(ModIdentifier("basalt_generator_mk1"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).luminance(4), Blocks.BASALT, 0.01f))
val BASALT_GENERATOR_MK2 = registerWithEntity<BlockGeneratorBlockEntity>(ModIdentifier("basalt_generator_mk2"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).luminance(4), Blocks.BASALT, 0.04f))
val BASALT_GENERATOR_MK3 = registerWithEntity<BlockGeneratorBlockEntity>(ModIdentifier("basalt_generator_mk3"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).luminance(4), Blocks.BASALT, 0.16f))
val BASALT_GENERATOR_MK4 = registerWithEntity<BlockGeneratorBlockEntity>(ModIdentifier("basalt_generator_mk4"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.EMERALD_BLOCK).luminance(4), Blocks.BASALT, 0.64f))
val BASALT_GENERATOR_MK5 = registerWithEntity<BlockGeneratorBlockEntity>(ModIdentifier("basalt_generator_mk5"), BlockGenerator(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK).luminance(4), Blocks.BASALT, 2.56f))

val LIGHT_SOURCE = register(ModIdentifier("light_source"), LightSource(), false)
val CHUNK_LOADER = registerWithEntity<ChunkLoaderBlockEntity>(ModIdentifier("chunk_loader"), ChunkLoader())
val TANK = registerWithEntity<TankBlockEntity>(ModIdentifier("tank"), Tank(), hasBlockItem = false, renderer = { TankBlockEntityRenderer::class }, apiRegistrations = { FluidStorage.SIDED.registerForBlockEntity(
    TankBlockEntity.Companion::getFluidStorage, it) })
val XP_SHOWER = registerWithEntity<XpShowerBlockEntity>(ModIdentifier("xp_shower"), XpShower())
val XP_DRAIN = register(ModIdentifier("xp_drain"), XpDrain())
val IGNITER = register(ModIdentifier("igniter"), Igniter())
val FLUID_HOPPER = registerWithEntity<FluidHopperBlockEntity>(
    ModIdentifier("fluid_hopper"),
    FluidHopper(),
    apiRegistrations = { blockEntityType ->
        FluidStorage.SIDED.registerForBlockEntities(
            { blockEntity, _ -> (blockEntity as FluidHopperBlockEntity).tank },
            blockEntityType
        )
    }
)

val WHITE_ELEVATOR = register(ModIdentifier("white_elevator"), Elevator())
val ORANGE_ELEVATOR = register(ModIdentifier("orange_elevator"), Elevator())
val MAGENTA_ELEVATOR = register(ModIdentifier("magenta_elevator"), Elevator())
val LIGHT_BLUE_ELEVATOR = register(ModIdentifier("light_blue_elevator"), Elevator())
val YELLOW_ELEVATOR = register(ModIdentifier("yellow_elevator"), Elevator())
val LIME_ELEVATOR = register(ModIdentifier("lime_elevator"), Elevator())
val PINK_ELEVATOR = register(ModIdentifier("pink_elevator"), Elevator())
val GRAY_ELEVATOR = register(ModIdentifier("gray_elevator"), Elevator())
val LIGHT_GRAY_ELEVATOR = register(ModIdentifier("light_gray_elevator"), Elevator())
val CYAN_ELEVATOR = register(ModIdentifier("cyan_elevator"), Elevator())
val BLUE_ELEVATOR = register(ModIdentifier("blue_elevator"), Elevator())
val PURPLE_ELEVATOR = register(ModIdentifier("purple_elevator"), Elevator())
val GREEN_ELEVATOR = register(ModIdentifier("green_elevator"), Elevator())
val BROWN_ELEVATOR = register(ModIdentifier("brown_elevator"), Elevator())
val RED_ELEVATOR = register(ModIdentifier("red_elevator"), Elevator())
val BLACK_ELEVATOR = register(ModIdentifier("black_elevator"), Elevator())

fun initBlocks() {
    blockRegistry.forEach{ it.value.init() }
}

fun initBlocksClient() {
    blockRegistry.forEach{ it.value.initClient() }
}