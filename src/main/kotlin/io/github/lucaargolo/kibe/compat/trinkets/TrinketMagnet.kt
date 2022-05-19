package io.github.lucaargolo.kibe.compat.trinkets

import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.Trinket
import dev.emi.trinkets.api.TrinketsApi
import io.github.lucaargolo.kibe.items.miscellaneous.Magnet
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

class TrinketMagnet(settings: Settings) : Magnet(settings), Trinket {
    init {
        TrinketsApi.registerTrinket(this, this)
    }

    override fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        inventoryTick(stack, entity.world, entity, -1, false)
    }

    override fun onEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        enable(stack)
    }

    override fun onUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        disable(stack)
    }
}