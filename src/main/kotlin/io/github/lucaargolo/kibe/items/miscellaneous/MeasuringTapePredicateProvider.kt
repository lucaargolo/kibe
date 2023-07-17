package io.github.lucaargolo.kibe.items.miscellaneous

import net.minecraft.client.item.ClampedModelPredicateProvider
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

class MeasuringTapePredicateProvider: ClampedModelPredicateProvider {

    override fun unclampedCall(stack: ItemStack, world: ClientWorld?, entity: LivingEntity?, seed: Int): Float {
        val nbt = stack.nbt ?: return 0f
        if (MeasuringTape.MEASURING_LEVEL in nbt) {
            return 1f
        }
        return 0f
    }


}