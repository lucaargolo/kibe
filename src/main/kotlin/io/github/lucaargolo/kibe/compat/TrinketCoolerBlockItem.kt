package io.github.lucaargolo.kibe.compat

import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.Trinket
import dev.emi.trinkets.api.TrinketsApi
import io.github.lucaargolo.kibe.item.CoolerBlockItem
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

class TrinketCoolerBlockItem(settings: Settings) : CoolerBlockItem(settings), Trinket {

    init {
        TrinketsApi.registerTrinket(this, this)
    }

    override fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        inventoryTick(stack, entity.world, entity, -1, false)
    }

}

