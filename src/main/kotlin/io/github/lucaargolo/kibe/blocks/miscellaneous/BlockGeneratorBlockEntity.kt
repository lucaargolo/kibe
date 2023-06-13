package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.SyncableBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.registry.Registry
import net.minecraft.registry.Registries
import net.minecraft.world.World

class BlockGeneratorBlockEntity(var generator: BlockGenerator, var block: Block, var rate: Float, pos: BlockPos, state: BlockState): SyncableBlockEntity(getEntityType(generator), pos, state), SidedInventory {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)
    var lastRenderProgress: Float = 0f
    var renderProgress: Float = 0f
    var progress: Float = 0f

    override fun writeNbt(tag: NbtCompound) {
        Inventories.writeNbt(tag, inventory)
        tag.putString("generator", Registries.BLOCK.getId(generator).toString())
        tag.putString("block", Registries.BLOCK.getId(block).toString())
        tag.putFloat("rate", rate)
        tag.putFloat("progress", progress)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        Inventories.readNbt(tag, inventory)
        generator = Registries.BLOCK.get(Identifier(tag.getString("generator"))) as BlockGenerator
        block = Registries.BLOCK.get(Identifier(tag.getString("block")))
        rate = tag.getFloat("rate")
        progress = tag.getFloat("progress")
    }

    override fun writeClientNbt(tag: NbtCompound): NbtCompound {
        tag.putString("block", Registries.BLOCK.getId(block).toString())
        tag.putFloat("rate", rate)
        return tag
    }

    override fun readClientNbt(tag: NbtCompound) {
        block = Registries.BLOCK.get(Identifier(tag.getString("block")))
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

    companion object {

        fun tick(world: World, pos: BlockPos, state: BlockState, entity: BlockGeneratorBlockEntity) {
            entity.renderProgress += entity.rate
            entity.progress += entity.rate
            if(entity.progress >= 1f) {
                var count = MathHelper.floor(entity.progress)
                entity.progress -= count
                var slot = 0
                while (slot < 27 && !entity.inventory[slot].isEmpty && entity.inventory[slot].count == entity.inventory[slot].maxCount) {
                    slot++
                }
                if (slot < 27) {
                    entity.inventory[slot].let { slotStack ->
                        if(slotStack.isEmpty) {
                            if(!world.isClient) {
                                entity.inventory[slot] = ItemStack(entity.block, count)
                                entity.markDirty()
                            }
                        }else{
                            if(slotStack.count + count <= slotStack.maxCount) {
                                if(!world.isClient) {
                                    slotStack.increment(count)
                                    entity.markDirty()
                                }
                            }else{
                                count -= slotStack.maxCount - slotStack.count
                                if(!world.isClient) {
                                    slotStack.count = slotStack.maxCount
                                }
                                entity.progress += count
                                entity.progress -= entity.rate
                                entity.markDirty()
                                tick(world, pos, state, entity)
                            }
                        }
                    }
                }else{
                    //TODO: Cache if is full
                    entity.progress = 0f
                }
            }
        }

    }


}