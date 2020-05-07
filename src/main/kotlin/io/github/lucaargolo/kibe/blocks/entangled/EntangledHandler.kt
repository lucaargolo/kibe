package io.github.lucaargolo.kibe.blocks.entangled

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry

class EntangledHandler {

    val ENTANGLED_CHEST = EntangledChest()

    fun init() {
        Registry.register(Registry.BLOCK, ENTANGLED_CHEST.id, ENTANGLED_CHEST)
        Registry.register(Registry.ITEM, ENTANGLED_CHEST.id, BlockItem(ENTANGLED_CHEST, Item.Settings().group(ItemGroup.MISC)))
        Registry.register(Registry.BLOCK_ENTITY_TYPE, ENTANGLED_CHEST.id, ENTANGLED_CHEST.entityType)

        ContainerProviderRegistry.INSTANCE.registerFactory(ENTANGLED_CHEST.id) { syncId: Int, identifier: Identifier?, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
            val pos = packetByteBuf.readBlockPos()
            EntangledChestContainer(
                syncId,
                playerEntity.inventory,
                playerEntity.world.getBlockEntity(pos) as EntangledChestEntity,
                BlockContext.create(playerEntity.world, pos)
            )
        }
    }

    fun initClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(ENTANGLED_CHEST.id) { syncId: Int, identifier: Identifier?, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
            val pos = packetByteBuf.readBlockPos()
            val entity = playerEntity.entityWorld.getBlockEntity(pos) as EntangledChestEntity
            return@registerFactory EntangledChestScreen(
                EntangledChestContainer(
                    syncId,
                    playerEntity.inventory,
                    entity,
                    BlockContext.EMPTY
                ), playerEntity.inventory, entity.name
            )
        }

        BlockEntityRendererRegistry.INSTANCE.register(ENTANGLED_CHEST.entityType) {
            EntangledChestEntityRenderer(it) as BlockEntityRenderer<BlockEntity>
        }
    }

}