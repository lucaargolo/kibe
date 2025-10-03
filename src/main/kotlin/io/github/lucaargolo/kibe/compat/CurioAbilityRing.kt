package io.github.lucaargolo.kibe.compat

import io.github.ladysnake.pal.PlayerAbility
import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.item.AbilityRing
import net.minecraft.item.ItemStack
import top.theillusivec4.curios.api.CuriosApi
import top.theillusivec4.curios.api.SlotContext
import top.theillusivec4.curios.api.type.capability.ICurioItem

class CurioAbilityRing(settings: Settings, ability: PlayerAbility) : AbilityRing(settings, ability), ICurioItem {

    init {
        KibeMod.LOGGER.info("[${KibeMod.MOD_NAME}] Creating Trinket AbilityRing for ${ability.id}")
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

