package io.github.lucaargolo.kibe.blocks.drawbridge

import io.github.lucaargolo.kibe.blocks.DRAWBRIDGE
import io.github.lucaargolo.kibe.blocks.getContainerInfo
import io.github.lucaargolo.kibe.utils.BlockEntityInventory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class DrawbridgeScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val entity: DrawbridgeBlockEntity, private val context: ScreenHandlerContext): ScreenHandler(getContainerInfo(DRAWBRIDGE)?.handlerType, syncId)  {

    val inventory = BlockEntityInventory(this, entity)

    init {
        checkSize(inventory, 2)
        inventory.onOpen(playerInventory.player)

        addSlot(object: Slot(inventory, 0, 80, 18) {
            override fun canInsert(stack: ItemStack) = stack.item is BlockItem && (stack.item as BlockItem).block.defaultState.isFullCube(entity.world, entity.pos)
        })

        addSlot(object: Slot(inventory, 1, 134, 18) {
            override fun getMaxItemCount() = 1
            override fun canInsert(stack: ItemStack) = stack.item is BlockItem && (stack.item as BlockItem).block.defaultState.isFullCube(entity.world, entity.pos)
        })

        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 49 + n*18))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 107))
        }
    }

    override fun onSlotClick(slot: Int, mouse: Int, actionType: SlotActionType?, playerEntity: PlayerEntity?): ItemStack {
        if(slot == 0) {
            entity.extendedBlock = null
            entity.extendedBlocks = 0
        }
        return super.onSlotClick(slot, mouse, actionType, playerEntity)
    }

    override fun onContentChanged(inventory: Inventory) {
        super.onContentChanged(inventory)
        val block = (inventory.getStack(1).item as? BlockItem)?.block ?: DRAWBRIDGE
        if(entity.lastCoverBlock != block && entity.world is ServerWorld) {
            entity.lastCoverBlock = block
            entity.markDirty()
            entity.sync()
        }
    }

    override fun transferSlot(player: PlayerEntity?, invSlot: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[invSlot]
        if (slot != null && slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (invSlot < 2) {
                if (!insertItem(itemStack2, 2, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (invSlot == 1 || !insertItem(itemStack2, 0, 1, false)) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }
        return itemStack
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return context.run({ world: World, blockPos: BlockPos ->
            if (world.getBlockState(blockPos).block != DRAWBRIDGE) false
            else player.squaredDistanceTo(
                blockPos.x + .5,
                blockPos.y + .5,
                blockPos.z + .5
            ) < 64.0
        }, true)
    }

}