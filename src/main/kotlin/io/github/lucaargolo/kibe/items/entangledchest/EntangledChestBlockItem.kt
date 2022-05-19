package io.github.lucaargolo.kibe.items.entangledchest

import io.github.lucaargolo.kibe.blocks.ENTANGLED_CHEST
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChest
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

import net.minecraft.text.Text
import net.minecraft.text.TextColor

import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity
import net.minecraft.world.World

class EntangledChestBlockItem(settings: Settings): BlockItem(ENTANGLED_CHEST, settings.rarity(Rarity.RARE)) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val tag = if(stack.hasNbt() && stack.nbt!!.contains("BlockEntityTag") ) {
            stack.orCreateNbt.get("BlockEntityTag") as NbtCompound
        }else{
            val newTag = NbtCompound()
            newTag.putString("key", EntangledChest.DEFAULT_KEY)
            (1..8).forEach {
                newTag.putString("rune$it", DyeColor.WHITE.name)
            }
            newTag.putString("colorCode", "00000000")
            newTag
        }
        val ownerText = Text.translatable("tooltip.kibe.owner")
        if(tag.getString("key") != EntangledChest.DEFAULT_KEY) tooltip.add(ownerText.append(Text.literal(tag.getString("owner")).formatted(Formatting.GRAY)))
        val color = Text.translatable("tooltip.kibe.color")
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE
            val text = Text.literal("■")
            text.style = text.style.withColor(TextColor.fromRgb(dc.mapColor.color))
            color.append(text)
        }
        tooltip.add(color)
    }


}