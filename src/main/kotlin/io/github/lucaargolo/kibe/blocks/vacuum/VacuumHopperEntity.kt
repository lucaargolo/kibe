package io.github.lucaargolo.kibe.blocks.vacuum

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.DefaultedList

class VacuumHopperEntity(vacuumHopper: VacuumHopper): LockableContainerBlockEntity(getEntityType(vacuumHopper)) {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(9, ItemStack.EMPTY)

    fun addStack(stack: ItemStack): ItemStack {
        var modifiableStack = stack
        inventory.forEachIndexed { id, stk ->
            if(modifiableStack == ItemStack.EMPTY) return@forEachIndexed
            if(stk.isEmpty) {
                inventory[id] = modifiableStack
                modifiableStack = ItemStack.EMPTY
            }else{
                if(stk.item == modifiableStack.item) {
                    if(stk.count+modifiableStack.count > stk.maxCount) {
                        val aux = stk.maxCount-stk.count
                        stk.count = stk.maxCount
                        modifiableStack.count -= aux
                    }else if(stk.count+modifiableStack.count == stk.maxCount){
                        stk.count = stk.maxCount
                        modifiableStack = ItemStack.EMPTY;
                    }else{
                        stk.count += modifiableStack.count
                        modifiableStack = ItemStack.EMPTY
                    }
                }
                if(modifiableStack.count <= 0) {
                    modifiableStack = ItemStack.EMPTY
                }
            }
        }
        markDirty()
        return modifiableStack
    }

    override fun createContainer(i: Int, playerInventory: PlayerInventory?) = null

    override fun getInvSize() = inventory.size;

    override fun isInvEmpty(): Boolean {
        val iterator = this.inventory.iterator()
        var itemStack: ItemStack
        do {
            if (iterator.hasNext())
                return true;
            itemStack = iterator.next()
        } while(itemStack.isEmpty)
        return false
    }

    override fun getInvStack(slot: Int) = inventory[slot]

    override fun takeInvStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(inventory, slot, amount)

    override fun removeInvStack(slot: Int): ItemStack = Inventories.removeStack(this.inventory, slot);

    override fun setInvStack(slot: Int, stack: ItemStack?) {
        inventory[slot] = stack
        if (stack!!.count > invMaxStackAmount) {
            stack.count = invMaxStackAmount
        }
    }

    override fun clear()  = inventory.clear()

    override fun getContainerName(): Text = LiteralText("Vacuum Hopper")

    override fun canPlayerUseInv(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }

}