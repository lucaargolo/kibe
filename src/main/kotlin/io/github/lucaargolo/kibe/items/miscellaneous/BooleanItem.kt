package io.github.lucaargolo.kibe.items.miscellaneous

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

open class BooleanItem(settings: Settings): Item(settings) {

    companion object {
        protected const val ENABLED = "enabled"
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        if (stack.item is AbilityRing) return
        if (isEnabled(stack)) {
            tooltip.add(Text.translatable("tooltip.kibe.enabled"))
            tooltip.add(Text.translatable("tooltip.kibe.shift2disable"))
        } else {
            tooltip.add(Text.translatable("tooltip.kibe.disabled"))
            tooltip.add(Text.translatable("tooltip.kibe.shift2enable"))
        }
    }

    override fun hasGlint(stack: ItemStack): Boolean = isEnabled(stack)

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = player.getStackInHand(hand)
        if(player.isSneaking) {
            stack.orCreateNbt.putBoolean(ENABLED, !isEnabled(stack))
            return TypedActionResult.success(stack)
        }
        return TypedActionResult.pass(stack)
    }

    open fun isEnabled(stack: ItemStack): Boolean {
        return stack.nbt?.getBoolean(ENABLED) ?: false
    }

    fun enable(stack: ItemStack) {
        if (!isEnabled(stack)) {
            stack.orCreateNbt.putBoolean(ENABLED, true)
        }
    }

    fun disable(stack: ItemStack) {
        if (isEnabled(stack)) {
            stack.orCreateNbt.putBoolean(ENABLED, false)
        }
    }
}