package io.github.lucaargolo.kibe.blocks.vacuum

import io.github.lucaargolo.kibe.SYNCHRONIZE_LAST_RECIPE_PACKET
import io.github.lucaargolo.kibe.blocks.VACUUM_HOPPER
import io.github.lucaargolo.kibe.blocks.getContainerInfo
import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_SERIALIZER
import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_TYPE
import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipe
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*
import kotlin.math.min

class VacuumHopperScreenHandler (syncId: Int, playerInventory: PlayerInventory, val entity: VacuumHopperEntity, private val context: ScreenHandlerContext): ScreenHandler(getContainerInfo(VACUUM_HOPPER)?.handlerType, syncId) {

    private var player: PlayerEntity = playerInventory.player
    private var craftingInv = CraftingInventory(this, 1, 1)
    private var resultInv = CraftingResultInventory()

    var lastRecipe: VacuumHopperRecipe? = null

    var inventory: Inventory = object: Inventory {
        override fun size(): Int {
            return entity.size()
        }

        override fun isEmpty(): Boolean {
            return entity.isEmpty
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
        inventory.onOpen(playerInventory.player)
        val i: Int = (3 - 4) * 18

        addSlot(object: Slot(resultInv, 0, 8 + 6 * 18, 18 + 2 * 18 ) {
            override fun canInsert(itemStack_1: ItemStack?) = false

            override fun onTakeItem(playerEntity: PlayerEntity, itemStack: ItemStack): ItemStack? {
                return if(entity.removeLiquidXp(lastRecipe!!.xpInput)) {
                    slots[1].stack.decrement(1)
                    entity.markDirty()
                    super.onTakeItem(playerEntity, itemStack)
                    itemStack
                }else{
                    ItemStack.EMPTY
                }
            }
        })
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

    override fun onSlotClick(slotId: Int, clickData: Int, actionType: SlotActionType, player: PlayerEntity): ItemStack {
        if(actionType == SlotActionType.QUICK_MOVE && slotId == 0 && slots[0].hasStack()) {
            var maxCraftSize = min(slots[1].stack.count, entity.tanks.first().volume.amount().asInt(1000)/lastRecipe!!.xpInput)
            val maxStackSize = lastRecipe!!.output.maxCount
            if(entity.removeLiquidXp(maxCraftSize*lastRecipe!!.xpInput)) {
                slots[1].stack.decrement(maxCraftSize)
                val craftResult = lastRecipe!!.output.item
                if(maxCraftSize > maxStackSize) {
                    while(maxCraftSize > maxStackSize) {
                        player.giveItemStack(ItemStack(craftResult, maxStackSize))
                        maxCraftSize -= maxStackSize
                    }
                    if(maxCraftSize > 0) {
                        player.giveItemStack(ItemStack(craftResult, maxCraftSize))
                    }
                }else{
                    player.giveItemStack(ItemStack(craftResult, maxCraftSize))
                }
            }
            return ItemStack.EMPTY
        }
        return super.onSlotClick(slotId, clickData, actionType, player)
    }

    private fun updateResult(syncId: Int, world: World, player: PlayerEntity, craftingInventory: CraftingInventory, resultInventory: CraftingResultInventory) {
        if (!world.isClient) {
            val serverPlayerEntity = player as ServerPlayerEntity
            var itemStack = ItemStack.EMPTY
            val optional: Optional<VacuumHopperRecipe> = world.server!!.recipeManager.getFirstMatch(VACUUM_HOPPER_RECIPE_TYPE, craftingInventory, world)
            if (optional.isPresent) {
                val craftingRecipe = optional.get()
                if (resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe) && entity.tanks.first().volume.amount().asInt(1000) >= craftingRecipe.xpInput) {
                    itemStack = craftingRecipe.craft(craftingInventory)
                    lastRecipe = craftingRecipe
                    val passedData = PacketByteBuf(Unpooled.buffer())
                    passedData.writeIdentifier(craftingRecipe.id)
                    VACUUM_HOPPER_RECIPE_SERIALIZER.write(passedData, craftingRecipe)
                    ServerPlayNetworking.send(player, SYNCHRONIZE_LAST_RECIPE_PACKET , passedData)
                }
            }
            resultInventory.setStack(0, itemStack)
            serverPlayerEntity.networkHandler.sendPacket(ScreenHandlerSlotUpdateS2CPacket(syncId, 0, itemStack))
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