package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.menu.CoolerBlockItemScreenHandler
import io.github.lucaargolo.kibe.utils.menu.ItemScreenHandlerFactory
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.tooltip.BundleTooltipComponent
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.BundleContentsComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.tooltip.TooltipData
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import java.util.*

class CoolerBlockItem(settings: Settings): BlockItem(BlockCompendium.COOLER, settings) {

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if(entity is PlayerEntity && entity.currentScreenHandler !is CoolerBlockItemScreenHandler && !entity.isCreative && !entity.isSpectator && entity.canConsume(false)) {
            val rawInventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
            val tag = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA)?.copyNbt() ?: NbtCompound()
            Inventories.readNbt(tag, rawInventory, world.registryManager)
            val foodStack = rawInventory[0]
            if(!foodStack.isEmpty && foodStack.contains(DataComponentTypes.FOOD)) {
                entity.eatFood(world, foodStack, foodStack.get(DataComponentTypes.FOOD))
                Inventories.writeNbt(tag, rawInventory, world.registryManager)
                stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(tag))
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
            player.openHandledScreen(ItemScreenHandlerFactory(this, hand, stack, ::CoolerBlockItemScreenHandler))
            return TypedActionResult.success(stack)
        }
        return super.use(world, player, hand)
    }


    override fun getTooltipData(stack: ItemStack): Optional<TooltipData> {
        return if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            val client = MinecraftClient.getInstance()
            val world = client.world
            if(world != null) {
                val inventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
                val tag = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA)?.copyNbt() ?: NbtCompound()
                Inventories.readNbt(tag, inventory, world.registryManager)
                val contents = BundleContentsComponent(inventory)
                if(!inventory[0].isEmpty) {
                    Optional.of(CoolerTooltipData(contents))
                }else{
                    Optional.empty()
                }
            }else{
                Optional.empty()
            }
        } else {
            Optional.empty()
        }
    }

    data class CoolerTooltipData(val contents: BundleContentsComponent) : TooltipData

    class CoolerTooltipComponent(contents: BundleContentsComponent) : BundleTooltipComponent(contents) {

        override fun getWidth(textRenderer: TextRenderer?): Int {
            return 1
        }

        override fun getHeight(): Int {
            return 1
        }

    }

}