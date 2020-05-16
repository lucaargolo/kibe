package io.github.lucaargolo.kibe.blocks.vacuum

import io.github.lucaargolo.kibe.blocks.VACUUM_HOPPER
import net.minecraft.container.BlockContext
import net.minecraft.container.Container
import net.minecraft.container.CraftingResultSlot
import net.minecraft.container.Slot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World


class VacuumHopperContainer (syncId: Int, playerInventory: PlayerInventory, val entity: VacuumHopperEntity, val blockContext: BlockContext): Container(null, syncId) {

    var craftingInv = CraftingInventory(this, 1, 1);
    var resultInv = CraftingResultInventory();

    var inventory: Inventory = object: Inventory {
        override fun getInvSize(): Int {
            return entity.invSize
        }

        override fun isInvEmpty(): Boolean {
            return entity.isInvEmpty
        }

        override fun getInvStack(slot: Int): ItemStack? {
            return entity.getInvStack(slot)
        }

        override fun removeInvStack(slot: Int): ItemStack? {
            val stack: ItemStack = entity.removeInvStack(slot)
            onContentChanged(this)
            return stack
        }

        override fun takeInvStack(slot: Int, amount: Int): ItemStack? {
            val stack: ItemStack = entity.takeInvStack(slot, amount)
            onContentChanged(this)
            return stack
        }

        override fun setInvStack(slot: Int, stack: ItemStack?) {
            entity.setInvStack(slot, stack)
            onContentChanged(this)
        }

        override fun markDirty() {
            entity.markDirty()
        }

        override fun canPlayerUseInv(player: PlayerEntity?): Boolean {
            return entity.canPlayerUseInv(player)
        }

        override fun clear() {
            entity.clear()
        }
    }

    init {

        addSlot(CraftingResultSlot(playerInventory.player, craftingInv, resultInv, 0, 8 + 6 * 18, 18 + 2 * 18 ))
        addSlot(Slot(craftingInv, 0, 8 + 6 * 18, 18 ))

        checkContainerSize(inventory, 9)
        inventory.onInvOpen(playerInventory.player)
        val i: Int = (3 - 4) * 18

        var n: Int
        var m: Int

        n = 0
        while (n < 3) {
            m = 0
            while (m < 3) {
                addSlot(Slot(inventory, m + n * 3, 8 + (m+2) * 18, 18 + n * 18))
                ++m
            }
            ++n
        }

        n = 0
        while (n < 3) {
            m = 0
            while (m < 9) {
                addSlot(
                    Slot(
                        playerInventory,
                        m + n * 9 + 9,
                        8 + m * 18,
                        103 + n * 18 + i
                    )
                )
                ++m
            }
            ++n
        }

        n = 0
        while (n < 9) {
            addSlot(Slot(playerInventory, n, 8 + n * 18, 161 + i))
            ++n
        }

    }

    private val player: PlayerEntity = playerInventory.player
    private val world: World = player.world

    override fun sendContentUpdates() {
        updateResult()
        super.sendContentUpdates()
    }

    private fun updateResult() {
        if (!world.isClient) {
            val serverPlayerEntity = player as ServerPlayerEntity
            var itemStack = ItemStack.EMPTY
            if(craftingInv.getInvStack(0).item == Items.GLASS_BOTTLE && entity.liquidXp > 333) {
                itemStack = ItemStack(Items.EXPERIENCE_BOTTLE)
            }
            resultInv.setInvStack(0, itemStack)
            serverPlayerEntity.networkHandler.sendPacket(ContainerSlotUpdateS2CPacket(syncId, 0, itemStack))
        }
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return blockContext.run({ world: World, blockPos: BlockPos ->
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
        if (slot != null && slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (invSlot == 0) {
                blockContext.run({ world, blockPos -> itemStack2.item.onCraft(itemStack2, world, player) })
                if (!insertItem(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY
                }
                slot.onStackChanged(itemStack2, itemStack)
            } else if (invSlot >= 2 && invSlot < 38) {
                if (!insertItem(itemStack2, 1, 2, false)) {
                    if (invSlot < 37) {
                        if (!insertItem(itemStack2, 29, 38, false)) {
                            return ItemStack.EMPTY
                        }
                    } else if (!insertItem(itemStack2, 2, 29, false)) {
                        return ItemStack.EMPTY
                    }
                }
            } else if (!insertItem(itemStack2, 2, 38, false)) {
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
            val itemStack3 = slot.onTakeItem(player, itemStack2)
            if (invSlot == 0) {
                player.dropItem(itemStack3, false)
            }
        }
        return itemStack
    }
}