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

    override fun hasGlint(stack: ItemStack): Boolean = isEnabled(stack)

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        if (isEnabled(stack)) {
            appendEnabledTooltip(stack, tooltip)
        } else {
            appendDisabledTooltip(stack, tooltip)
        }
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = player.getStackInHand(hand)
        if(player.isSneaking && !world.isClient) {
            toggle(stack)
        }
        return TypedActionResult.pass(stack)
    }

    open fun appendEnabledTooltip(stack: ItemStack, tooltip: MutableList<Text>) {
        tooltip.add(Text.translatable("tooltip.kibe.enabled"))
        tooltip.add(Text.translatable("tooltip.kibe.shift2disable"))
    }

    open fun appendDisabledTooltip(stack: ItemStack, tooltip: MutableList<Text>) {
        tooltip.add(Text.translatable("tooltip.kibe.enabled"))
        tooltip.add(Text.translatable("tooltip.kibe.shift2disable"))
    }

    open fun isEnabled(stack: ItemStack): Boolean {
        return stack.nbt?.getBoolean(ENABLED) ?: false
    }

    open fun enable(stack: ItemStack) {
        stack.orCreateNbt.putBoolean(ENABLED, true)
    }

    open fun disable(stack: ItemStack) {
        stack.orCreateNbt.putBoolean(ENABLED, false)
    }

    open fun toggle(stack: ItemStack) {
        if(isEnabled(stack)) {
            disable(stack)
        }else{
            enable(stack)
        }
    }

    companion object {
        const val ENABLED = "enabled"
    }

}