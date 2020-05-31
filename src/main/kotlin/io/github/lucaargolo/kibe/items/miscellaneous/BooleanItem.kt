package io.github.lucaargolo.kibe.items.miscellaneous

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class BooleanItem(settings: Settings): Item(settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        if(isEnabled(stack)) {
            tooltip.add(LiteralText("${Formatting.GREEN}Enabled"))
            tooltip.add(LiteralText("${Formatting.BLUE}${Formatting.ITALIC}Shift click to disable"))
        }else {
            tooltip.add(LiteralText("${Formatting.RED}Disabled"))
            tooltip.add(LiteralText("${Formatting.BLUE}${Formatting.ITALIC}Shift click to enable"))
        }
        super.appendTooltip(stack, world, tooltip, context)
    }

    override fun hasEnchantmentGlint(stack: ItemStack): Boolean {
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

    companion object {
        fun isEnabled(stack: ItemStack): Boolean {
            val tag = stack.orCreateTag
            return tag.contains("enabled") && tag.getBoolean("enabled")
        }
    }

}