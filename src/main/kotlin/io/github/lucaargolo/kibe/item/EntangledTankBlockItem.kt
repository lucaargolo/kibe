package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.block.EntangledTank
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import io.github.lucaargolo.kibe.item.EntangledBucket.Companion.getFluidInv
import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity

class EntangledTankBlockItem(settings: Settings): BlockItem(BlockCompendium.ENTANGLED_TANK, settings.rarity(Rarity.RARE)) {

    override fun appendTooltip(stack: ItemStack, context: TooltipContext?, tooltip: MutableList<Text>, type: TooltipType?) {
        super.appendTooltip(stack, context, tooltip, type)
        val ownerText = Text.translatable("tooltip.kibe.owner")
        val key = stack.get(ComponentTypeCompendium.ENTANGLED_KEY) ?: EntangledTank.DEFAULT_KEY
        if(key != EntangledTank.DEFAULT_KEY && stack.contains(ComponentTypeCompendium.OWNER))
            tooltip.add(ownerText.append(Text.literal(stack.get(ComponentTypeCompendium.OWNER)).formatted(Formatting.GRAY)))
        val color = Text.translatable("tooltip.kibe.color")
        var colorCode = ""
        if(stack.contains(ComponentTypeCompendium.RUNE_SET)) {
            stack.get(ComponentTypeCompendium.RUNE_SET)?.forEach { dc ->
                colorCode += dc.id.let { int -> Integer.toHexString(int) }
                val text = Text.literal("■")
                text.style = text.style.withColor(TextColor.fromRgb(dc.mapColor.color))
                color.append(text)
            }
        }else{
            colorCode = "00000000"
            color.append(Text.literal("■■■■■■■■"))
        }
        tooltip.add(color)
        val fluidInv = getFluidInv(null, key, colorCode)
        if(!fluidInv.isResourceBlank)
            tooltip.add(FluidVariantAttributes.getName(fluidInv.variant).copyContentOnly().append(Text.literal(": ${Formatting.GRAY}${FluidHelper.getMb(fluidInv.amount)}mB")))

    }

}