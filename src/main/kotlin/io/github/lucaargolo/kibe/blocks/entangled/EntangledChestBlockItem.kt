package io.github.lucaargolo.kibe.blocks.entangled

import net.minecraft.block.Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.TranslatableText
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity
import net.minecraft.world.World

class EntangledChestBlockItem(block: Block, settings: Settings): BlockItem(block, settings.rarity(Rarity.RARE)) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val tag = if(stack.hasTag() && stack.tag!!.contains("BlockEntityTag") ) {
            stack.orCreateTag.get("BlockEntityTag") as CompoundTag
        }else{
            val newTag = CompoundTag()
            newTag.putString("key", EntangledChest.DEFAULT_KEY)
            (1..8).forEach {
                newTag.putString("rune$it", DyeColor.WHITE.name)
            }
            newTag.putString("colorCode", "00000000")
            newTag
        }
        val ownerText = TranslatableText("tooltip.kibe.owner")
        if(tag.getString("key") != EntangledChest.DEFAULT_KEY) tooltip.add(ownerText.append(LiteralText(tag.getString("owner")).formatted(Formatting.GRAY)))
        val color = TranslatableText("tooltip.kibe.color")
        (0..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
            val text = LiteralText("■")
            text.style = text.style.withColor(TextColor.fromRgb(dc.materialColor.color))
            color.append(text)
        }
        tooltip.add(color)
    }


}