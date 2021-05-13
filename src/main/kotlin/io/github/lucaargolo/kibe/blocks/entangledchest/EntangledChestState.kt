package io.github.lucaargolo.kibe.blocks.entangledchest

import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.PersistentState

class EntangledChestState : PersistentState() {

    private var inventoryMap = mutableMapOf<String, DefaultedList<ItemStack>>()

    private fun createInventory(colorCode: String) {
        inventoryMap[colorCode] = DefaultedList.ofSize(27, ItemStack.EMPTY)
    }

    private fun hasInventory(colorCode: String): Boolean {
        return inventoryMap[colorCode] != null
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        inventoryMap.forEach { (colorCode, inventory) ->
            val subTag = NbtCompound()
            Inventories.writeNbt(subTag, inventory)
            tag.put(colorCode, subTag)
        }
        return tag
    }

    private val invMaxStackAmount = 64

    fun size(colorCode: String): Int {
        if(!hasInventory(colorCode)) createInventory(colorCode)
        return inventoryMap[colorCode]!!.size
    }

    fun isEmpty(colorCode: String): Boolean {
        if(!hasInventory(colorCode)) createInventory(colorCode)
        return inventoryMap[colorCode]!!.all { it.isEmpty }
    }

    fun getStack(slot: Int, colorCode: String): ItemStack {
        if(!hasInventory(colorCode)) createInventory(colorCode)
        return inventoryMap[colorCode]!![slot]
    }

    fun removeStack(slot: Int, amount: Int, colorCode: String): ItemStack {
        if(!hasInventory(colorCode)) createInventory(colorCode)
        return Inventories.splitStack(inventoryMap[colorCode]!!, slot, amount)
    }

    fun removeStack(slot: Int, colorCode: String): ItemStack {
        if(!hasInventory(colorCode)) createInventory(colorCode)
        return Inventories.removeStack(inventoryMap[colorCode]!!, slot)
    }

    fun setStack(slot: Int, stack: ItemStack?, colorCode: String) {
        if(!hasInventory(colorCode)) createInventory(colorCode)
        inventoryMap[colorCode]!![slot] = stack
        if (stack!!.count > invMaxStackAmount) {
            stack.count = invMaxStackAmount
        }
    }

    fun clear(colorCode: String) {
        if(!hasInventory(colorCode)) createInventory(colorCode)
        inventoryMap[colorCode]!!.clear()
    }

    companion object {
        fun createFromTag(tag: NbtCompound): EntangledChestState {
            val state = EntangledChestState()
            tag.keys.forEach {
                val tempInventory = DefaultedList.ofSize(27, ItemStack.EMPTY)
                Inventories.readNbt(tag.get(it) as NbtCompound, tempInventory)
                state.inventoryMap[it] = tempInventory
            }
            return state
        }
    }

}