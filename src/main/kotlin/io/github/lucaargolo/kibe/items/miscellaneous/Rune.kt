package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.items.WHITE_RUNE
import io.github.lucaargolo.kibe.items.itemRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

import net.minecraft.util.DyeColor
import net.minecraft.world.World

class Rune(val color: DyeColor, settings: Settings): Item(settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip.add(Text.translatable("tooltip.kibe.lore.rune"))
    }

    companion object {
        fun getRuneByColor(color: DyeColor): Rune {
            itemRegistry.forEach { (_, modItem) -> if(modItem.item is Rune && modItem.item.color == color) return modItem.item }
            return WHITE_RUNE as Rune
        }
    }

}