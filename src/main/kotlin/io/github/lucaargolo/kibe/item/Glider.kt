package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
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
        if(isEnabled(stack)) {
            stack.set(ComponentTypeCompendium.ENABLED, false)
        }else{
            stack.set(ComponentTypeCompendium.ENABLED, true)
        }
        return TypedActionResult.success(stack)
    }

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity?, slot: Int, selected: Boolean) {
        var realSelected = selected
        if(!realSelected) {
            realSelected = (entity as? PlayerEntity)?.inventory?.offHand?.get(0)?.equals(stack) ?: false
        }
        if(!realSelected && isEnabled(stack)) {
            stack.set(ComponentTypeCompendium.ENABLED, false)
        }
    }

    companion object {

        fun isEnabled(stack: ItemStack): Boolean {
            return stack.get(ComponentTypeCompendium.ENABLED) == true
        }

    }

}