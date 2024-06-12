package io.github.lucaargolo.kibe.utils.helper

import io.github.ladysnake.pal.*
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import java.util.function.BiFunction

object AbilityHelper {

    val ABILITY_TO_EFFECT = mutableMapOf<PlayerAbility, StatusEffect>()

    val RING_SOURCE: AbilitySource = Pal.getAbilitySource(Identifier("kibe", "ring"))

    val INFINITE_FIRE_RESISTENCE = register("magma_ability", ::SimpleAbilityTracker, StatusEffects.FIRE_RESISTANCE)
    val INFINITE_WATER_BREATHING = register("water_ability", ::SimpleAbilityTracker, StatusEffects.WATER_BREATHING)

    private fun register(identifier: String, factory: BiFunction<PlayerAbility, PlayerEntity, AbilityTracker>, status: StatusEffect): PlayerAbility {
        val playerAbility = register(identifier, factory)
        ABILITY_TO_EFFECT[playerAbility] = status
        return playerAbility
    }

    fun register(identifier: String, factory: BiFunction<PlayerAbility, PlayerEntity, AbilityTracker>): PlayerAbility {
        return Pal.registerAbility(ModIdentifier(identifier), factory)
    }

}




