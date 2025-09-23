package io.github.lucaargolo.kibe.blockentity

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class PlacerBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(BlockEntityCompendium.PLACER, pos, state), SidedInventory {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(9, ItemStack.EMPTY)

    override fun writeNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        Inventories.writeNbt(tag, inventory, registryLookup)
    }

    override fun readNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.readNbt(tag, registryLookup)
        Inventories.readNbt(tag, inventory, registryLookup)
    }

    override fun size() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int) = inventory[slot]

    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(inventory, slot, amount)

    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(this.inventory, slot)

    override fun setStack(slot: Int, stack: ItemStack) {
        inventory[slot] = stack
        if (stack.count > maxCountPerStack) {
            stack.count = maxCountPerStack
        }
    }

    override fun clear()  = inventory.clear()

    override fun canPlayerUse(player: PlayerEntity): Boolean {
        return world?.getBlockEntity(pos)?.let { it == this && player.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0 } ?: false
    }

    override fun getAvailableSlots(side: Direction?) = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8)

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = stack.item is BlockItem

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = true


}