package io.github.lucaargolo.kibe.blocks.witherbuilder

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.tag.BlockTags
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction

class WitherBuilderBlockEntity(placer: WitherBuilder): BlockEntity(getEntityType(placer)), SidedInventory {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(7, ItemStack.EMPTY)

    override fun toTag(tag: CompoundTag): CompoundTag {
        Inventories.toTag(tag, inventory)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        Inventories.fromTag(tag, inventory)
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

    override fun getAvailableSlots(side: Direction?): IntArray {
        val maxCount = (inventory.map { if (it.isEmpty) 0 else it.count }.minOrNull() ?: 0) + 1
        val a = inventory.mapIndexed { slot, stack -> Pair(slot, stack) }
        val b = a.filter { it.second.isEmpty || it.second.count < maxCount }
        val c = b.map { it.first }
        return c.toIntArray()
    }

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        return if((0..3).contains(slot)) {
            (stack.item as? BlockItem)?.block?.isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS) ?: false
        }else{
            stack.item == Items.WITHER_SKELETON_SKULL
        }
    }

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = true


}