package io.github.lucaargolo.kibe.blocks.trashcan

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.collection.DefaultedList

class TrashCanEntity(trashCan: TrashCan): LockableContainerBlockEntity(getEntityType(trashCan)) {

    val inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    override fun createScreenHandler(i: Int, playerInventory: PlayerInventory?): ScreenHandler? {
        return null
    }

    override fun size() = inventory.size


    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removeStack(slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
        inventory[slot] = ItemStack.EMPTY
    }

    override fun clear() {
        inventory.clear()
    }

    override fun getContainerName(): Text = TranslatableText("screen.kibe.trash_can")

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }



}