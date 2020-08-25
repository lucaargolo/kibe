package io.github.lucaargolo.kibe.items.cooler

import io.github.lucaargolo.kibe.items.COOLER
import io.github.lucaargolo.kibe.items.getContainerInfo
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Hand
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class CoolerBlockItemScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val hand: Hand, val world: World, val tag: CompoundTag): ScreenHandler(getContainerInfo(COOLER)?.handlerType, syncId)  {

    val rawInventory = DefaultedList.ofSize(1, ItemStack.EMPTY)

    val inventory = object: Inventory {
        override fun markDirty() {}
        override fun clear() = rawInventory.clear()
        override fun getStack(slot: Int) = rawInventory[slot]
        override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(rawInventory, slot, amount)
        override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(rawInventory, slot)
        override fun setStack(slot: Int, stack: ItemStack?) {
            rawInventory[slot] = stack
            if (stack!!.count > maxCountPerStack) {
                stack.count = maxCountPerStack
            }
        }
        override fun isEmpty() = rawInventory[0].isEmpty
        override fun canPlayerUse(player: PlayerEntity?) = true
        override fun size() = 1
    }

    init {
        Inventories.fromTag(tag, rawInventory)
        checkSize(inventory, 1)
        inventory.onOpen(playerInventory.player)

        addSlot(object: Slot(inventory, 0, 8+18*4, 18) {
            override fun canInsert(stack: ItemStack) = stack.item.isFood
        })

        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 49 + n*18))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 107))
        }
    }

    override fun onSlotClick(i: Int, j: Int, actionType: SlotActionType?, playerEntity: PlayerEntity?): ItemStack {
        val stack = if(hand == Hand.MAIN_HAND && i in 0..slots.size && getSlot(i).stack == playerInventory.mainHandStack)
            ItemStack.EMPTY
        else
            super.onSlotClick(i, j, actionType, playerEntity)
        this.onContentChanged(null)
        return stack
    }

    override fun onContentChanged(inventory: Inventory?) {
        super.onContentChanged(inventory)
        Inventories.toTag(tag, rawInventory)
        val coolerStack = playerInventory.player.getStackInHand(hand)
        if (coolerStack.item is CoolerBlockItem) {
            coolerStack.orCreateTag.put("BlockEntityTag", tag.copy())
        }
    }

    override fun transferSlot(player: PlayerEntity?, invSlot: Int): ItemStack? {
        if(hand == Hand.MAIN_HAND && invSlot in 0..slots.size && getSlot(invSlot).stack == playerInventory.mainHandStack)
            return ItemStack.EMPTY
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

    override fun canUse(player: PlayerEntity) = true

}