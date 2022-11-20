package io.github.lucaargolo.kibe.utils

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.text.Text

open class SpikeDamageSource(name: String = "Spikes"): DamageSource(name) {
    companion object {
        val INSTANCE = SpikeDamageSource()
    }
    override fun getDeathMessage(entity: LivingEntity?): Text {
        return Text.of("${entity?.name ?: "unknown"} got spiked to death.")
    }

    class DiamondSpikeDamageSource: SpikeDamageSource("Diamond Spikes") {
        companion object {
            val INSTANCE = DiamondSpikeDamageSource()
        }
        override fun getDeathMessage(entity: LivingEntity?): Text {
            return Text.of("${entity?.name ?: "unknown"} got fancily spiked to death.")
        }
    }
}