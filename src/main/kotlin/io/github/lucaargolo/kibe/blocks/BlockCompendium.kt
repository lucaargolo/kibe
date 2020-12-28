@file:Suppress("UNCHECKED_CAST")

package io.github.lucaargolo.kibe.blocks

import io.github.lucaargolo.kibe.CLIENT
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorch
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchScreen
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchScreenHandler
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
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntityRenderer
import io.github.lucaargolo.kibe.blocks.miscellaneous.*
import io.github.lucaargolo.kibe.blocks.tank.Tank
import io.github.lucaargolo.kibe.blocks.tank.TankBlockEntity
import io.github.lucaargolo.kibe.blocks.tank.TankBlockEntityRenderer
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCan
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCanScreenHandler
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCanEntity
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCanScreen
import io.github.lucaargolo.kibe.blocks.vacuum.*
import io.github.lucaargolo.kibe.items.entangledchest.EntangledChestBlockItem
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.Material
import net.minecraft.block.MaterialColor
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
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

    var title: Text = LiteralText("")

    fun init(blockIdentifier: Identifier) {
        val id = identifier ?: blockIdentifier
        title = TranslatableText("screen.$MOD_ID.${id.path}")
        handlerType = ScreenHandlerRegistry.registerExtended(id) { i, playerInventory, packetByteBuf ->
            val pos = packetByteBuf.readBlockPos()
            val player = playerInventory.player
            val world = player.world
            val be = world.getBlockEntity(pos)
            handler = handlerClass.java.constructors[0].newInstance(i, playerInventory, be, ScreenHandlerContext.create(world, pos)) as T
            handler
        }
    }

    fun initClient() {
        ScreenRegistry.register(handlerType) { handler, playerInventory, title -> screenClass.get().java.constructors[0].newInstance(handler, playerInventory, title) as HandledScreen<T>  }
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
                Registry.register(Registry.ITEM, identifier, blockItem.java.constructors[0].newInstance(block, Item.Settings()) as BlockItem)
            else
                Registry.register(Registry.ITEM, identifier, BlockItem(block, Item.Settings()))
        }
        if(entity != null) Registry.register(Registry.BLOCK_ENTITY_TYPE, identifier, entity)
        containers.forEach { it.init(identifier) }
    }

    fun initClient() {
        containers.forEach { it.initClient() }
        if(renderer != null) {
            BlockEntityRendererRegistry.INSTANCE.register(entity) { it2 ->
                renderer!!.java.constructors[0].newInstance(it2) as BlockEntityRenderer<T>
            }
        }
    }

}

val blockRegistry = linkedMapOf<Block, BlockInfo<*>>()

fun getBlockId(block: Block) = blockRegistry[block]?.identifier
fun getEntityType(block: Block) = blockRegistry[block]?.entity
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

fun <T: BlockEntity> registerWithEntity(identifier: Identifier, block: Block, hasBlockItem: Boolean = true, blockItem: KClass<*>? = null, renderer: Supplier<KClass<*>>? = null, containers: List<ContainerInfo<*>> = listOf()): Block {
    val bli = blockItem as? KClass<BlockItem>
    val ent = (block as? BlockEntityProvider)?.let { BlockEntityType.Builder.create(Supplier { it.createBlockEntity(null) }, block).build(null) as BlockEntityType<T> }
    val rnd = if(CLIENT) renderer?.let { it.get() as KClass<BlockEntityRenderer<T>> } else null
    val info = BlockInfo(identifier, block, hasBlockItem, bli, ent, rnd, containers)
    blockRegistry[block] = info
    return block
}

val CURSED_DIRT = register(Identifier(MOD_ID, "cursed_dirt"), CursedDirt())
val REDSTONE_TIMER = registerWithEntity<RedstoneTimerEntity>(Identifier(MOD_ID, "redstone_timer"), RedstoneTimer(), renderer = Supplier { RedstoneTimerEntityRenderer::class })

