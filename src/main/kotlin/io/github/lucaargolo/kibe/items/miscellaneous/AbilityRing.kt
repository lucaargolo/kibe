package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.ladysnake.pal.PlayerAbility
import io.github.lucaargolo.kibe.TRINKET
import io.github.lucaargolo.kibe.compat.trinkets.TrinketAbilityRing
import io.github.lucaargolo.kibe.mixed.PlayerEntityMixed
import net.minecraft.client.item.TooltipContext
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
                    it.kibe_activeRingsList.removeAll { pair -> pair.second != world.time }
                } catch (_: Exception) { }
                it.kibe_activeRingsList.add(Pair(stack, world.time))
            }
        }
    }

    override fun appendDisabledTooltip(stack: ItemStack, tooltip: MutableList<Text>) {
        val tag = stack.orCreateNbt
        if(tag.contains("enabled") && tag.getBoolean("enabled") && tag.contains("unique") && !tag.getBoolean("unique")) {
            tooltip.add(Text.translatable("tooltip.kibe.overflow"))
            tooltip.add(Text.translatable("tooltip.kibe.overflowed"))
            tooltip.add(Text.translatable("tooltip.kibe.shift2disable"))
        }else{
            tooltip.add(Text.translatable("tooltip.kibe.disabled"))
            tooltip.add(Text.translatable("tooltip.kibe.shift2enable"))
        }
    }

    override fun isEnabled(stack: ItemStack): Boolean {
        val tag = stack.orCreateNbt
        return ENABLED in tag && tag.getBoolean(ENABLED) && UNIQUE in tag && tag.getBoolean(UNIQUE)
    }

    override fun toggle(stack: ItemStack) {
        if(super.isEnabled(stack)) {
            disable(stack)
        }else{
            enable(stack)
        }
    }

    companion object {
        const val UNIQUE = "unique"

        val RINGS = mutableListOf<AbilityRing>()

        fun create(settings: Settings, ability: PlayerAbility): AbilityRing =
            if (TRINKET) TrinketAbilityRing(settings, ability) else AbilityRing(settings, ability)
    }
}