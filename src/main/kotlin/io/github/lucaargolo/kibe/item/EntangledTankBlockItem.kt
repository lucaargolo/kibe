package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.block.EntangledTank
import io.github.lucaargolo.kibe.data.state.EntangledTankState
import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity

class EntangledTankBlockItem(settings: Settings): BlockItem(BlockCompendium.ENTANGLED_TANK, settings.rarity(Rarity.RARE)) {

    override fun appendTooltip(stack: ItemStack, context: TooltipContext?, tooltip: MutableList<Text>, type: TooltipType?) {
        super.appendTooltip(stack, context, tooltip, type)
        val tag = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA)?.copyNbt() ?: let{
            val newTag = NbtCompound()
            newTag.putString("key", EntangledTank.DEFAULT_KEY)
            (1..8).forEach {
                newTag.putString("rune$it", DyeColor.WHITE.name)
            }
            newTag.putString("colorCode", "00000000")
            newTag
        }
        val ownerText = Text.translatable("tooltip.kibe.owner")
        if(tag.getString("key") != EntangledTank.DEFAULT_KEY) tooltip.add(ownerText.append(Text.literal(tag.getString("owner")).formatted(Formatting.GRAY)))
        val color = Text.translatable("tooltip.kibe.color")
        var colorCode = ""
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE
            colorCode += dc.id.let { int -> Integer.toHexString(int) }
            val text = Text.literal("■")
            text.style = text.style.withColor(TextColor.fromRgb(dc.mapColor.color))
            color.append(text)
        }
        tooltip.add(color)
        val key = tag.getString("key")
        EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.add(Pair(key, colorCode))
        val fluidInv = EntangledTankState.CLIENT_STATES[key]?.fluidInvMap?.get(colorCode) ?: object: SingleVariantStorage<FluidVariant>() {
            override fun getCapacity(variant: FluidVariant?) = 0L
            override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
        }
        FluidHelper.writeTank(tag, fluidInv)
        if(!fluidInv.isResourceBlank)
            tooltip.add(FluidVariantAttributes.getName(fluidInv.variant).copyContentOnly().append(Text.literal(": ${Formatting.GRAY}${FluidHelper.getMb(fluidInv.amount)}mB")))
    }

}