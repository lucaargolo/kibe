package io.github.lucaargolo.kibe.items

import com.mojang.datafixers.util.Pair
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.client.gui.screen.ingame.ContainerScreen
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.container.Container
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import java.util.function.Function
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

open class ModItem(item: Item) {

    var item: Item = item
        private set
    private var bakedModel: BakedModel? = null
    private var container: KClass<Container>? = null
    private var containerScreen: KClass<ContainerScreen<*>>? = null

    constructor(): this(Item(Item.Settings()))

    @Suppress("UNCHECKED_CAST")
    constructor(item: Item, container: KClass<*>, containerScreen: KClass<*>) : this(item) {
        this.container = container as KClass<Container>
        this.containerScreen = containerScreen as KClass<ContainerScreen<*>>
    }

    @Suppress("UNCHECKED_CAST")
    constructor(item: Item, bakedModel: BakedModel, container: KClass<*>, containerScreen: KClass<*>) : this(item) {
        this.bakedModel = bakedModel
        this.container = container as KClass<Container>
        this.containerScreen = containerScreen as KClass<ContainerScreen<*>>
    }

    open fun init(identifier: Identifier) {
        Registry.register(Registry.ITEM, identifier, item)
        if(container != null) {
            ContainerProviderRegistry.INSTANCE.registerFactory(identifier) { syncId: Int, _, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
                container!!.primaryConstructor!!.call(syncId,
                    playerEntity.inventory,
                    playerEntity.world,
                    packetByteBuf.readCompoundTag()!!
                )
            }
        }
    }

    open fun initClient(identifier: Identifier) {
        if(bakedModel != null) {
            ModelLoadingRegistry.INSTANCE.registerVariantProvider {
                ModelVariantProvider { modelIdentifier, _ ->
                    if(modelIdentifier.namespace == identifier.namespace && modelIdentifier.path == identifier.path) {
                        return@ModelVariantProvider object : UnbakedModel {
                            override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
                            override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings, modelId: Identifier) = bakedModel
                            override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>, unresolvedTextureReferences: MutableSet<Pair<String, String>>): MutableCollection<SpriteIdentifier> = mutableListOf()
                        }
                    }
                    return@ModelVariantProvider null
                }
            }
        }
        if(containerScreen != null) {
            ScreenProviderRegistry.INSTANCE.registerFactory(identifier) { syncId: Int, _, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
                containerScreen!!.primaryConstructor!!.call(
                    container!!.primaryConstructor!!.call(syncId,
                        playerEntity.inventory,
                        playerEntity.world,
                        packetByteBuf.readCompoundTag()
                    ), playerEntity.inventory, TranslatableText("screen.name.${identifier.path}")
                )
            }
        }
    }
}