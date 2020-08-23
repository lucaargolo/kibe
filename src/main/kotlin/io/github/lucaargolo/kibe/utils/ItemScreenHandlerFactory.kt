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

class ItemScreenHandlerFactory(val item: Item, val tag: CompoundTag, val slot: Int = 0): ExtendedScreenHandlerFactory {

    override fun createMenu(syncId: Int, playerInv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val world = player.world
        return getContainerInfo(item)!!.handlerClass.java.constructors[0].newInstance(
            syncId, playerInv, slot, world, tag
        ) as ScreenHandler
    }

    override fun writeScreenOpeningData(p0: ServerPlayerEntity?, p1: PacketByteBuf?) {
        p1?.writeInt(slot)
        p1?.writeCompoundTag(tag)
    }

    override fun getDisplayName() = getContainerInfo(item)!!.title

}