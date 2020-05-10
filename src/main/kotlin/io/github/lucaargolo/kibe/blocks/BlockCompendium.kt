package io.github.lucaargolo.kibe.blocks

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.entangled.*
import io.github.lucaargolo.kibe.blocks.miscellaneous.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.Identifier

private val registry = mutableMapOf<Identifier, ModBlock>()

val CURSED_DIRT = register(Identifier(MOD_ID, "cursed_dirt"), ModBlock(CursedDirt()))
val REDSTONE_TIMER = register(Identifier(MOD_ID, "redstone_timer"), ModBlockWithEntity<RedstoneTimerEntity>(RedstoneTimer(), RedstoneTimerEntityRenderer::class))
val IRON_SPIKES = register(Identifier(MOD_ID, "iron_spikes"), ModBlock(Spikes(6F, false, FabricBlockSettings.of(Material.METAL))))
val DIAMOND_SPIKES = register(Identifier(MOD_ID, "diamond_spikes"), ModBlock(Spikes(7F, true, FabricBlockSettings.of(Material.METAL))))
val REGULAR_CONVEYOR_BELT = register(Identifier(MOD_ID, "regular_conveyor_belt"), ModBlock(ConveyorBelt(0.125F)))
val FAST_CONVEYOR_BELT = register(Identifier(MOD_ID, "fast_conveyor_belt"), ModBlock(ConveyorBelt(0.25F)))
val EXPRESS_CONVEYOR_BELT = register(Identifier(MOD_ID, "express_conveyor_belt"), ModBlock(ConveyorBelt(0.5F)))
val ENTANGLED_CHEST = register(Identifier(MOD_ID, "entangled_chest"), ModBlockWithEntity<EntangledChestEntity>(EntangledChest(), EntangledChestEntityRenderer::class, EntangledChestContainer::class, EntangledChestScreen::class))

private fun register(identifier: Identifier, block: ModBlock): Block {
    registry[identifier] = block
    return block.block
}

fun getId(block: Block): Identifier? {
    registry.forEach {
        if(it.value.block == block) return it.key
    }
    return null
}

fun getEntityType(block: Block): BlockEntityType<out BlockEntity>? {
    registry.forEach {
        if(it.value.block == block) return (it.value as ModBlockWithEntity<*>).entity
    }
    return null
}

fun initBlocks() {
    registry.forEach{ it.value.init(it.key) }
}

fun initBlocksClient() {
    registry.forEach{ it.value.initClient(it.key) }
}
