package io.github.lucaargolo.kibe

import io.github.lucaargolo.kibe.blocks.entangled.EntangledChestEntityRenderer
import io.github.lucaargolo.kibe.blocks.entangled.EntangledHandler
import io.github.lucaargolo.kibe.blocks.miscellaneous.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.*

val MOD_ID = "kibe"
val FAKE_PLAYER_UUID = UUID.randomUUID();
val ENTANGLED_HANDLER = EntangledHandler()

val REDSTONE_TIMER = RedstoneTimer()


@Suppress("unused")
fun init() {

    ENTANGLED_HANDLER.init()

    val CURSED_EARTH = CursedDirt()
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "cursed_dirt"), CURSED_EARTH)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "cursed_dirt"), BlockItem(CURSED_EARTH, Item.Settings().group(ItemGroup.MISC)))

    Registry.register(Registry.BLOCK, REDSTONE_TIMER.id, REDSTONE_TIMER)
    Registry.register(Registry.ITEM, REDSTONE_TIMER.id, BlockItem(REDSTONE_TIMER, Item.Settings().group(ItemGroup.MISC)))
    Registry.register(Registry.BLOCK_ENTITY_TYPE, REDSTONE_TIMER.id, REDSTONE_TIMER.entityType)

    val IRON_SPIKES = Spikes(6F, false, FabricBlockSettings.of(Material.METAL))
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "iron_spikes"), IRON_SPIKES)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "iron_spikes"), BlockItem(IRON_SPIKES, Item.Settings().group(ItemGroup.MISC)))
    val DIAMOND_SPIKES = Spikes(7F, true, FabricBlockSettings.of(Material.METAL))
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "diamond_spikes"), DIAMOND_SPIKES)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "diamond_spikes"), BlockItem(DIAMOND_SPIKES, Item.Settings().group(ItemGroup.MISC)))

    val REGULAR_CONVEYOR_BELT = ConveyorBelt(0.125F)
    val FAST_CONVEYOR_BELT = ConveyorBelt(0.25F)
    val EXPRESS_CONVEYOR_BELT = ConveyorBelt(0.5F)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "regular_conveyor_belt"), REGULAR_CONVEYOR_BELT)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "regular_conveyor_belt"), BlockItem(REGULAR_CONVEYOR_BELT, Item.Settings().group(ItemGroup.MISC)))
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "fast_conveyor_belt"), FAST_CONVEYOR_BELT)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "fast_conveyor_belt"), BlockItem(FAST_CONVEYOR_BELT, Item.Settings().group(ItemGroup.MISC)))
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "express_conveyor_belt"), EXPRESS_CONVEYOR_BELT)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "express_conveyor_belt"), BlockItem(EXPRESS_CONVEYOR_BELT, Item.Settings().group(ItemGroup.MISC)))

}

@Suppress("unused")
fun initClient() {


    ENTANGLED_HANDLER.initClient()


    BlockEntityRendererRegistry.INSTANCE.register(REDSTONE_TIMER.entityType) {
        RedstoneTimerEntityRenderer(it)
    }
}

