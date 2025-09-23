package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.menu.PocketTrashCanScreenHandler
import io.github.lucaargolo.kibe.utils.menu.ItemScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class PocketTrashCan(settings: Settings): Item(settings) {

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        player.openHandledScreen(ItemScreenHandlerFactory(this, hand, player.getStackInHand(hand), ::PocketTrashCanScreenHandler))
        return TypedActionResult.success(player.getStackInHand(hand))
    }

}