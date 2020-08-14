package io.github.lucaargolo.kibe.blocks.vacuum

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.collection.DefaultedList
import kotlin.math.min

class VacuumHopperEntity(vacuumHopper: VacuumHopper): LockableContainerBlockEntity(getEntityType(vacuumHopper)), BlockEntityClientSerializable {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(9, ItemStack.EMPTY)
    var liquidXp: Int = 0
        private set

    fun addLiquid(qnt: Int): Boolean {
        liquidXp = min(liquidXp+qnt, 16000)
        markDirty()
        return true
    }

    fun removeLiquid(qnt: Int): Boolean {
        return if(liquidXp - qnt >= 0) {
            liquidXp -= qnt
            markDirty()
            true
        }else false
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putInt("fluid", liquidXp)
        Inventories.toTag(tag, inventory)
        return super.toTag(tag)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.putInt("fluid", liquidXp)
        Inventories.toTag(tag, inventory)
        return tag
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        liquidXp = tag.getInt("fluid")
        Inventories.fromTag(tag, inventory)
    }

    override fun fromClientTag(tag: CompoundTag) {
        liquidXp = tag.getInt("fluid")
        Inventories.fromTag(tag, inventory)
    }

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
                        modifiableStack = ItemStack.EMPTY
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

    override fun createScreenHandler(i: Int, playerInventory: PlayerInventory?) = null

    override fun size() = inventory.size

    override fun isEmpty(): Boolean {
        val iterator = this.inventory.iterator()
        var itemStack: ItemStack
        do {
            if (iterator.hasNext())
                return true
            itemStack = iterator.next()
        } while(itemStack.isEmpty)
        return false
    }

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

    override fun getContainerName(): Text = TranslatableText("screen.kibe.vacuum_hopper")

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }

}