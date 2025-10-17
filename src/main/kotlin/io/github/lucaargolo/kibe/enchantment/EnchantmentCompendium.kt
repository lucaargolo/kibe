package io.github.lucaargolo.kibe.enchantment

import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

object EnchantmentCompendium {

    val SLIMY: RegistryKey<Enchantment> = of("slimy")

    private fun of(path: String): RegistryKey<Enchantment> {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, ModIdentifier.of(path))
    }

}