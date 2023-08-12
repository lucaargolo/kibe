package io.github.lucaargolo.kibe.items.cooler

import io.github.lucaargolo.kibe.blocks.COOLER
import io.github.lucaargolo.kibe.utils.ItemScreenHandlerFactory
import net.minecraft.client.item.BundleTooltipData
import net.minecraft.client.item.TooltipData
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import java.util.*

class CoolerBlockItem(settings: Settings): BlockItem(COOLER, settings) {

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if(entity is PlayerEntity && entity.currentScreenHandler !is CoolerBlockItemScreenHandler && !entity.isCreative && !entity.isSpectator && !entity.isInvulnerable && !entity.abilities.invulnerable && entity.canConsume(false)) {
            val rawInventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
            val tag = stack.orCreateNbt.getCompound("BlockEntityTag")
            Inventories.readNbt(tag, rawInventory)
            val foodStack = rawInventory[0]
            if(!foodStack.isEmpty && foodStack.isFood) {
                entity.eatFood(world, foodStack)
                Inventories.writeNbt(tag, rawInventory)
                stack.orCreateNbt.put("BlockEntityTag", tag)
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if(context.player?.isSneaking == true)
            return use(context.world, context.player, context.hand).result
        return super.useOnBlock(context)
    }

    override fun use(world: World, player: PlayerEntity?, hand: Hand): TypedActionResult<ItemStack> {
        if(!world.isClient) player?.let {
            val stack = player.getStackInHand(hand)
            val tag = stack.orCreateNbt.getCompound("BlockEntityTag")
            player.openHandledScreen(ItemScreenHandlerFactory(this, hand, tag))
            return TypedActionResult.success(stack)
        }
        return super.use(world, player, hand)
    }

    override fun getTooltipData(stack: ItemStack): Optional<TooltipData> {
        val rawInventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
        val tag = stack.orCreateNbt.getCompound("BlockEntityTag")
        Inventories.readNbt(tag, rawInventory)
        return if(!rawInventory[0].isEmpty) Optional.of(CoolerTooltipData(rawInventory)) else Optional.empty()
    }

}