package io.github.lucaargolo.kibe.blockentity

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.utils.SyncableBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class DrawbridgeBlockEntity(pos: BlockPos, state: BlockState): SyncableBlockEntity(BlockEntityCompendium.DRAWBRIDGE, pos, state), SidedInventory {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(2, ItemStack.EMPTY)

    var lastCoverBlock: Block = BlockCompendium.DRAWBRIDGE

    override fun writeNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.writeNbt(tag, registryLookup)
        val nbtList = NbtList()
        for (i in inventory.indices) {
            val itemStack = inventory[i]
            val nbtCompound = NbtCompound()
            nbtCompound.putByte("Slot", i.toByte())
            if(!itemStack.isEmpty) {
                nbtList.add(itemStack.encode(registryLookup, nbtCompound))
            }else{
                nbtList.add(nbtCompound)
            }
        }
        tag.put("Items", nbtList)
    }

    override fun readNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.readNbt(tag, registryLookup)
        val nbtList: NbtList = tag.getList("Items", NbtElement.COMPOUND_TYPE.toInt())

        for (i in nbtList.indices) {
            val nbtCompound = nbtList.getCompound(i)
            val j = nbtCompound.getByte("Slot").toInt() and 255
            if (j < inventory.size) {
                if(nbtCompound.contains("id")) {
                    inventory[j] = ItemStack.fromNbt(registryLookup, nbtCompound).orElse(ItemStack.EMPTY)
                }else{
                    inventory[j] = ItemStack.EMPTY
                }
            }
        }
    }

    override fun writeClientNbt(tag: NbtCompound, registryLookup: WrapperLookup): NbtCompound {
        return tag.also { writeNbt(it, registryLookup) }
    }

    override fun readClientNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        readNbt(tag, registryLookup)
        MinecraftClient.getInstance().worldRenderer.updateBlock(world, pos, cachedState, cachedState, 0)
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

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = stack.item is BlockItem && (stack.item as BlockItem).block.defaultState.isFullCube(world, pos)

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = true

}