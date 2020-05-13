package io.github.lucaargolo.kibe

import com.mojang.datafixers.util.Pair
import io.github.lucaargolo.kibe.blocks.initBlocks
import io.github.lucaargolo.kibe.blocks.initBlocksClient
import io.github.lucaargolo.kibe.effects.CURSED_EFFECT
import io.github.lucaargolo.kibe.effects.initEffects
import io.github.lucaargolo.kibe.items.CURSED_DROPLETS
import io.github.lucaargolo.kibe.items.ENTANGLED_BAG
import io.github.lucaargolo.kibe.items.entangled.EntangledBagBakedModel
import io.github.lucaargolo.kibe.items.entangled.EntangledBagContainer
import io.github.lucaargolo.kibe.items.entangled.EntangledBagScreen
import io.github.lucaargolo.kibe.items.getItemId
import io.github.lucaargolo.kibe.items.initItems
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.container.PlayerContainer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.loot.ConstantLootTableRange
import net.minecraft.loot.UniformLootTableRange
import net.minecraft.loot.condition.EntityPropertiesLootCondition
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.LootingEnchantLootFunction
import net.minecraft.predicate.entity.EntityEffectPredicate
import net.minecraft.predicate.entity.EntityPredicate
import net.minecraft.resource.ResourceManager
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import java.util.function.Consumer
import java.util.function.Function


const val MOD_ID = "kibe"

@Suppress("unused")
fun init() {
    initBlocks()
    initItems()
    initEffects()
    initLootTables()
    ContainerProviderRegistry.INSTANCE.registerFactory(getItemId(ENTANGLED_BAG)) { syncId: Int, _, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
        EntangledBagContainer(syncId,
            playerEntity.inventory,
            playerEntity.world,
            packetByteBuf.readCompoundTag()!!
        )
    }
}

@Suppress("unused")
fun initClient() {
    initBlocksClient()
    initTexturesClient()
    ModelLoadingRegistry.INSTANCE.registerAppender { manager: ResourceManager?, out: Consumer<ModelIdentifier?> ->
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_background"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_ring"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_gold_core"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_diamond_core"), "inventory"))
    }

    ScreenProviderRegistry.INSTANCE.registerFactory(getItemId(ENTANGLED_BAG)) { syncId: Int, _, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
        EntangledBagScreen(
            EntangledBagContainer(syncId,
                playerEntity.inventory,
                playerEntity.world,
                packetByteBuf.readCompoundTag()!!
            ), playerEntity.inventory, LiteralText("Entangled Bag")
        )
    }

    ModelLoadingRegistry.INSTANCE.registerVariantProvider { resourceManager ->
        ModelVariantProvider { modelIdentifier, modelProviderContext ->
            if(modelIdentifier.namespace == MOD_ID && modelIdentifier.path == "entangled_bag") {
                return@ModelVariantProvider object : UnbakedModel {
                    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
                    override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings, modelId: Identifier) = EntangledBagBakedModel()
                    override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>, unresolvedTextureReferences: MutableSet<Pair<String, String>>): MutableCollection<SpriteIdentifier> = mutableListOf()
                }
            }
            return@ModelVariantProvider null
        }
    }
}

fun initTexturesClient() {
    @Suppress("deprecated")
    ClientSpriteRegistryCallback.event(PlayerContainer.BLOCK_ATLAS_TEXTURE).register(ClientSpriteRegistryCallback { _, registry ->
        registry.register(Identifier(MOD_ID, "block/entangled_chest_runes"))
        (0..15).forEach{
            registry.register(Identifier(MOD_ID, "block/redstone_timer_$it"))
        }
    })
}

fun initLootTables() {
    //Add cursed droplets drop to mobs with the cursed effect
    LootTableLoadingCallback.EVENT.register(LootTableLoadingCallback { _, _, id: Identifier, supplier: FabricLootSupplierBuilder, _ ->
        if (id.toString().startsWith("minecraft:entities")) {
            val poolBuilder = FabricLootPoolBuilder.builder()
                .withRolls(ConstantLootTableRange.create(1))
                .withEntry(ItemEntry.builder(CURSED_DROPLETS))
                .withCondition(
                    EntityPropertiesLootCondition.builder(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.create().effects(EntityEffectPredicate.create().withEffect(CURSED_EFFECT))
                    ))
                .withCondition(RandomChanceLootCondition.builder(0.25F))
                .withFunction(LootingEnchantLootFunction.builder(UniformLootTableRange.between(0f,1.5f)).build())
            supplier.withPool(poolBuilder)
        }
    })
    //Add cursed droplets to wither skeletons
    LootTableLoadingCallback.EVENT.register(LootTableLoadingCallback { _, _, id: Identifier, supplier: FabricLootSupplierBuilder, _ ->
        if (id.toString() == "minecraft:entities/wither_skeleton") {
            val poolBuilder = FabricLootPoolBuilder.builder()
                .withRolls(ConstantLootTableRange.create(1))
                .withEntry(ItemEntry.builder(CURSED_DROPLETS))
                .withCondition(RandomChanceLootCondition.builder(0.5F))
                .withFunction(LootingEnchantLootFunction.builder(UniformLootTableRange.between(0f,1.5f)).build())
            supplier.withPool(poolBuilder)
        }
    })
}
