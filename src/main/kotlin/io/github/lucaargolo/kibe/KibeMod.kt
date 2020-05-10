package io.github.lucaargolo.kibe

import io.github.lucaargolo.kibe.blocks.initBlocks
import io.github.lucaargolo.kibe.blocks.initBlocksClient
import io.github.lucaargolo.kibe.effects.CURSED_EFFECT
import io.github.lucaargolo.kibe.effects.initEffects
import io.github.lucaargolo.kibe.items.CURSED_DROPLETS
import io.github.lucaargolo.kibe.items.initItems
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.minecraft.container.PlayerContainer
import net.minecraft.entity.EntityType
import net.minecraft.loot.ConstantLootTableRange
import net.minecraft.loot.UniformLootTableRange
import net.minecraft.loot.condition.EntityPropertiesLootCondition
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.LootingEnchantLootFunction
import net.minecraft.predicate.entity.EntityEffectPredicate
import net.minecraft.predicate.entity.EntityPredicate
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

const val MOD_ID = "kibe"

@Suppress("unused")
fun init() {
    initBlocks()
    initItems()
    initEffects()
    initLootTables()
}

@Suppress("unused")
fun initClient() {
    initBlocksClient()
    initTexturesClient()
}

fun initTexturesClient() {
    @Suppress("deprecated")
    ClientSpriteRegistryCallback.event(PlayerContainer.BLOCK_ATLAS_TEXTURE).register(ClientSpriteRegistryCallback { _, registry ->
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
