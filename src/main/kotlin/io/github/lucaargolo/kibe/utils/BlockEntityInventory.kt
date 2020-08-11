package io.github.lucaargolo.kibe.utils

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

class BlockEntityInventory(private val handler: ScreenHandler, val entity: Inventory): Inventory {
    override fun size(): Int {
        return entity.size()
    }

    override fun isEmpty(): Boolean {
        return entity.isEmpty
    }

    override fun getStack(slot: Int): ItemStack? {
        return entity.getStack(slot)
    }

    override fun removeStack(slot: Int): ItemStack? {
        val stack: ItemStack = entity.removeStack(slot)
        handler.onContentChanged(this)
        return stack
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack? {
        val stack: ItemStack = entity.removeStack(slot, amount)
        handler.onContentChanged(this)
        return stack
    }

    override fun setStack(slot: Int, stack: ItemStack) {
        entity.setStack(slot, stack)
        handler.onContentChanged(this)
    }

    override fun markDirty() {
        entity.markDirty()
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return entity.canPlayerUse(player)
    }

    override fun clear() {
        entity.clear()
    }

}