package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.ladysnake.pal.PlayerAbility
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class AbilityRing(settings: Settings, val ability: PlayerAbility): BooleanItem(settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        if(isEnabled(stack)) {
            tooltip.add(TranslatableText("tooltip.kibe.enabled").formatted(Formatting.GREEN))
            tooltip.add(TranslatableText("tooltip.kibe.shift2disable").formatted(Formatting.BLUE, Formatting.ITALIC))
        }else {
            val tag = stack.orCreateTag
            if(tag.contains("enabled") && tag.getBoolean("enabled") && tag.contains("unique") && !tag.getBoolean("unique")) {
                tooltip.add(TranslatableText("tooltip.kibe.overflow").formatted(Formatting.RED))
                tooltip.add(TranslatableText("tooltip.kibe.overflowed").formatted(Formatting.DARK_RED, Formatting.ITALIC))
                tooltip.add(TranslatableText("tooltip.kibe.shift2disable").formatted(Formatting.BLUE, Formatting.ITALIC))
            }else{
                tooltip.add(TranslatableText("tooltip.kibe.disabled").formatted(Formatting.RED))
                tooltip.add(TranslatableText("tooltip.kibe.shift2enable").formatted(Formatting.BLUE, Formatting.ITALIC))
            }
        }
    }

    override fun isEnabled(stack: ItemStack): Boolean {
        val tag = stack.orCreateTag
        return tag.contains("enabled") && tag.getBoolean("enabled") && tag.contains("unique") && tag.getBoolean("unique")
    }



}