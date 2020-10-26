package io.github.lucaargolo.kibe.blocks.bigtorch

import io.github.lucaargolo.kibe.blocks.BIG_TORCH
import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.state.property.Properties
import net.minecraft.util.Tickable
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import kotlin.collections.LinkedHashSet
import kotlin.math.min
import kotlin.math.sqrt


class BigTorchBlockEntity(bigTorch: BigTorch): BlockEntity(getEntityType(bigTorch)), BlockEntityClientSerializable, SidedInventory, Tickable {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(9, ItemStack.EMPTY)

    var torchPercentage = 0.0
    var chunkRadius = 0

    var count = 0
    override fun tick() {
        if(count++ == 40) {
            count = 0
            world?.let {
                addSuppressedChunks(it.registryKey, this.getSuppressedChunks())
            }
        }
    }

    fun updateValues() {
        var torchQuantity = 0.0
        inventory.forEach { torchQuantity += it.count }
        torchPercentage = (torchQuantity/(inventory.size*64.0))
        world?.let { removeSuppressedChunks(it.registryKey, this.getSuppressedChunks())}
        chunkRadius = min(sqrt(torchQuantity / 9).toInt(), 8)
        world?.let { addSuppressedChunks(it.registryKey, this.getSuppressedChunks())}
        if(world?.getBlockState(pos)?.block == BIG_TORCH)
            world?.setBlockState(pos, cachedState.with(Properties.LEVEL_8, chunkRadius))
    }

    override fun markRemoved() {
        world?.let { removeSuppressedChunks(it.registryKey, this.getSuppressedChunks())}
        super.markRemoved()
    }

    override fun markDirty() {
        super.markDirty()
        updateValues()
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        //tag.putInt("suppressedSpawns", suppressedSpawns)
        Inventories.toTag(tag, inventory)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        //suppressedSpawns = tag.getInt("suppressedSpawns")
        Inventories.fromTag(tag, inventory)
        updateValues()
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        return toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag) {
        fromTag(BIG_TORCH.defaultState, tag)
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

    override fun getAvailableSlots(side: Direction?) = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8)

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = stack.item == Items.TORCH

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = true

    private fun getSuppressedChunks(): LinkedHashSet<ChunkPos> {
        val chunks = linkedSetOf<ChunkPos>()
        val centerChunk = ChunkPos(pos)
        for (x in (centerChunk.x - chunkRadius) until (centerChunk.x + chunkRadius)) {
            for (z in (centerChunk.z - chunkRadius) until (centerChunk.z + chunkRadius)) {
                chunks.add(ChunkPos(x, z))
            }
        }
        return chunks
    }

    companion object {
        private val suppressedChunkMap = linkedMapOf<RegistryKey<World>, LinkedHashSet<ChunkPos>>()
        private var isException = false

        fun setException(boolean: Boolean) {
            isException = boolean
        }

        fun addSuppressedChunks(registryKey: RegistryKey<World>, linkedHashSet: LinkedHashSet<ChunkPos>) {
            val set = suppressedChunkMap[registryKey] ?: linkedSetOf()
            set.addAll(linkedHashSet)
            suppressedChunkMap[registryKey] = set
        }

        fun removeSuppressedChunks(registryKey: RegistryKey<World>, linkedHashSet: LinkedHashSet<ChunkPos>) {
            suppressedChunkMap[registryKey]?.removeAll(linkedHashSet)
        }

        fun isChunkSuppressed(registryKey: RegistryKey<World>, chunkPos: ChunkPos): Boolean {
            if(isException) {
                isException = false
                return false
            }
            val set = suppressedChunkMap[registryKey] ?: linkedSetOf()
            return set.contains(chunkPos)
        }
    }

}
