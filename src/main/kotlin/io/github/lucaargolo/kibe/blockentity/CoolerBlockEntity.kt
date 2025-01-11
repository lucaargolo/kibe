package io.github.lucaargolo.kibe.blockentity

import io.github.lucaargolo.kibe.utils.SyncableBlockEntity
import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.minecraft.block.BlockState
import net.minecraft.component.ComponentMap
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class CoolerBlockEntity(pos: BlockPos, state: BlockState): SyncableBlockEntity(BlockEntityCompendium.COOLER, pos, state), SidedInventory {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    override fun writeNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.writeNbt(tag, registryLookup)
        Inventories.writeNbt(tag, inventory, registryLookup)
    }

    override fun readNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.readNbt(tag, registryLookup)
        Inventories.readNbt(tag, inventory, registryLookup)
    }

    override fun writeClientNbt(tag: NbtCompound, registryLookup: WrapperLookup): NbtCompound {
        return tag.also { writeNbt(it, registryLookup) }
    }

    override fun readClientNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        readNbt(tag, registryLookup)
    }

    //TODO: Properly fix this here and in Entangled Chest/Tank
    override fun addComponents(builder: ComponentMap.Builder) {
        builder.add(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(NbtCompound().also { FluidHelper.writeTank(it, tank) }))
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

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = stack.contains(DataComponentTypes.FOOD)

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = true


}