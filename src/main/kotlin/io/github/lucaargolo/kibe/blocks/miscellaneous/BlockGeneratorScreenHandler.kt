package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getContainerInfo
import io.github.lucaargolo.kibe.utils.BlockEntityInventory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockGeneratorScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val entity: BlockGeneratorBlockEntity, private val context: ScreenHandlerContext): ScreenHandler(getContainerInfo(entity.generator)?.handlerType, syncId)  {

    val inventory = BlockEntityInventory(this, entity)

    init {
        checkSize(inventory, 27)
        inventory.onOpen(playerInventory.player)

        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(object: Slot(inventory, m + n * 9, 8 + (m * 18), 36 + n * 18) {
                    override fun canInsert(stack: ItemStack) = false
                })
            }
        }


        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 104 + n*18))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 162))
        }
    }

    override fun transferSlot(player: PlayerEntity?, invSlot: Int): ItemStack? {
        val slot = this.slots[invSlot]
        if (slot.hasStack() && invSlot < 27) {
            val itemStack = slot.stack
            if (!insertItem(itemStack, 27, this.slots.size, true)) {
                return ItemStack.EMPTY
            }
            if (itemStack.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
            return itemStack
        }
        return ItemStack.EMPTY
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return context.get({ world: World, blockPos: BlockPos ->
            if (world.getBlockState(blockPos).block != entity.generator) false
            else player.squaredDistanceTo(
                blockPos.x + .5,
                blockPos.y + .5,
                blockPos.z + .5
            ) < 64.0
        }, true)
    }

}