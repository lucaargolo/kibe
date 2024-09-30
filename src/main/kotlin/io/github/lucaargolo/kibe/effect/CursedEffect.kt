package io.github.lucaargolo.kibe.effect

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier

class CursedEffect: StatusEffect(StatusEffectCategory.HARMFUL, 3484199) {

    init {
        addAttributeModifier(
            EntityAttributes.GENERIC_MOVEMENT_SPEED,
            Identifier.ofVanilla("effect.speed"),
            1.0,
            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        )
        addAttributeModifier(
            EntityAttributes.GENERIC_ATTACK_SPEED,
            Identifier.ofVanilla("effect.haste"),
            1.0,
            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        )
        addAttributeModifier(
            EntityAttributes.GENERIC_ATTACK_DAMAGE,
            Identifier.ofVanilla("effect.strength"),
            1.0,
            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        )
    }

    override fun onApplied(entity: LivingEntity, amplifier: Int) {
        if(entity is PlayerEntity) {
            entity.removeStatusEffect(EffectCompendium.CURSED)
        }else{
            entity.absorptionAmount += entity.health
            super.onApplied(entity, amplifier)
        }
    }

}