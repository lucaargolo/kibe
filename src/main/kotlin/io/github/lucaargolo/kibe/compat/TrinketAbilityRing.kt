package io.github.lucaargolo.kibe.compat

import io.github.ladysnake.pal.PlayerAbility
import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.item.AbilityRing
import io.github.lucaargolo.kibe.mixed.PlayerEntityMixed
import net.minecraft.item.ItemStack
import top.theillusivec4.curios.api.CuriosApi
import top.theillusivec4.curios.api.SlotContext
import top.theillusivec4.curios.api.type.capability.ICurioItem

class TrinketAbilityRing(settings: Settings, ability: PlayerAbility) : AbilityRing(settings, ability), ICurioItem {

    init {
        KibeMod.LOGGER.info("[${KibeMod.MOD_NAME}] Creating Trinket AbilityRing for ${ability.id}")
        CuriosApi.registerCurio(this, this)
    }

    override fun curioTick(slotContext: SlotContext, stack: ItemStack) {
        val entity = slotContext.entity
        if(!entity.world.isClient) {
            (entity as? PlayerEntityMixed)?.let {
                try {
                    it.`kibe$getActiveRingsList`().removeAll { pair -> pair.second != entity.world.time }
                } catch (_: Exception) { }
                it.`kibe$getActiveRingsList`().add(Pair(stack, entity.world.time))
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

    override fun onEquip(slotContext: SlotContext, prevStack: ItemStack, stack: ItemStack) {
        super.onEquip(slotContext, prevStack, stack)
        enable(stack)
    }

    override fun onUnequip(slotContext: SlotContext, newStack: ItemStack, stack: ItemStack) {
        super.onUnequip(slotContext, newStack, stack)
        disable(stack)
    }

}

