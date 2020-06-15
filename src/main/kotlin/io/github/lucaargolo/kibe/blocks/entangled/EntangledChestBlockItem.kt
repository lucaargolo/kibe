package io.github.lucaargolo.kibe.blocks.entangled

import net.minecraft.block.Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
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
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
            val text = LiteralText("${getFormattingFromDye(dc)}■")
            color.append(text)
        }
        tooltip.add(color)
    }

    companion object {
        fun getFormattingFromDye(color: DyeColor): Formatting {
            return when(color) {
                DyeColor.BLACK -> Formatting.BLACK
                DyeColor.RED -> Formatting.DARK_RED
                DyeColor.GREEN -> Formatting.DARK_GREEN
                DyeColor.BROWN -> Formatting.GOLD
                DyeColor.PURPLE -> Formatting.DARK_PURPLE
                DyeColor.MAGENTA -> Formatting.LIGHT_PURPLE
                DyeColor.BLUE -> Formatting.DARK_BLUE
                DyeColor.LIGHT_BLUE -> Formatting.BLUE
                DyeColor.CYAN -> Formatting.BLUE
                DyeColor.LIGHT_GRAY -> Formatting.GRAY
                DyeColor.GRAY -> Formatting.DARK_GRAY
                DyeColor.PINK -> Formatting.RED
                DyeColor.LIME -> Formatting.GREEN
                DyeColor.YELLOW -> Formatting.YELLOW
                DyeColor.ORANGE -> Formatting.GOLD
                else -> Formatting.WHITE
            }
        }
    }
}