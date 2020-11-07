package io.github.lucaargolo.kibe.blocks.entangledchest

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.DyeColor
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos

class EntangledChestEntity(chest: EntangledChest, pos: BlockPos, state: BlockState): LockableContainerBlockEntity(getEntityType(chest), pos, state), BlockEntityClientSerializable {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)
    var runeColors = mutableMapOf<Int, DyeColor>()
    var key = EntangledChest.DEFAULT_KEY
    var owner = ""

    init {
        (1..8).forEach {
            runeColors[it] = DyeColor.WHITE
        }
    }

    fun getColorCode(): String {
        var code = ""
        (1..8).forEach {
            code += when(runeColors[it]!!) {
                DyeColor.WHITE -> '0'
                DyeColor.ORANGE -> '1'
                DyeColor.MAGENTA -> '2'
                DyeColor.LIGHT_BLUE -> '3'
                DyeColor.YELLOW -> '4'
                DyeColor.LIME -> '5'
                DyeColor.PINK -> '6'
                DyeColor.GRAY -> '7'
                DyeColor.LIGHT_GRAY -> '8'
                DyeColor.CYAN -> '9'
                DyeColor.BLUE -> 'A'
                DyeColor.PURPLE -> 'B'
                DyeColor.GREEN -> 'C'
                DyeColor.BROWN -> 'D'
                DyeColor.RED -> 'E'
                DyeColor.BLACK -> 'F'
            }
        }
        return code
    }

    override fun createScreenHandler(i: Int, playerInventory: PlayerInventory?): ScreenHandler? {
        return null
    }

    private fun hasPersistentState(): Boolean = hasWorld() && !world!!.isClient

    private fun getPersistentState(): EntangledChestState? {
        return (world as? ServerWorld)?.let { serverWorld ->
            serverWorld.server.overworld.persistentStateManager.getOrCreate( {EntangledChestState(key)}, key)
        }
    }

    override fun markDirty() {
        if(hasPersistentState()) {
             getPersistentState()!!.markDirty()
        }
        super.markDirty()
    }

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
        }
        key = tag.getString("key")
        owner = tag.getString("owner")
    }

    override fun fromClientTag(tag: CompoundTag) {
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
        }
        key = tag.getString("key")
        owner = tag.getString("owner")
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY)
        Inventories.fromTag(tag, this.inventory)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        (1..8).forEach {
            tag.putString("rune$it", runeColors[it]!!.getName())
        }
        tag.putString("key", key)
        tag.putString("owner", owner)
        if(hasPersistentState()) {
            var subTag = CompoundTag()
            subTag = getPersistentState()!!.toTag(subTag)
            if(subTag[getColorCode()] != null) {
                subTag = subTag.get(getColorCode()) as CompoundTag
                tag.put("Items", subTag.get("Items"))
            }
        }
        else Inventories.toTag(tag, this.inventory)
        return tag
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        (1..8).forEach {
            tag.putString("rune$it", runeColors[it]!!.getName())
        }
        tag.putString("key", key)
        tag.putString("owner", owner)
        Inventories.toTag(tag, this.inventory)
        return tag
    }

    override fun size(): Int {
        return if(hasPersistentState()) getPersistentState()!!.size(getColorCode())
        else inventory.size
    }

    override fun isEmpty(): Boolean {
        return if(hasPersistentState()) getPersistentState()!!.isEmpty(getColorCode())
        else inventory.all { it.isEmpty }
    }

    override fun getStack(slot: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.getStack(slot, getColorCode())
        else inventory[slot]
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.removeStack(slot, amount, getColorCode())
        else Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.removeStack(slot, getColorCode())
        else Inventories.removeStack(this.inventory, slot)
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
        if(hasPersistentState()) getPersistentState()!!.setStack(slot, stack, getColorCode())
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

    override fun getContainerName(): Text = TranslatableText("screen.kibe.entangled_chest")

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }




}