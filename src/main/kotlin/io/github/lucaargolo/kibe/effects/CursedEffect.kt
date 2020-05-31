package io.github.lucaargolo.kibe.effects

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectType
import net.minecraft.entity.player.PlayerEntity

class CursedEffect: StatusEffect(StatusEffectType.HARMFUL, 3484199) {

    init {
        addAttributeModifier(
            EntityAttributes.GENERIC_MOVEMENT_SPEED,
            "91AEAA56-376B-4498-935B-2F7F68070635",
            1.0,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
        addAttributeModifier(
            EntityAttributes.GENERIC_ATTACK_SPEED,
            "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3",
            1.0,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
        addAttributeModifier(
            EntityAttributes.GENERIC_ATTACK_DAMAGE,
            "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9",
            1.0,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
    }


    override fun onApplied(entity: LivingEntity, attributes: AttributeContainer?, amplifier: Int) {
        if(entity is PlayerEntity) {
            entity.removeStatusEffect(this)
        }else{
            entity.absorptionAmount = entity.absorptionAmount + entity.health
            super.onApplied(entity, attributes, amplifier)
        }
    }



}