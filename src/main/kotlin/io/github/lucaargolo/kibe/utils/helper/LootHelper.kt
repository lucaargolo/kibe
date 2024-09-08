package io.github.lucaargolo.kibe.utils.helper

import io.github.lucaargolo.kibe.effect.EffectCompendium
import io.github.lucaargolo.kibe.item.ItemCompendium
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.minecraft.loot.LootPool
import net.minecraft.loot.condition.EntityPropertiesLootCondition
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.LootingEnchantLootFunction
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
        LootTableEvents.MODIFY.register { _, _, id, supplier, _ ->
            if (id.toString().startsWith("minecraft:entities")) {
                val poolBuilder = LootPool.Builder()
                    .rolls(ConstantLootNumberProvider.create(1f))
                    .with(ItemEntry.builder(ItemCompendium.CURSED_DROPLETS))
                    .conditionally(
                        EntityPropertiesLootCondition.builder(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.create()
                                .effects(EntityEffectPredicate.create().withEffect(EffectCompendium.CURSED))
                        )
                    )
                    .conditionally(RandomChanceLootCondition.builder(0.05F))
                    .apply(LootingEnchantLootFunction.builder(UniformLootNumberProvider.create(0f, 1.5f)))
                supplier.pool(poolBuilder)
            }
        }
        //Add cursed droplets to wither skeletons
        LootTableEvents.MODIFY.register { _, _, id, supplier, _ ->
            if (id.toString() == "minecraft:entities/wither_skeleton") {
                val poolBuilder = LootPool.Builder()
                    .rolls(ConstantLootNumberProvider.create(1f))
                    .with(ItemEntry.builder(ItemCompendium.CURSED_DROPLETS))
                    .conditionally(RandomChanceLootCondition.builder(0.1F))
                    .apply(LootingEnchantLootFunction.builder(UniformLootNumberProvider.create(0f, 1.5f)))
                supplier.pool(poolBuilder)
            }
        }
    }

}