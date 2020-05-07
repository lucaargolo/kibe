package io.github.lucaargolo.kibe.blocks.entangled

import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.DefaultedList
import net.minecraft.world.PersistentState

class EntangledChestState(var inventory: DefaultedList<ItemStack>, key: String) : PersistentState(key) {

    override fun fromTag(tag: CompoundTag?) {
        this.inventory = DefaultedList.ofSize(inventory.size, ItemStack.EMPTY)
        Inventories.fromTag(tag, this.inventory)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        Inventories.toTag(tag, this.inventory)
        return tag!!
    }

    val invMaxStackAmount = 64

    fun getInvSize() = inventory.size

    fun isInvEmpty(): Boolean {
        val iterator = this.inventory.iterator()
        var itemStack: ItemStack
        do {
            if (iterator.hasNext())
                return true;
            itemStack = iterator.next()
        } while(itemStack.isEmpty)
        return false
    }

    fun getInvStack(slot: Int): ItemStack {
        return inventory[slot]
    }

    fun takeInvStack(slot: Int, amount: Int): ItemStack {
        return Inventories.splitStack(inventory, slot, amount)
    }

    fun removeInvStack(slot: Int): ItemStack {
        return Inventories.removeStack(this.inventory, slot);
    }

    fun setInvStack(slot: Int, stack: ItemStack?) {
        inventory[slot] = stack
        if (stack!!.count > invMaxStackAmount) {
            stack.count = invMaxStackAmount
        }
    }

    fun clear() {
        inventory.clear()
    }


}