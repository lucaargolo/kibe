package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.item.ArmorItem.Type
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.neoforged.neoforge.registries.DeferredHolder
import java.util.*
import java.util.function.Supplier

object ArmorMaterialCompendium: RegistryCompendium<ArmorMaterial>(Registries.ARMOR_MATERIAL) {

    val SLIME_BOOTS = register("slime_boots", Util.make(EnumMap(Type::class.java)) { map ->
        map[Type.BOOTS] = 2
        map[Type.LEGGINGS] = 5
        map[Type.CHESTPLATE] = 6
        map[Type.HELMET] = 2
        map[Type.BODY] = 5
    }, 9, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 0.0f) { Ingredient.ofItems(Items.SLIME_BALL) }

    private fun register(id: String, defense: EnumMap<Type, Int>, enchantability: Int, equipSound: RegistryEntry<SoundEvent>, toughness: Float, knockbackResistance: Float, repairIngredient: Supplier<Ingredient>): DeferredHolder<ArmorMaterial, ArmorMaterial> {
        val list = listOf(ArmorMaterial.Layer(Identifier.ofVanilla(id)))
        return register(id, defense, enchantability, equipSound, toughness, knockbackResistance, repairIngredient, list)
    }

    private fun register(id: String, defense: EnumMap<Type, Int>, enchantability: Int, equipSound: RegistryEntry<SoundEvent>, toughness: Float, knockbackResistance: Float, repairIngredient: Supplier<Ingredient>, layers: List<ArmorMaterial.Layer>): DeferredHolder<ArmorMaterial, ArmorMaterial> {
        val enumMap: EnumMap<Type, Int> = EnumMap(Type::class.java)

        for (type in Type.entries.toTypedArray()) {
            enumMap[type] = defense[type]
        }

        return register(id) { ArmorMaterial(enumMap, enchantability, equipSound, repairIngredient, layers, toughness, knockbackResistance) }
    }

}