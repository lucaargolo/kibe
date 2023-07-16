package io.github.lucaargolo.kibe.compat.trinkets

import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.Trinket
import dev.emi.trinkets.api.TrinketsApi
import io.github.ladysnake.pal.PlayerAbility
import io.github.lucaargolo.kibe.MOD_NAME
import io.github.lucaargolo.kibe.items.miscellaneous.AbilityRing
import io.github.lucaargolo.kibe.mixed.PlayerEntityMixed
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

class TrinketAbilityRing(settings: Settings, ability: PlayerAbility) : AbilityRing(settings, ability), Trinket {

    init {
        io.github.lucaargolo.kibe.LOGGER.info("[$MOD_NAME] Creating Trinket AbilityRing for ${ability.id}")
        TrinketsApi.registerTrinket(this, this)
    }

    override fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        if(!entity.world.isClient) {
            (entity as? PlayerEntityMixed)?.let {
                try {
                    it.kibe_activeRingsList.removeAll { pair -> pair.second != entity.world.time }
                } catch (_: Exception) { }
                it.kibe_activeRingsList.add(Pair(stack, entity.world.time))
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

