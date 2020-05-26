package io.github.lucaargolo.kibe.blocks

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.entangled.*
import io.github.lucaargolo.kibe.blocks.miscellaneous.*
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCan
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCanContainer
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCanEntity
import io.github.lucaargolo.kibe.blocks.trashcan.TrashCanScreen
import io.github.lucaargolo.kibe.blocks.vacuum.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.Identifier

val blockRegistry = mutableMapOf<Identifier, ModBlock>()

val CURSED_DIRT = register(Identifier(MOD_ID, "cursed_dirt"), ModBlock(CursedDirt()))
val BLESSED_DIRT = register(Identifier(MOD_ID, "blessed_dirt"), ModBlock(BlessedDirt()))
val REDSTONE_TIMER = register(Identifier(MOD_ID, "redstone_timer"), ModBlockWithEntity<RedstoneTimerEntity>(RedstoneTimer(), RedstoneTimerEntityRenderer::class))
val IRON_SPIKES = register(Identifier(MOD_ID, "iron_spikes"), ModBlock(Spikes(6F, false, FabricBlockSettings.of(Material.METAL))))
val DIAMOND_SPIKES = register(Identifier(MOD_ID, "diamond_spikes"), ModBlock(Spikes(7F, true, FabricBlockSettings.of(Material.METAL))))
val REGULAR_CONVEYOR_BELT = register(Identifier(MOD_ID, "regular_conveyor_belt"), ModBlock(ConveyorBelt(0.125F)))
val FAST_CONVEYOR_BELT = register(Identifier(MOD_ID, "fast_conveyor_belt"), ModBlock(ConveyorBelt(0.25F)))
val EXPRESS_CONVEYOR_BELT = register(Identifier(MOD_ID, "express_conveyor_belt"), ModBlock(ConveyorBelt(0.5F)))
val ENTANGLED_CHEST = register(Identifier(MOD_ID, "entangled_chest"), ModBlockWithEntity<EntangledChestEntity>(EntangledChest(), EntangledChestEntityRenderer::class, EntangledChestContainer::class, EntangledChestScreen::class))
val TRASH_CAN = register(Identifier(MOD_ID, "trash_can"), ModBlockWithEntity<TrashCanEntity>(TrashCan(), TrashCanContainer::class, TrashCanScreen::class))
val VACUUM_HOPPER = register(Identifier(MOD_ID, "vacuum_hopper"), ModBlockWithEntity<VacuumHopperEntity>(VacuumHopper(), VacuumHopperEntityRenderer::class, VacuumHopperContainer::class, VacuumHopperScreen::class))

val WHITE_ELEVATOR = register(Identifier(MOD_ID, "white_elevator"), ModBlock(Elevator()))
val ORANGE_ELEVATOR = register(Identifier(MOD_ID, "orange_elevator"), ModBlock(Elevator()))
val MAGENTA_ELEVATOR = register(Identifier(MOD_ID, "magenta_elevator"), ModBlock(Elevator()))
val LIGHT_BLUE_ELEVATOR = register(Identifier(MOD_ID, "light_blue_elevator"), ModBlock(Elevator()))
val YELLOW_ELEVATOR = register(Identifier(MOD_ID, "yellow_elevator"), ModBlock(Elevator()))
val LIME_ELEVATOR = register(Identifier(MOD_ID, "lime_elevator"), ModBlock(Elevator()))
val PINK_ELEVATOR = register(Identifier(MOD_ID, "pink_elevator"), ModBlock(Elevator()))
val GRAY_ELEVATOR = register(Identifier(MOD_ID, "gray_elevator"), ModBlock(Elevator()))
val LIGHT_GRAY_ELEVATOR = register(Identifier(MOD_ID, "light_gray_elevator"), ModBlock(Elevator()))
val CYAN_ELEVATOR = register(Identifier(MOD_ID, "cyan_elevator"), ModBlock(Elevator()))
val BLUE_ELEVATOR = register(Identifier(MOD_ID, "blue_elevator"), ModBlock(Elevator()))
val PURPLE_ELEVATOR = register(Identifier(MOD_ID, "purple_elevator"), ModBlock(Elevator()))
val GREEN_ELEVATOR = register(Identifier(MOD_ID, "green_elevator"), ModBlock(Elevator()))
val BROWN_ELEVATOR = register(Identifier(MOD_ID, "brown_elevator"), ModBlock(Elevator()))
val RED_ELEVATOR = register(Identifier(MOD_ID, "red_elevator"), ModBlock(Elevator()))
val BLACK_ELEVATOR = register(Identifier(MOD_ID, "black_elevator"), ModBlock(Elevator()))

private fun register(identifier: Identifier, block: ModBlock): Block {
    blockRegistry[identifier] = block
    return block.block
}

fun getBlockId(block: Block): Identifier? {
    blockRegistry.forEach {
        if(it.value.block == block) return it.key
    }
    return null
}

fun getEntityType(block: Block): BlockEntityType<out BlockEntity>? {
    blockRegistry.forEach {
        if(it.value.block == block) return (it.value as ModBlockWithEntity<*>).entity
    }
    return null
}

fun initBlocks() {
    blockRegistry.forEach{ it.value.init(it.key) }
}

fun initBlocksClient() {
    blockRegistry.forEach{ it.value.initClient(it.key) }
}
