package io.github.lucaargolo.kibe.compat.trinkets

import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.Trinket
import dev.emi.trinkets.api.TrinketsApi
import io.github.lucaargolo.kibe.items.miscellaneous.Magnet
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

class TrinketMagnet(settings: Settings) : Magnet(settings), Trinket {
    init {
        TrinketsApi.registerTrinket(this, this)
    }

    override fun tick(stack: ItemStack?, slot: SlotReference?, entity: LivingEntity?) {
        if (stack == null || slot == null || entity !is PlayerEntity) return
        inventoryTick(stack, entity.entityWorld, entity, slot.index, false)
    }

    override fun onEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        enable(stack)
    }

    override fun onUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        disable(stack)
    }
}