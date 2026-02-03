package io.github.lucaargolo.kibe.compat.trinkets

import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.Trinket
import dev.emi.trinkets.api.TrinketsApi
import io.github.ladysnake.pal.PlayerAbility
import io.github.lucaargolo.kibe.items.miscellaneous.AbilityRing
import io.github.lucaargolo.kibe.mixed.PlayerEntityMixed
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

class TrinketAbilityRing(settings: Settings, ability: PlayerAbility) : AbilityRing(settings, ability), Trinket {

    init {
        io.github.lucaargolo.kibe.LOGGER.info("creating Trinket AbilityRing for ${ability.id}")
        TrinketsApi.registerTrinket(this, this)
    }

    override fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        if(!entity.world.isClient) {
            (entity as? PlayerEntityMixed)?.let {
                if (isBroken(stack)) { // IF ITS BROKEN FIX IT
                    unbroken(stack)
                    enable(stack)
                }

                try {
                    it.kibe_activeRingsList.removeAll { pair -> pair.second != entity.world.time }
                } catch (_: Exception) { }
                it.kibe_activeRingsList.add(Pair(stack, entity.world.time))

                val tag = stack.orCreateNbt
                val currentDimension = entity.world.dimension.toString() // CURRENT DIMENSION UPDATE
                val storedDimension = tag.getString("dimension") // PREVIOUS DIMENSION STORED

                if (currentDimension != storedDimension) { // PLAYER CHANGED DIMENSIONS REEE
                    broken(stack) // THIS CRAP BROKEN
                    disable(stack)
                    tag.putString("dimension", currentDimension) // STORE NEW DIMENSION
                }
            }
        }
    }

    override fun onEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        enable(stack)
    }

    override fun onUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        disable(stack)
    }

}

