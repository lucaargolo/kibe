package io.github.lucaargolo.kibe.blocks.witherbuilder

import io.github.lucaargolo.kibe.blocks.WITHER_BUILDER
import io.github.lucaargolo.kibe.blocks.getContainerInfo
import io.github.lucaargolo.kibe.utils.BlockEntityInventory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class WitherBuilderScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val entity: WitherBuilderBlockEntity, private val context: ScreenHandlerContext): ScreenHandler(getContainerInfo(WITHER_BUILDER)?.handlerType, syncId)  {

    val inventory = BlockEntityInventory(this, entity)

    init {
        checkSize(inventory, 7)
        inventory.onOpen(playerInventory.player)

        addSlot(object: Slot(inventory, 0, 80, 54) {
            override fun canInsert(stack: ItemStack) = (stack.item as? BlockItem)?.block?.registryEntry?.isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS) ?: false
        })

        (0..2).forEach { n ->
            addSlot(object: Slot(inventory, 1+n, 62 + n*18, 36) {
                override fun canInsert(stack: ItemStack) = (stack.item as? BlockItem)?.block?.registryEntry?.isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS) ?: false
            })
            addSlot(object: Slot(inventory, 4+n, 62 + n*18, 18) {
                override fun canInsert(stack: ItemStack) = stack.item == Items.WITHER_SKELETON_SKULL
            })
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
        if (slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (invSlot < 7) {
                if (!insertItem(itemStack2, 7, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, 0, 7, false)) {
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
        return context.get({ world: World, blockPos: BlockPos ->
            if (world.getBlockState(blockPos).block != WITHER_BUILDER) false
            else player.squaredDistanceTo(
                blockPos.x + .5,
                blockPos.y + .5,
                blockPos.z + .5
            ) < 64.0
        }, true)
    }

}