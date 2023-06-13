package io.github.lucaargolo.kibe.effects

import io.github.lucaargolo.kibe.MOD_ID
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.util.Identifier
import net.minecraft.registry.Registry
import net.minecraft.registry.Registries

val effectRegistry = mutableMapOf<Identifier, StatusEffect>()

val CURSED_EFFECT = register(Identifier(MOD_ID, "cursed_effect"), CursedEffect())

private fun register(identifier: Identifier, effect: StatusEffect): StatusEffect {
    effectRegistry[identifier] = effect
    return effect
}

fun initEffects() {
    effectRegistry.forEach{ Registry.register(Registries.STATUS_EFFECT, it.key, it.value) }
}