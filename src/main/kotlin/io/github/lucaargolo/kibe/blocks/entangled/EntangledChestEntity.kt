package io.github.lucaargolo.kibe.blocks.entangled

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.container.Container
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.DefaultedList

class EntangledChestEntity(chest: EntangledChest): LockableContainerBlockEntity(getEntityType(chest)) {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)

    override fun createContainer(i: Int, playerInventory: PlayerInventory?): Container? {
        return null
    }

    private fun hasPersistentState(): Boolean = hasWorld() && !world!!.isClient

    private fun getPersistentState(): EntangledChestState? {
        return if(hasWorld() && !world!!.isClient) {
            (world as ServerWorld).persistentStateManager.getOrCreate( {EntangledChestState(DefaultedList.ofSize(27, ItemStack.EMPTY), "sexo anal")}, "sexo anal")
        }else null
    }

    override fun markDirty() {
        if(hasPersistentState()) {
            getPersistentState()!!.markDirty()
        }
        super.markDirty()
    }

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        if(hasPersistentState()) getPersistentState()!!.fromTag(tag)
        else {
            this.inventory = DefaultedList.ofSize(this.invSize, ItemStack.EMPTY)
            Inventories.fromTag(tag, this.inventory)
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        super.toTag(tag)
        if(hasPersistentState()) getPersistentState()!!.toTag(tag)
        else Inventories.toTag(tag, this.inventory)
        return tag!!
    }

    override fun getInvSize(): Int {
        return if(hasPersistentState()) getPersistentState()!!.getInvSize();
        else inventory.size;
    }

    override fun isInvEmpty(): Boolean {
        return if(hasPersistentState()) getPersistentState()!!.isInvEmpty();
        else {
            val iterator = this.inventory.iterator()
            var itemStack: ItemStack
            do {
                if (iterator.hasNext())
                    return true;
                itemStack = iterator.next()
            } while(itemStack.isEmpty)
            return false
        }
    }

    override fun getInvStack(slot: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.getInvStack(slot);
        else inventory[slot]
    }

    override fun takeInvStack(slot: Int, amount: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.takeInvStack(slot, amount);
        else Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeInvStack(slot: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.removeInvStack(slot);
        else Inventories.removeStack(this.inventory, slot);
    }

    override fun setInvStack(slot: Int, stack: ItemStack?) {
        if(hasPersistentState()) getPersistentState()!!.setInvStack(slot, stack);
        else {
            inventory[slot] = stack
            if (stack!!.count > invMaxStackAmount) {
                stack.count = invMaxStackAmount
            }
        }
    }

    override fun clear() {
        return if(hasPersistentState()) getPersistentState()!!.clear();
        else inventory.clear()
    }

    override fun getContainerName(): Text = LiteralText("Entangled Chest")

    override fun canPlayerUseInv(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }



}