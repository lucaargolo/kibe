package io.github.lucaargolo.kibe.utils.helper

import io.github.lucaargolo.kibe.effect.EffectCompendium
import io.github.lucaargolo.kibe.item.ItemCompendium
import net.fabricmc.fabric.api.loot.v3.LootTableEvents
import net.minecraft.loot.LootPool
import net.minecraft.loot.condition.EntityPropertiesLootCondition
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.EnchantedCountIncreaseLootFunction
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.loot.provider.number.UniformLootNumberProvider
import net.minecraft.predicate.entity.EntityEffectPredicate
import net.minecraft.predicate.entity.EntityPredicate

object LootHelper {

    fun initialize() {
        addCursedDroplets()
    }

    fun addCursedDroplets() {
        //Add cursed droplets drop to mobs with the cursed effect
        LootTableEvents.MODIFY.register { key, builder, _, lookup ->
            if (key.value.toString().startsWith("minecraft:entities")) {

                val poolBuilder = LootPool.Builder()
                    .rolls(ConstantLootNumberProvider.create(1f))
                    .with(ItemEntry.builder(ItemCompendium.CURSED_DROPLETS))
                    .conditionally(
                        EntityPropertiesLootCondition.builder(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.create()
                                .effects(EntityEffectPredicate.Builder.create().addEffect(EffectCompendium.CURSED))
                        )
                    )
                    .conditionally(RandomChanceLootCondition.builder(0.05F))
                    .apply(EnchantedCountIncreaseLootFunction.builder(lookup, UniformLootNumberProvider.create(0f, 1.5f)))
                builder.pool(poolBuilder)
            }
        }
        //Add cursed droplets to wither skeletons
        LootTableEvents.MODIFY.register { key, builder, _, lookup ->
            if (key.value.toString() == "minecraft:entities/wither_skeleton") {
                val poolBuilder = LootPool.Builder()
                    .rolls(ConstantLootNumberProvider.create(1f))
                    .with(ItemEntry.builder(ItemCompendium.CURSED_DROPLETS))
                    .conditionally(RandomChanceLootCondition.builder(0.1F))
                    .apply(EnchantedCountIncreaseLootFunction.builder(lookup, UniformLootNumberProvider.create(0f, 1.5f)))
                builder.pool(poolBuilder)
            }
        }
    }

}