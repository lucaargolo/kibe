package io.github.lucaargolo.kibe.items.entangledtank

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTank
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.TranslatableText
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity
import net.minecraft.world.World

class EntangledTankBlockItem(settings: Settings): BlockItem(ENTANGLED_TANK, settings.rarity(Rarity.RARE)) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val tag = if(stack.hasNbt() && stack.nbt!!.contains("BlockEntityTag") ) {
            stack.orCreateNbt.get("BlockEntityTag") as NbtCompound
        }else{
            val newTag = NbtCompound()
            newTag.putString("key", EntangledTank.DEFAULT_KEY)
            (1..8).forEach {
                newTag.putString("rune$it", DyeColor.WHITE.name)
            }
            newTag.putString("colorCode", "00000000")
            newTag
        }
        val ownerText = TranslatableText("tooltip.kibe.owner")
        if(tag.getString("key") != EntangledTank.DEFAULT_KEY) tooltip.add(ownerText.append(LiteralText(tag.getString("owner")).formatted(Formatting.GRAY)))
        val color = TranslatableText("tooltip.kibe.color")
        var colorCode = ""
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
            colorCode += dc.id.let { int -> Integer.toHexString(int) }
            val text = LiteralText("■")
            text.style = text.style.withColor(TextColor.fromRgb(dc.mapColor.color))
            color.append(text)
        }
        tooltip.add(color)
        val key = tag.getString("key")
        EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.add(Pair(key, colorCode))
        val fluidInv = EntangledTankState.CLIENT_STATES[key]?.fluidInvMap?.get(colorCode) ?: SimpleFixedFluidInv(1, FluidAmount.ONE)
        fluidInv.toTag(tag)
        if(!fluidInv.getInvFluid(0).isEmpty)
            tooltip.add(fluidInv.getInvFluid(0).fluidKey.name.shallowCopy().append(LiteralText(": ${Formatting.GRAY}${fluidInv.getInvFluid(0).amount().asInt(1000)}mB")))
    }

}