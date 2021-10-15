@file:Suppress("DEPRECATION")

package io.github.lucaargolo.kibe.blocks.entangledchest

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.PersistentState

class EntangledChestState : PersistentState() {

    private var inventoryMap = mutableMapOf<String, DefaultedList<ItemStack>>()
    private var storageMap = mutableMapOf<String, Storage<ItemVariant>>()

    private fun createInventory(colorCode: String) {
        inventoryMap[colorCode] = DefaultedList.ofSize(27, ItemStack.EMPTY)
    }

    private fun hasInventory(colorCode: String): Boolean {
        return inventoryMap[colorCode] != null
    }

    fun getStorage(colorCode: String): Storage<ItemVariant> {
        val existing = storageMap[colorCode]
        if (existing != null) return existing
        if(!hasInventory(colorCode)) return Storage.empty()

        return InventoryStorage.of(
            object: Inventory {
                override fun clear() = clear(colorCode)
                override fun size(): Int = size(colorCode)
                override fun isEmpty(): Boolean = isEmpty(colorCode)
                override fun getStack(slot: Int): ItemStack = getStack(slot, colorCode)
                override fun removeStack(slot: Int, amount: Int): ItemStack = removeStack(slot, amount, colorCode)
                override fun removeStack(slot: Int): ItemStack = removeStack(slot, colorCode)
                override fun setStack(slot: Int, stack: ItemStack?) = setStack(slot, stack, colorCode)
                override fun canPlayerUse(player: PlayerEntity?): Boolean = true

                override fun markDirty() {
                    this@EntangledChestState.markDirty()
                }
            },
            null
        )
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