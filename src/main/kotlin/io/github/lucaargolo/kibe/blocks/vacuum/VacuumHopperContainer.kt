package io.github.lucaargolo.kibe.blocks.vacuum

import io.github.lucaargolo.kibe.blocks.VACUUM_HOPPER
import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_TYPE
import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipe
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
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*


class VacuumHopperContainer (syncId: Int, playerInventory: PlayerInventory, val entity: VacuumHopperEntity, private val context: BlockContext): Container(null, syncId) {

    private var resultInv = CraftingResultInventory()
    private var player: PlayerEntity = playerInventory.player

    private var craftingInv = CraftingInventory(this, 1, 1)

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
        checkContainerSize(inventory, 9)
        inventory.onInvOpen(playerInventory.player)
        val i: Int = (3 - 4) * 18

        addSlot(CraftingResultSlot(playerInventory.player, craftingInv, resultInv, 0, 8 + 6 * 18, 18 + 2 * 18 ))
        addSlot(Slot(craftingInv, 0, 8 + 6 * 18, 18 ))

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

        updateResult(syncId, player.world, player, craftingInv, resultInv)
    }

    private fun updateResult(syncId: Int, world: World, player: PlayerEntity, craftingInventory: CraftingInventory, resultInventory: CraftingResultInventory) {
        if (!world.isClient) {
            val serverPlayerEntity = player as ServerPlayerEntity
            var itemStack = ItemStack.EMPTY
            val optional: Optional<VacuumHopperRecipe> = world.server!!.recipeManager.getFirstMatch(VACUUM_HOPPER_RECIPE_TYPE, craftingInventory, world)
            if (optional.isPresent) {
                val craftingRecipe = optional.get()
                if (resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe)) {
                    itemStack = craftingRecipe.craft(craftingInventory)
                }
            }
            resultInventory.setInvStack(0, itemStack)
            serverPlayerEntity.networkHandler.sendPacket(ContainerSlotUpdateS2CPacket(syncId, 0, itemStack))
        }
    }

    override fun sendContentUpdates() {
        updateResult(syncId, player.world, player, craftingInv, resultInv)
        super.sendContentUpdates()
    }

    override fun close(player: PlayerEntity?) {
        super.close(player)
        context.run { world: World, _: BlockPos ->
            dropInventory(player, world, craftingInv)
        }
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return context.run({ world: World, blockPos: BlockPos ->
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
                context.run { world: World?, _: BlockPos? ->
                    itemStack2.item.onCraft(itemStack2, world, player)
                }
                if (!insertItem(itemStack2, 11, 47, true)) {
                    return ItemStack.EMPTY
                }
                slot.onStackChanged(itemStack2, itemStack)
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
            val itemStack3 = slot.onTakeItem(player, itemStack2)
            if (invSlot == 0) {
                player.dropItem(itemStack3, false)
            }
        }

        return itemStack
    }

    override fun canInsertIntoSlot(stack: ItemStack, slot: Slot): Boolean {
        return slot.inventory !== resultInv && super.canInsertIntoSlot(stack, slot)
    }

}