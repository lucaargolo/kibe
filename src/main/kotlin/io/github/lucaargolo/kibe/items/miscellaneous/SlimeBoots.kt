package io.github.lucaargolo.kibe.items.miscellaneous

import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundEvents

class SlimeBoots(settings: Settings) : ArmorItem(object : ArmorMaterial {
    override fun getRepairIngredient() = Ingredient.ofItems(Items.SLIME_BALL)
    override fun getToughness() = 1.0F
    override fun getKnockbackResistance() = 0F
    override fun getEquipSound() = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC
    override fun getName() = "slime"
    override fun getDurability(type: Type) = 128
    override fun getEnchantability() = 9
    override fun getProtection(type: Type) = 2
}, Type.BOOTS, settings)