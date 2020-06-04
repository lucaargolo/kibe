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
        super.appendTooltip(stack, world, tooltip, context)
        if(isEnabled(stack)) {
            tooltip.add(TranslatableText("tooltip.kibe.enabled"))
            tooltip.add(TranslatableText("tooltip.kibe.shift2disable"))
        }else {
            val tag = stack.orCreateTag
            if(tag.contains("enabled") && tag.getBoolean("enabled") && tag.contains("unique") && !tag.getBoolean("unique")) {
                tooltip.add(TranslatableText("tooltip.kibe.overflow"))
                tooltip.add(TranslatableText("tooltip.kibe.overflowed"))
                tooltip.add(TranslatableText("tooltip.kibe.shift2disable"))
            }else{
                tooltip.add(TranslatableText("tooltip.kibe.disabled"))
                tooltip.add(TranslatableText("tooltip.kibe.shift2enable"))
            }
        }
    }

    override fun isEnabled(stack: ItemStack): Boolean {
        val tag = stack.orCreateTag
        return tag.contains("enabled") && tag.getBoolean("enabled") && tag.contains("unique") && tag.getBoolean("unique")
    }



}