package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.blocks.miscellaneous.Spikes
import net.minecraft.entity.LivingEntity

object SpikeHelper {

    private val set = linkedSetOf<LivingEntity>()

    fun shouldCancelLootDrop(entity: LivingEntity) = set.contains(entity)

    fun setSpike(entity: LivingEntity, type: Spikes.Type?) {
        if(type == Spikes.Type.STONE || type == Spikes.Type.GOLD) {
            set.add(entity)
        }else{
            set.remove(entity)
        }
    }

}