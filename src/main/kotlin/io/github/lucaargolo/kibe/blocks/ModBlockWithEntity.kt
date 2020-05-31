package io.github.lucaargolo.kibe.blocks

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST", "unused")
class ModBlockWithEntity<T: BlockEntity>: ModBlock {

    var entity: BlockEntityType<T>? = null
        private set
    private var renderer: KClass<BlockEntityRenderer<T>>? = null
    private var container: KClass<ScreenHandler>? = null
    private var containerScreen: KClass<HandledScreen<*>>? = null

    constructor(block: BlockWithEntity) : super(block) {
        this.entity = BlockEntityType.Builder.create(Supplier { block.createBlockEntity(null) }, block).build(null) as BlockEntityType<T>
    }

    constructor(block: BlockWithEntity, blockEntityRenderer: KClass<*>) : super(block) {
        this.entity = BlockEntityType.Builder.create(Supplier { block.createBlockEntity(null) }, block).build(null) as BlockEntityType<T>
        this.renderer = blockEntityRenderer as KClass<BlockEntityRenderer<T>>
    }

    constructor(block: BlockWithEntity, blockEntityRenderer: KClass<*>, blockEntityScreenHandler: KClass<*>, blockEntityScreen: KClass<*>) : super(block) {
        this.entity = BlockEntityType.Builder.create(Supplier { block.createBlockEntity(null) }, block).build(null) as BlockEntityType<T>
        this.renderer = blockEntityRenderer as KClass<BlockEntityRenderer<T>>
        this.container = blockEntityScreenHandler as KClass<ScreenHandler>
        this.containerScreen = blockEntityScreen as KClass<HandledScreen<*>>
    }

    constructor(block: BlockWithEntity, blockEntityScreenHandler: KClass<*>, blockEntityScreen: KClass<*>) : super(block) {
        this.entity = BlockEntityType.Builder.create(Supplier { block.createBlockEntity(null) }, block).build(null) as BlockEntityType<T>
        this.container = blockEntityScreenHandler as KClass<ScreenHandler>
        this.containerScreen = blockEntityScreen as KClass<HandledScreen<*>>
    }

    override fun init(identifier: Identifier) {
        super.init(identifier)
        if (entity != null) {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, identifier, entity)
        }
        if (container != null) {
            ContainerProviderRegistry.INSTANCE.registerFactory(identifier) { syncId: Int, _, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
                val pos = packetByteBuf.readBlockPos()
                container!!.primaryConstructor!!.call(syncId,
                    playerEntity.inventory,
                    playerEntity.world.getBlockEntity(pos),
                    ScreenHandlerContext.create(playerEntity.world, pos)
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
                containerScreen!!.primaryConstructor!!.call(
                    container!!.primaryConstructor!!.call(
                        syncId,
                        playerEntity.inventory,
                        entity,
                        ScreenHandlerContext.EMPTY
                    ), playerEntity.inventory, entity.name
                )
            }
        }
        if(renderer != null) {
            BlockEntityRendererRegistry.INSTANCE.register(entity) { it2 ->
                renderer!!.primaryConstructor!!.call(it2)
            }
        }
    }
}