package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.block.EntangledChest
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity

class EntangledChestBlockItem(settings: Settings): BlockItem(BlockCompendium.ENTANGLED_CHEST, settings.rarity(Rarity.RARE)) {

    override fun appendTooltip(stack: ItemStack, context: TooltipContext?, tooltip: MutableList<Text>, type: TooltipType?) {
        super.appendTooltip(stack, context, tooltip, type)
        val ownerText = Text.translatable("tooltip.kibe.owner")
        val key = stack.get(ComponentTypeCompendium.ENTANGLED_KEY) ?: EntangledChest.DEFAULT_KEY
        if(key != EntangledChest.DEFAULT_KEY && stack.contains(ComponentTypeCompendium.OWNER))
            tooltip.add(ownerText.append(Text.literal(stack.get(ComponentTypeCompendium.OWNER)).formatted(Formatting.GRAY)))
        val color = Text.translatable("tooltip.kibe.color")
        (stack.get(ComponentTypeCompendium.RUNE_SET) ?: KibeMod.DEFAULT_RUNE_SET).forEach { dc ->
            val text = Text.literal("■")
            text.style = text.style.withColor(TextColor.fromRgb(dc.mapColor.color))
            color.append(text)
        }
        tooltip.add(color)
    }

}