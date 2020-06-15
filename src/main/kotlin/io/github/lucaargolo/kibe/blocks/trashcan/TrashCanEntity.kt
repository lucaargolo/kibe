package io.github.lucaargolo.kibe.blocks.trashcan

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.container.Container
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.DefaultedList

class TrashCanEntity(trashCan: TrashCan): LockableContainerBlockEntity(getEntityType(trashCan)) {

    val inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    override fun createContainer(i: Int, playerInventory: PlayerInventory?): Container? {
        return null
    }

    override fun getInvSize() = inventory.size


    override fun isInvEmpty(): Boolean {
        val iterator = this.inventory.iterator()
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

    override fun takeInvStack(slot: Int, amount: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removeInvStack(slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun setInvStack(slot: Int, stack: ItemStack?) {
        inventory[slot] = ItemStack.EMPTY
    }

    override fun clear() {
        inventory.clear()
    }

    override fun getContainerName(): Text = TranslatableText("screen.kibe.trash_can")

    override fun canPlayerUseInv(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }



}