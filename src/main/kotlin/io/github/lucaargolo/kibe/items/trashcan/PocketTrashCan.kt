package io.github.lucaargolo.kibe.items.trashcan

import io.github.lucaargolo.kibe.items.getItemId
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class PocketTrashCan(settings: Settings): Item(settings) {

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if(!world.isClient) {
            val tag = player.getStackInHand(hand).orCreateTag
            ContainerProviderRegistry.INSTANCE.openContainer(getItemId(this), player as ServerPlayerEntity?) { buf -> buf.writeCompoundTag(tag) }
        }
        return TypedActionResult.success(player.getStackInHand(hand))
    }

}