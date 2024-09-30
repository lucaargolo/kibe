package io.github.lucaargolo.kibe.utils.menu

import io.github.lucaargolo.kibe.KibeMod
import io.netty.buffer.ByteBuf
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.function.ValueLists
import net.minecraft.world.World

class ItemScreenHandlerFactory<T: ScreenHandler>(val item: Item, val hand: Hand, val stack: ItemStack, private val consumer: (Int, PlayerInventory, Hand, World, ItemStack) -> T): ExtendedScreenHandlerFactory<ItemScreenHandlerFactory.Data> {

    private val displayName: Text = Text.translatable("screen.${KibeMod.MOD_ID}.${Registries.ITEM.getId(item).path}")

    override fun createMenu(syncId: Int, playerInv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val world = player.world
        return consumer.invoke(syncId, playerInv, hand, world, stack)
    }

    override fun getScreenOpeningData(player: ServerPlayerEntity?): Data {
        return Data(hand, stack)
    }

    override fun getDisplayName() = displayName

    data class Data(val hand: Hand, val stack: ItemStack) {

        companion object {
            private val HAND_PACKET_CODEC: PacketCodec<ByteBuf, Hand> = PacketCodecs.indexed(ValueLists.createIdToValueFunction(
                Hand::ordinal, Hand.entries.toTypedArray(), ValueLists.OutOfBoundsHandling.WRAP
            ), Hand::ordinal)

            val PACKET_CODEC = PacketCodec.tuple(HAND_PACKET_CODEC, Data::hand, ItemStack.PACKET_CODEC, Data::stack, ::Data)
        }

    }

}