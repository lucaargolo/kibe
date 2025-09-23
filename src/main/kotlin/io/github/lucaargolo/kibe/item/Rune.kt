package io.github.lucaargolo.kibe.item

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.DyeColor

class Rune(val color: DyeColor, settings: Settings): Item(settings) {

    override fun appendTooltip(stack: ItemStack?, context: TooltipContext?, tooltip: MutableList<Text>, type: TooltipType?) {
        super.appendTooltip(stack, context, tooltip, type)
        tooltip.add(Text.translatable("tooltip.kibe.lore.rune"))
    }

    companion object {
        fun getRuneByColor(color: DyeColor): Rune {
            return ItemCompendium.RUNES.firstOrNull{it.color == color} ?: ItemCompendium.WHITE_RUNE
        }
    }

}