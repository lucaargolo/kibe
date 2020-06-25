package io.github.lucaargolo.kibe.items.miscellaneous

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

open class BooleanItem(settings: Settings): Item(settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        if(stack.item is AbilityRing) return
        if(isEnabled(stack)) {
            tooltip.add(TranslatableText("tooltip.kibe.enabled"))
            tooltip.add(TranslatableText("tooltip.kibe.shift2disable"))
        }else {
            tooltip.add(TranslatableText("tooltip.kibe.disabled"))
            tooltip.add(TranslatableText("tooltip.kibe.shift2enable"))
        }
    }

    override fun hasGlint(stack: ItemStack): Boolean {
        return isEnabled(stack)
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = player.getStackInHand(hand)
        if(player.isSneaking) {
            val tag = stack.orCreateTag
            if(tag.contains("enabled") && tag.getBoolean("enabled")) {
                tag.putBoolean("enabled", false)
            }else{
                tag.putBoolean("enabled", true)
            }
            stack.tag = tag
            return TypedActionResult.success(stack)
        }
        return TypedActionResult.pass(stack)
    }

    open fun isEnabled(stack: ItemStack): Boolean {
        val tag = stack.orCreateTag
        return tag.contains("enabled") && tag.getBoolean("enabled")
    }

}