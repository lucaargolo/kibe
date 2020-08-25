package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.items.getContainerInfo
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand

class ItemScreenHandlerFactory(val item: Item, val hand: Hand, val tag: CompoundTag): ExtendedScreenHandlerFactory {

    override fun createMenu(syncId: Int, playerInv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val world = player.world
        return getContainerInfo(item)!!.handlerClass.java.constructors[0].newInstance(
            syncId, playerInv, hand, world, tag
        ) as ScreenHandler
    }

    override fun writeScreenOpeningData(p0: ServerPlayerEntity?, p1: PacketByteBuf?) {
        p1?.writeEnumConstant(hand)
        p1?.writeCompoundTag(tag)
    }

    override fun getDisplayName() = getContainerInfo(item)!!.title

}