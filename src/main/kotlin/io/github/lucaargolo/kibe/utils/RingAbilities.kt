package io.github.lucaargolo.kibe.utils

import io.github.ladysnake.pal.AbilitySource
import io.github.ladysnake.pal.Pal
import io.github.ladysnake.pal.PlayerAbility
import io.github.ladysnake.pal.SimpleAbilityTracker
import io.github.lucaargolo.kibe.MOD_ID
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier

val ringAbilitySource: AbilitySource = Pal.getAbilitySource(Identifier("kibe", "ring"))
val potionToAbilityMap = mutableMapOf<PlayerAbility, StatusEffect>()

private fun registerEffect(playerAbility: PlayerAbility, status: StatusEffect): PlayerAbility {
    potionToAbilityMap[playerAbility] = status
    return playerAbility
}

val INFINITE_FIRE_RESISTENCE: PlayerAbility = registerEffect(Pal.registerAbility(Identifier(MOD_ID, "magma_ability")) { ability: PlayerAbility?, player: PlayerEntity? ->
    SimpleAbilityTracker(ability, player)
}, StatusEffects.FIRE_RESISTANCE)

val INFINITE_WATER_BREATHING: PlayerAbility = registerEffect(Pal.registerAbility(Identifier(MOD_ID, "water_ability")) { ability: PlayerAbility?, player: PlayerEntity? ->
    SimpleAbilityTracker(ability, player)
}, StatusEffects.WATER_BREATHING)