val IRON_SPIKES = register(Identifier(MOD_ID, "iron_spikes"), Spikes(6F, false, FabricBlockSettings.of(Material.METAL, MaterialColor.IRON).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)))
val DIAMOND_SPIKES = register(Identifier(MOD_ID, "diamond_spikes"), Spikes(7F, true, FabricBlockSettings.of(Material.METAL, MaterialColor.DIAMOND).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)))
val REGULAR_CONVEYOR_BELT = register(Identifier(MOD_ID, "regular_conveyor_belt"), ConveyorBelt(0.050))
val FAST_CONVEYOR_BELT = register(Identifier(MOD_ID, "fast_conveyor_belt"), ConveyorBelt(0.1))
val EXPRESS_CONVEYOR_BELT = register(Identifier(MOD_ID, "express_conveyor_belt"), ConveyorBelt(0.2))

val ENTANGLED_TANK = registerWithEntity<EntangledChestEntity>(Identifier(MOD_ID, "entangled_tank"), EntangledTank(), renderer = Supplier { EntangledTankEntityRenderer::class }, hasBlockItem = false)
val ENTANGLED_CHEST = registerWithEntity<EntangledChestEntity>(Identifier(MOD_ID, "entangled_chest"), EntangledChest(), renderer = Supplier { EntangledChestEntityRenderer::class }, hasBlockItem = false, containers = listOf(ContainerInfo<EntangledChestScreenHandler>(EntangledChestScreenHandler::class, Supplier { EntangledChestScreen::class })))
val TRASH_CAN = registerWithEntity<TrashCanEntity>(Identifier(MOD_ID, "trash_can"), TrashCan(), containers = listOf(ContainerInfo<TrashCanScreenHandler>(TrashCanScreenHandler::class, Supplier {  TrashCanScreen::class })))
val VACUUM_HOPPER = registerWithEntity<VacuumHopperEntity>(Identifier(MOD_ID, "vacuum_hopper"), VacuumHopper(), renderer = Supplier { VacuumHopperEntityRenderer::class }, containers = listOf(ContainerInfo<VacuumHopperScreenHandler>(VacuumHopperScreenHandler::class, Supplier {  VacuumHopperScreen::class })))
val BIG_TORCH = registerWithEntity<BigTorchBlockEntity>(Identifier(MOD_ID, "big_torch"), BigTorch(), containers = listOf(ContainerInfo<BigTorchScreenHandler>(BigTorchScreenHandler::class, Supplier { BigTorchScreen::class })))
val COOLER = registerWithEntity<CoolerBlockEntity>(Identifier(MOD_ID, "cooler"), Cooler(), hasBlockItem = false, containers = listOf(ContainerInfo<CoolerScreenHandler>(CoolerScreenHandler::class, Supplier { CoolerScreen::class })))
val DRAWBRIDGE = registerWithEntity<DrawbridgeBlockEntity>(Identifier(MOD_ID, "drawbridge"), Drawbridge(), containers = listOf(ContainerInfo<DrawbridgeScreenHandler>(DrawbridgeScreenHandler::class, Supplier { DrawbridgeScreen::class })))

val LIGHT_SOURCE = register(Identifier(MOD_ID, "light_source"), LightSource(), false)
val CHUNK_LOADER = registerWithEntity<ChunkLoaderBlockEntity>(Identifier(MOD_ID, "chunk_loader"), ChunkLoader())
val TANK = registerWithEntity<TankBlockEntity>(Identifier(MOD_ID, "tank"), Tank(), hasBlockItem = false, renderer = Supplier { TankBlockEntityRenderer::class })
val XP_SHOWER = registerWithEntity<XpShowerBlockEntity>(Identifier(MOD_ID, "xp_shower"), XpShower())
val XP_DRAIN = register(Identifier(MOD_ID, "xp_drain"), XpDrain())
val FLUID_HOPPER = registerWithEntity<FluidHopperBlockEntity>(Identifier(MOD_ID, "fluid_hopper"), FluidHopper())

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