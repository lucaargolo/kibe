package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.ladysnake.pal.PlayerAbility
import io.github.lucaargolo.kibe.TRINKET
import io.github.lucaargolo.kibe.compat.trinkets.TrinketAbilityRing
import io.github.lucaargolo.kibe.mixed.PlayerEntityMixed
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Shadow

@Suppress("LeakingThis")
open class AbilityRing(settings: Settings, val ability: PlayerAbility): BooleanItem(settings) {

    init {
        RINGS.add(this)
    }

    @Shadow
    var lastworld: World? = null

    var togglenexttick = 0

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if(!world.isClient) {
            (entity as? PlayerEntityMixed)?.let {
                try {
                    it.kibe_activeRingsList.removeAll { pair -> pair.second != world.time }
                } catch (_: Exception) { }
                it.kibe_activeRingsList.add(Pair(stack, world.time))
                if ((entity.entityWorld != lastworld) && (lastworld != null) && (super.isEnabled(stack))) { //if the entity changed worlds since the ring was initialized and this is NOT on first join, if the ring is not enabled don't bother
                    lastworld = entity.entityWorld //sets the new most recent world
                    togglenexttick = 1
                    disable(stack) //toggle to disabled because on hard transfers the ring fails to work
                } else if (togglenexttick == 1){ //if the value was set signalling a change last tick
                    togglenexttick = 0
                    enable(stack) //toggle back to enabled
                } else if (lastworld == null) { //if the world is null, most likely on first join or weird cases
                    lastworld = entity.entityWorld
                }
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