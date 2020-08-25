package io.github.lucaargolo.kibe.items.trashcan

import io.github.lucaargolo.kibe.utils.ItemScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class PocketTrashCan(settings: Settings): Item(settings) {

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val tag = player.getStackInHand(hand).orCreateTag
        player.openHandledScreen(ItemScreenHandlerFactory(this, hand, tag))
        return TypedActionResult.success(player.getStackInHand(hand))
    }

}