package io.github.lucaargolo.kibe.utils

import io.github.ladysnake.pal.*
import io.github.lucaargolo.kibe.MOD_ID
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

val ringAbilitySource: AbilitySource = Pal.getAbilitySource(Identifier("kibe", "ring"));
val potionToAbilityMap = mutableMapOf<PlayerAbility, StatusEffect>()

private fun registerEffect(playerAbility: PlayerAbility, status: StatusEffect): PlayerAbility {
    potionToAbilityMap.put(playerAbility, status)
    return playerAbility
}

val INFINITE_FIRE_RESISTENCE: PlayerAbility = registerEffect(Pal.registerAbility(Identifier(MOD_ID, "magma_ability")) { ability: PlayerAbility?, player: PlayerEntity? ->
    SimpleAbilityTracker(ability, player)
}, StatusEffects.FIRE_RESISTANCE)

val INFINITE_WATER_BREATHING: PlayerAbility = registerEffect(Pal.registerAbility(Identifier(MOD_ID, "water_ability")) { ability: PlayerAbility?, player: PlayerEntity? ->
    SimpleAbilityTracker(ability, player)
}, StatusEffects.WATER_BREATHING)


