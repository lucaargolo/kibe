package io.github.lucaargolo.kibe.effect

import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.registry.Registries
import net.minecraftforge.registries.ForgeRegistries

object EffectCompendium : RegistryCompendium<StatusEffect>(ForgeRegistries.MOB_EFFECTS) {

    val CURSED by register("cursed_effect", { CursedEffect() })

}

