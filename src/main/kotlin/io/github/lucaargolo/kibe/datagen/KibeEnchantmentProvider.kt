package io.github.lucaargolo.kibe.datagen

import io.github.lucaargolo.kibe.enchantment.EnchantmentCompendium
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition
import net.minecraft.component.type.AttributeModifierSlot
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.ItemTags
import java.util.concurrent.CompletableFuture

class KibeEnchantmentProvider(output: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>) : FabricDynamicRegistryProvider(output, registryLookup) {

    override fun configure(registryLookup: RegistryWrapper.WrapperLookup, entries: Entries) {
        register(entries, EnchantmentCompendium.SLIMY, Enchantment.builder(
            Enchantment.definition(
                registryLookup.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                2, 1, Enchantment.leveledCost(10, 20), Enchantment.leveledCost(60, 20), 4, AttributeModifierSlot.FEET
            )
        ))
    }

    private fun register(entries: Entries, key: RegistryKey<Enchantment>, builder: Enchantment.Builder, vararg conditions: ResourceCondition) {
        entries.add(key, builder.build(key.getValue()), *conditions)
    }

    override fun getName() = "KibeEnchantmentProvider"
}