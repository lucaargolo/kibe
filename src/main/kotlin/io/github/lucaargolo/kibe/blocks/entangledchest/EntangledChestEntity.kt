package io.github.lucaargolo.kibe.blocks.entangledchest

import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.SyncableBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class EntangledChestEntity(chest: EntangledChest, pos: BlockPos, state: BlockState): SyncableBlockEntity(getEntityType(chest), pos, state), Inventory {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)
    var runeColors = mutableMapOf<Int, DyeColor>()
    var key = EntangledChest.DEFAULT_KEY
    var owner = ""

    init {
        (1..8).forEach {
            runeColors[it] = DyeColor.WHITE
        }
        updateColorCode()
    }

    var colorCode = "00000000"

    fun updateColorCode() {
        var code = ""
        (1..8).forEach {
            code += runeColors[it]?.id?.let { int -> Integer.toHexString(int) }
        }
        colorCode = code
    }

    private fun hasPersistentState(): Boolean = hasWorld() && !world!!.isClient

    private fun getPersistentState(): EntangledChestState? {
        return (world as? ServerWorld)?.let { serverWorld ->
            serverWorld.server.overworld.persistentStateManager.getOrCreate( { EntangledChestState.createFromTag(it)}, { EntangledChestState() }, key)
        }
    }

    override fun markDirty() {
        if(hasPersistentState()) {
             getPersistentState()!!.markDirty()
        }
        super.markDirty()
    }

    private var lastComparatorOutput = 0
    var isBeingCompared = false

    fun getComparatorOutput(): Int {
        val comparatorOutput = ScreenHandler.calculateComparatorOutput(this as Inventory)
        isBeingCompared = true
        lastComparatorOutput = comparatorOutput
        return comparatorOutput
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE
        }
        updateColorCode()
        key = tag.getString("key")
        owner = tag.getString("owner")
        isBeingCompared = tag.getBoolean("isBeingCompared")
        lastComparatorOutput = tag.getInt("lastComparatorOutput")
    }

    override fun readClientNbt(tag: NbtCompound) {
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE
        }
        updateColorCode()
        key = tag.getString("key")
        owner = tag.getString("owner")
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY)
        Inventories.readNbt(tag, this.inventory)
    }

    override fun writeNbt(tag: NbtCompound) {
        super.writeNbt(tag)
        (1..8).forEach {
            tag.putString("rune$it", runeColors[it]!!.getName())
        }
        tag.putString("key", key)
        tag.putString("owner", owner)
        tag.putBoolean("isBeingCompared", isBeingCompared)
        tag.putInt("lastComparatorOutput", lastComparatorOutput)
        if(hasPersistentState()) {
            var subTag = NbtCompound()
            subTag = getPersistentState()!!.writeNbt(subTag)
            if(subTag[colorCode] != null) {
                subTag = subTag.get(colorCode) as NbtCompound
                tag.put("Items", subTag.get("Items"))
            }
        }
        else Inventories.writeNbt(tag, this.inventory)
    }

    override fun writeClientNbt(tag: NbtCompound): NbtCompound {
        (1..8).forEach {
            tag.putString("rune$it", runeColors[it]!!.getName())
        }
        tag.putString("key", key)
        tag.putString("owner", owner)
        Inventories.writeNbt(tag, this.inventory)
        return tag
    }

    override fun size(): Int {
        return if(hasPersistentState()) getPersistentState()!!.size(colorCode)
        else inventory.size
    }

    override fun isEmpty(): Boolean {
        return if(hasPersistentState()) getPersistentState()!!.isEmpty(colorCode)
        else inventory.all { it.isEmpty }
    }

    override fun getStack(slot: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.getStack(slot, colorCode)
        else inventory[slot]
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.removeStack(slot, amount, colorCode)
        else Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.removeStack(slot, colorCode)
        else Inventories.removeStack(this.inventory, slot)
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
        if(hasPersistentState()) getPersistentState()!!.setStack(slot, stack, colorCode)
        else {
            inventory[slot] = stack
            if (stack!!.count > maxCountPerStack) {
                stack.count = maxCountPerStack
            }
        }
    }

    override fun clear() {
        inventory.clear()
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }

    companion object {

        fun tick(world: World, pos: BlockPos, state: BlockState, entity: EntangledChestEntity) {
            if(!world.isClient && entity.isBeingCompared) {
                val comparatorOutput = ScreenHandler.calculateComparatorOutput(entity as Inventory)
                if(comparatorOutput != entity.lastComparatorOutput) {
                    world.updateComparators(pos, state.block)
                }
            }
        }
    }

}