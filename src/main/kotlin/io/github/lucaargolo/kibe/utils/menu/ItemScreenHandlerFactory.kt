package io.github.lucaargolo.kibe.utils.menu

import io.github.lucaargolo.kibe.KibeMod
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.world.World

class ItemScreenHandlerFactory<T: ScreenHandler>(val item: Item, val hand: Hand, val tag: NbtCompound, val consumer: (Int, PlayerInventory, Hand, World, NbtCompound) -> T): ExtendedScreenHandlerFactory {

    private val displayName: Text = Text.translatable("screen.${KibeMod.MOD_ID}.${Registries.ITEM.getId(item).path}")

    override fun createMenu(syncId: Int, playerInv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val world = player.world
        return consumer.invoke(syncId, playerInv, hand, world, tag)
    }

    override fun writeScreenOpeningData(p0: ServerPlayerEntity?, p1: PacketByteBuf?) {
        p1?.writeEnumConstant(hand)
        p1?.writeNbt(tag)
    }

    override fun getDisplayName() = displayName

}