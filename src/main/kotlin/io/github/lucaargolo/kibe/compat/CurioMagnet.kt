package io.github.lucaargolo.kibe.compat

import io.github.lucaargolo.kibe.item.Magnet
import net.minecraft.item.ItemStack
import top.theillusivec4.curios.api.CuriosApi
import top.theillusivec4.curios.api.SlotContext
import top.theillusivec4.curios.api.type.capability.ICurioItem


class CurioMagnet(settings: Settings) : Magnet(settings), ICurioItem {

    init {
        CuriosApi.registerCurio(this, this)
    }

    override fun onEquip(slotContext: SlotContext, prevStack: ItemStack, stack: ItemStack) {
        super.onEquip(slotContext, prevStack, stack)
        enable(stack)
    }

    override fun onUnequip(slotContext: SlotContext, newStack: ItemStack, stack: ItemStack) {
        super.onUnequip(slotContext, newStack, stack)
        disable(stack)
    }

}