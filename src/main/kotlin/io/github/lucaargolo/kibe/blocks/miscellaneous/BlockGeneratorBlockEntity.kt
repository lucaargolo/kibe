package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.PropertyDelegate
import net.minecraft.util.Identifier
import net.minecraft.util.Tickable
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.registry.Registry

class BlockGeneratorBlockEntity(var generator: BlockGenerator, var block: Block, var rate: Float): BlockEntity(getEntityType(generator)), BlockEntityClientSerializable, SidedInventory, Tickable {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)
    var lastRenderProgress: Float = 0f
    var renderProgress: Float = 0f
    var progress: Float = 0f

    override fun tick() {

        renderProgress += rate
        progress += rate
        if(progress >= 1f) {
            var count = MathHelper.floor(progress)
            progress -= count
            var slot = 0
            while (slot < 27 && !inventory[slot].isEmpty && inventory[slot].count == inventory[slot].maxCount) {
                slot++
            }
            if (slot < 27) {
                inventory[slot].let { slotStack ->
                    if(slotStack.isEmpty) {
                        if(world?.isClient == false) {
                            inventory[slot] = ItemStack(block, count)
                            markDirty()
                        }
                    }else{
                        if(slotStack.count + count <= slotStack.maxCount) {
                            if(world?.isClient == false) {
                                slotStack.increment(count)
                                markDirty()
                            }
                        }else{
                            count -= slotStack.maxCount - slotStack.count
                            if(world?.isClient == false) {
                                slotStack.count = slotStack.maxCount
                            }
                            progress += count
                            progress -= rate
                            markDirty()
                            tick()
                        }
                    }
                }
            }else{
                //TODO: Cache if is full
                progress = 0f
            }
        }

    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        Inventories.toTag(tag, inventory)
        tag.putString("generator", Registry.BLOCK.getId(generator).toString())
        tag.putString("block", Registry.BLOCK.getId(block).toString())
        tag.putFloat("rate", rate)
        tag.putFloat("progress", progress)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        Inventories.fromTag(tag, inventory)
        generator = Registry.BLOCK.get(Identifier(tag.getString("generator"))) as BlockGenerator
        block = Registry.BLOCK.get(Identifier(tag.getString("block")))
        rate = tag.getFloat("rate")
        progress = tag.getFloat("progress")
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.putString("block", Registry.BLOCK.getId(block).toString())
        tag.putFloat("rate", rate)
        return tag
    }

    override fun fromClientTag(tag: CompoundTag) {
        block = Registry.BLOCK.get(Identifier(tag.getString("block")))
        rate = tag.getFloat("rate")
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

    override fun getAvailableSlots(side: Direction?) = (0..26).toList().toIntArray()

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = false

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = true


}