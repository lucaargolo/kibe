package io.github.lucaargolo.kibe.blocks.cooler

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class CoolerBlockEntity(cooler: Cooler, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(cooler), pos, state), BlockEntityClientSerializable, SidedInventory {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        Inventories.writeNbt(tag, inventory)
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        Inventories.readNbt(tag, inventory)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        return writeNbt(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        readNbt(tag)
    }

    override fun size() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int) = inventory[slot]

    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(inventory, slot, amount)

    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(this.inventory, slot)

    override fun setStack(slot: Int, stack: ItemStack?) {
        inventory[slot] = stack
        if (stack!!.count > maxCountPerStack) {
            stack.count = maxCountPerStack
        }
    }

    override fun clear()  = inventory.clear()

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }

    override fun getAvailableSlots(side: Direction?) = intArrayOf(0)

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = stack.item.isFood

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = true


}