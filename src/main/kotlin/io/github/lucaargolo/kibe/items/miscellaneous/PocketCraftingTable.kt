package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.mixin.CraftingTableContainerMixin
import net.minecraft.container.ContainerFactory
import net.minecraft.container.CraftingTableContainer
import net.minecraft.container.SimpleNamedContainerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.TranslatableText
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

@Suppress("CAST_NEVER_SUCCEEDS")
class PocketCraftingTable(settings: Settings): Item(settings) {

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if(!world.isClient) {
            user.openContainer(SimpleNamedContainerFactory(ContainerFactory { i, playerInventory, _ ->
                object: CraftingTableContainer(i, playerInventory)  {
                    override fun onContentChanged(inventory: Inventory?) {
                        updateResult(syncId, world, (this as CraftingTableContainerMixin).player, (this as CraftingTableContainerMixin).craftingInv, (this as CraftingTableContainerMixin).resultInv)
                    }

                    override fun close(player: PlayerEntity?) {
                        super.close(player)
                        dropInventory(player, world, (this as CraftingTableContainerMixin).craftingInv)
                    }
                }
            }, TranslatableText("screen.kibe.pocket_crafting_table")))
        }
        return TypedActionResult.success(user.getStackInHand(hand))
    }


}