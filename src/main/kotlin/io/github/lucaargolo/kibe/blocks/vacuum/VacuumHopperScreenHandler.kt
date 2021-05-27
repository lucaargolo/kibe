package io.github.lucaargolo.kibe.blocks.vacuum

import io.github.lucaargolo.kibe.blocks.VACUUM_HOPPER
import io.github.lucaargolo.kibe.blocks.getContainerInfo
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class VacuumHopperScreenHandler (syncId: Int, playerInventory: PlayerInventory, val entity: VacuumHopperEntity, private val context: ScreenHandlerContext): ScreenHandler(getContainerInfo(VACUUM_HOPPER)?.handlerType, syncId) {

    private val propertyDelegate = entity.propertyDelegate

    var processingTicks
        get() = propertyDelegate.get(0)
        set(value) = propertyDelegate.set(0, value)

    var totalProcessingTicks
        get() = propertyDelegate.get(1)
        set(value) = propertyDelegate.set(1, value)

    var inventory: Inventory = object: Inventory {
        override fun size(): Int {
            return entity.size()
        }

        override fun isEmpty(): Boolean {
            return entity.isEmpty()
        }

        override fun getStack(slot: Int): ItemStack {
            return entity.getStack(slot)
        }

        override fun removeStack(slot: Int): ItemStack {
            val stack: ItemStack = entity.removeStack(slot)
            onContentChanged(this)
            return stack
        }

        override fun removeStack(slot: Int, amount: Int): ItemStack {
            val stack: ItemStack = entity.removeStack(slot, amount)
            onContentChanged(this)
            return stack
        }

        override fun setStack(slot: Int, stack: ItemStack?) {
            entity.setStack(slot, stack)
            onContentChanged(this)
        }

        override fun markDirty() {
            entity.markDirty()
        }

        override fun canPlayerUse(player: PlayerEntity?): Boolean {
            return entity.canPlayerUse(player)
        }

        override fun clear() {
            entity.clear()
        }

    }

    init {
        checkSize(inventory, 9)
        checkDataCount(propertyDelegate, 2)
        inventory.onOpen(playerInventory.player)
        val i: Int = (3 - 4) * 18

        addSlot(object: Slot(inventory, 10, 8 + 6 * 18, 18 + 2 * 18 ) {
            override fun canInsert(itemStack: ItemStack) = false
        })
        addSlot(Slot(inventory, 9, 8 + 6 * 18, 18 ))

        (0..2).forEach {n ->
            (0..2).forEach { m ->
                addSlot(Slot(inventory, m + n * 3, 8 + (m+2) * 18, 18 + n * 18))
            }
        }

        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 161 + i))
        }

        addProperties(propertyDelegate)
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return context.get({ world: World, blockPos: BlockPos ->
            if (world.getBlockState(
                    blockPos
                ).block != VACUUM_HOPPER
            ) false else player.squaredDistanceTo(
                blockPos.x + .5,
                blockPos.y + .5,
                blockPos.z + .5
            ) < 64.0
        }, true)
    }

    override fun transferSlot(player: PlayerEntity, invSlot: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = slots[invSlot]
        if (slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (invSlot == 0) {
                context.run { world: World?, _: BlockPos? ->
                    itemStack2.item.onCraft(itemStack2, world, player)
                }
                if (!insertItem(itemStack2, 11, 47, true)) {
                    return ItemStack.EMPTY
                }
                slot.onQuickTransfer(itemStack2, itemStack)
            } else if (invSlot in 11..46) {
                if (!insertItem(itemStack2, 1, 11, false)) {
                    if (invSlot < 38) {
                        if (!insertItem(itemStack2, 38, 47, false)) {
                            return ItemStack.EMPTY
                        }
                    } else if (!insertItem(itemStack2, 11, 38, false)) {
                        return ItemStack.EMPTY
                    }
                }
            } else if (!insertItem(itemStack2, 11, 47, false)) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
            if (itemStack2.count == itemStack.count) {
                return ItemStack.EMPTY
            }
            slot.onTakeItem(player, itemStack2)
            if (invSlot == 0) {
                player.dropItem(itemStack2, false)
            }
        }

        return itemStack
    }

}