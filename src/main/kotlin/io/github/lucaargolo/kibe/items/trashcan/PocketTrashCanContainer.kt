package io.github.lucaargolo.kibe.items.trashcan

import net.minecraft.container.Container
import net.minecraft.container.Slot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.DefaultedList
import net.minecraft.world.World

@Suppress("UNUSED_PARAMETER")
class PocketTrashCanContainer(syncId: Int, playerInventory: PlayerInventory, val world: World, tag: CompoundTag?): Container(null, syncId) {

    val inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    private var synchronizedInventory: Inventory = object: Inventory {
        override fun getInvSize() = inventory.size

        override fun isInvEmpty(): Boolean {
            val iterator = inventory.iterator()
            var itemStack: ItemStack
            do {
                if (iterator.hasNext())
                    return true
                itemStack = iterator.next()
            } while(itemStack.isEmpty)
            return false
        }

        override fun getInvStack(slot: Int): ItemStack {
            return ItemStack.EMPTY
        }

        override fun markDirty() {
            return
        }

        override fun takeInvStack(slot: Int, amount: Int): ItemStack {
            return ItemStack.EMPTY
        }

        override fun removeInvStack(slot: Int): ItemStack {
            return ItemStack.EMPTY
        }

        override fun canPlayerUseInv(player: PlayerEntity?): Boolean {
            return true
        }

        override fun setInvStack(slot: Int, stack: ItemStack?) {
            inventory[slot] = ItemStack.EMPTY
        }

        override fun clear() {
            inventory.clear()
        }
    }

    init {
        checkContainerSize(synchronizedInventory, 1)
        synchronizedInventory.onInvOpen(playerInventory.player)
        val i: Int = (3 - 4) * 18

        addSlot(Slot(synchronizedInventory, 0, 8 + 4*18,  36))

        (0..2).forEach {n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 161 + i))
        }

    }

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }

    override fun transferSlot(player: PlayerEntity?, invSlot: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[invSlot]
        if (slot != null && slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (invSlot < 1) {
                if (!insertItem(itemStack2, 1, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, 0, 1, false)) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }
        return itemStack
    }

}