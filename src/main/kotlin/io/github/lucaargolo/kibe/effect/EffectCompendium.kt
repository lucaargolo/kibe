package io.github.lucaargolo.kibe.effect

import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.registry.Registries

object EffectCompendium : RegistryCompendium<StatusEffect>(Registries.STATUS_EFFECT) {

    val CURSED = registerReference("cursed_effect", CursedEffect())

}

