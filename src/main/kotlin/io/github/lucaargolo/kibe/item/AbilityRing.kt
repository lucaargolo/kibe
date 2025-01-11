package io.github.lucaargolo.kibe.item

import io.github.ladysnake.pal.PlayerAbility
import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.compat.TrinketAbilityRing
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import io.github.lucaargolo.kibe.mixed.PlayerEntityMixed
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

import net.minecraft.world.World

@Suppress("LeakingThis")
open class AbilityRing(settings: Settings, val ability: PlayerAbility): BooleanItem(settings) {

    init {
        RINGS.add(this)
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if(!world.isClient) {
            (entity as? PlayerEntityMixed)?.let {
                try {
                    it.`kibe$getActiveRingsList`().removeAll { pair -> pair.second != world.time }
                } catch (_: Exception) { }
                it.`kibe$getActiveRingsList`().add(Pair(stack, world.time))
            }
        }
    }

    override fun appendDisabledTooltip(stack: ItemStack, tooltip: MutableList<Text>) {
        if(stack.get(ComponentTypeCompendium.ENABLED) == true && stack.get(ComponentTypeCompendium.UNIQUE) != true) {
            tooltip.add(Text.translatable("tooltip.kibe.overflow"))
            tooltip.add(Text.translatable("tooltip.kibe.overflowed"))
            tooltip.add(Text.translatable("tooltip.kibe.shift2disable"))
        }else{
            tooltip.add(Text.translatable("tooltip.kibe.disabled"))
            tooltip.add(Text.translatable("tooltip.kibe.shift2enable"))
        }
    }

    override fun isEnabled(stack: ItemStack): Boolean {
        return stack.get(ComponentTypeCompendium.ENABLED) == true && stack.get(ComponentTypeCompendium.UNIQUE) == true
    }

    override fun toggle(stack: ItemStack) {
        if(super.isEnabled(stack)) {
            disable(stack)
        }else{
            enable(stack)
        }
    }

    companion object {
        val RINGS = mutableListOf<AbilityRing>()

        fun create(settings: Settings, ability: PlayerAbility): AbilityRing =
            if (KibeMod.TRINKET) TrinketAbilityRing(settings, ability) else AbilityRing(settings, ability)
    }
}