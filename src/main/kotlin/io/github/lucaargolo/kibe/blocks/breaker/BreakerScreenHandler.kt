package io.github.lucaargolo.kibe.blocks.breaker

import io.github.lucaargolo.kibe.blocks.BREAKER
import io.github.lucaargolo.kibe.blocks.getContainerInfo
import io.github.lucaargolo.kibe.utils.BlockEntityInventory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BreakerScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val entity: BreakerBlockEntity, private val context: ScreenHandlerContext): ScreenHandler(getContainerInfo(BREAKER)?.handlerType, syncId)  {

    val inventory = BlockEntityInventory(this, entity)

    init {
        checkSize(inventory, 9)
        inventory.onOpen(playerInventory.player)

        (0..2).forEach { n ->
            (0..2).forEach { m ->
                addSlot(object: Slot(inventory, m + n * 3, 26 + (m+2) * 18, 18 + n * 18) {
                    override fun canInsert(stack: ItemStack) = stack.item is BlockItem
                })
            }
        }


        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 86 + n*18))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 144))
        }
    }

    override fun transferSlot(player: PlayerEntity?, invSlot: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[invSlot]
        if (slot != null && slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (invSlot < 9) {
                if (!insertItem(itemStack2, 9, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, 0, 9, false)) {
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
            if (world.getBlockState(blockPos).block != BREAKER) false
            else player.squaredDistanceTo(
                blockPos.x + .5,
                blockPos.y + .5,
                blockPos.z + .5
            ) < 64.0
        }, true)
    }

}