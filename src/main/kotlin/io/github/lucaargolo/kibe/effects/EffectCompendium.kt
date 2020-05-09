package io.github.lucaargolo.kibe.effects

import io.github.lucaargolo.kibe.MOD_ID
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

private val registry = mutableMapOf<Identifier, StatusEffect>()

val CURSED_EFFECT = register(Identifier(MOD_ID, "cursed_effect"), CursedEffect())

private fun register(identifier: Identifier, effect: StatusEffect): StatusEffect {
    registry[identifier] = effect
    return effect
}

fun initEffects() {
    registry.forEach{ Registry.register(Registry.STATUS_EFFECT, it.key, it.value) }
}