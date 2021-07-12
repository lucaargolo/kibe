package io.github.lucaargolo.kibe.compat

import dev.emi.trinkets.api.SlotType
import dev.emi.trinkets.api.Trinket
import io.github.ladysnake.pal.PlayerAbility
import io.github.lucaargolo.kibe.items.miscellaneous.AbilityRing
import io.github.lucaargolo.kibe.mixed.PlayerEntityMixed
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

fun initTrinketsCompat() {
    //TrinketSlots.addSlot(SlotGroups.HAND, Slots.RING, Identifier("trinkets", "textures/item/empty_trinket_slot_ring.png"));
}

class TrinketRing(settings: Settings, ability: PlayerAbility): AbilityRing(settings, ability), Trinket {

    override fun canEquip(stack: ItemStack, slot: Trinket.SlotReference, entity: LivingEntity): Boolean {
        return true
    }

//    override fun canWearInSlot(group: String, slot: String): Boolean {
//        return group == SlotGroups.HAND && slot == Slots.RING
//    }

    override fun tick(stack: ItemStack, slot: Trinket.SlotReference, entity: LivingEntity) {
        (entity as? PlayerEntityMixed)?.let {
            try { it.kibe_activeRingsList.removeAll { pair -> pair.second != entity.world.time } } catch (e: Exception) { }
            it.kibe_activeRingsList.add(Pair(stack, entity.world.time))
        }
    }

//    override fun tick(player: PlayerEntity, stack: ItemStack) {
//        (player as? PlayerEntityMixed)?.let {
//            try { it.kibe_activeRingsList.removeAll { pair -> pair.second != player.world.time } } catch (e: Exception) { }
//            it.kibe_activeRingsList.add(Pair(stack, player.world.time))
//        }
//    }

    override fun onEquip(stack: ItemStack, slot: Trinket.SlotReference, entity: LivingEntity) {
        if(stack.nbt?.getBoolean("enabled") != true)
            stack.orCreateNbt.putBoolean("enabled", true)
    }

//    override fun onEquip(player: PlayerEntity, stack: ItemStack) {
//        if(stack.tag?.getBoolean("enabled") != true)
//            stack.orCreateTag.putBoolean("enabled", true)
//    }

    override fun onUnequip(stack: ItemStack, slot: Trinket.SlotReference, entity: LivingEntity) {
        if(stack.nbt?.getBoolean("enabled") == true)
            stack.orCreateNbt.putBoolean("enabled", false)
    }

//    override fun onUnequip(player: PlayerEntity, stack: ItemStack) {
//        if(stack.tag?.getBoolean("enabled") == true)
//            stack.orCreateTag.putBoolean("enabled", false)
//    }

}

