package io.github.lucaargolo.kibe.items.miscellaneous

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class Glider(settings: Settings): Item(settings) {

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = player.getStackInHand(hand)
        val tag = stack.orCreateTag
        if(tag.contains("enabled") && tag.getBoolean("enabled")) {
            tag.putBoolean("enabled", false)
        }else{
            tag.putBoolean("enabled", true)
        }
        stack.tag = tag
        return TypedActionResult.success(stack)
    }

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity?, slot: Int, selected: Boolean) {
        var realSelected = selected
        if(!realSelected) {
            realSelected = (entity as? PlayerEntity)?.inventory?.offHand?.get(0)?.equals(stack) ?: false
        }
        val tag = stack.orCreateTag
        if(!realSelected && tag.contains("enabled") && tag.getBoolean("enabled")) {
            tag.putBoolean("enabled", false)
        }
    }

    companion object {

        fun isEnabled(stack: ItemStack): Boolean {
            val tag = stack.orCreateTag
            return tag.contains("enabled") && tag.getBoolean("enabled")
        }

    }

}