package io.github.lucaargolo.kibe.blocks

import io.github.lucaargolo.kibe.blocks.entangled.EntangledChestEntity
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.client.gui.screen.ingame.ContainerScreen
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.container.BlockContext
import net.minecraft.container.Container
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import java.util.function.Supplier

class ModBlockWithEntity<T: BlockEntity>: ModBlock {

    var entity: BlockEntityType<T>? = null
        private set
    private var renderer: Class<BlockEntityRenderer<T>>? = null
    private var container: Class<Container>? = null
    private var containerScreen: Class<ContainerScreen<*>>? = null

    @Suppress("UNCHECKED_CAST", "unused")
    constructor(block: BlockWithEntity) : super(block) {
        this.entity = BlockEntityType.Builder.create(Supplier { block.createBlockEntity(null) }, block).build(null) as BlockEntityType<T>
    }

    @Suppress("UNCHECKED_CAST", "unused")
    constructor(block: BlockWithEntity, blockEntityRenderer: Class<*>) : super(block) {
        this.entity = BlockEntityType.Builder.create(Supplier { block.createBlockEntity(null) }, block).build(null) as BlockEntityType<T>
        this.renderer = blockEntityRenderer as Class<BlockEntityRenderer<T>>
    }

    @Suppress("UNCHECKED_CAST", "unused")
    constructor(block: BlockWithEntity, blockEntityRenderer: Class<*>, blockEntityContainer: Class<*>, blockEntityScreen: Class<*>) : super(block) {
        this.entity = BlockEntityType.Builder.create(Supplier { block.createBlockEntity(null) }, block).build(null) as BlockEntityType<T>
        this.renderer = blockEntityRenderer as Class<BlockEntityRenderer<T>>
        this.container = blockEntityContainer as Class<Container>
        this.containerScreen = blockEntityScreen as Class<ContainerScreen<*>>
    }

    override fun init(identifier: Identifier) {
        super.init(identifier)
        if (entity != null) {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, identifier, entity)
        }
        if (container != null) {
            ContainerProviderRegistry.INSTANCE.registerFactory(identifier) { syncId: Int, _, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
                val pos = packetByteBuf.readBlockPos()
                container!!.getConstructor().newInstance(syncId,
                    playerEntity.inventory,
                    playerEntity.world.getBlockEntity(pos) as EntangledChestEntity,
                    BlockContext.create(playerEntity.world, pos)
                )
            }
        }
    }

    override fun initClient(identifier: Identifier) {
        super.initClient(identifier)
        if(containerScreen != null) {
            ScreenProviderRegistry.INSTANCE.registerFactory(identifier) { syncId: Int, _, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
                val pos = packetByteBuf.readBlockPos()
                val entity = playerEntity.entityWorld.getBlockEntity(pos) as LockableContainerBlockEntity
                containerScreen!!.getConstructor().newInstance(
                    container!!.getConstructor().newInstance(
                        syncId,
                        playerEntity.inventory,
                        entity,
                        BlockContext.EMPTY
                    ), playerEntity.inventory, entity.name
                )
            }
        }
        if(renderer != null) {
            BlockEntityRendererRegistry.INSTANCE.register(entity) { it2 ->
                renderer!!.getConstructor().newInstance(it2)
            }
        }
    }
}