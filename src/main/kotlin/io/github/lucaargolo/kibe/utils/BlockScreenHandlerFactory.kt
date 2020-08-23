package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.blocks.getContainerInfo
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos

class BlockScreenHandlerFactory(val block: Block, val pos: BlockPos): ExtendedScreenHandlerFactory {

    override fun createMenu(syncId: Int, playerInv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val world = player.world
        val be = world.getBlockEntity(pos)
        return getContainerInfo(block)!!.handlerClass.java.constructors[0].newInstance(
            syncId, playerInv, be, ScreenHandlerContext.create(world, pos)
        ) as ScreenHandler
    }

    override fun writeScreenOpeningData(p0: ServerPlayerEntity?, p1: PacketByteBuf?) {
        p1?.writeBlockPos(pos)
    }

    override fun getDisplayName() = getContainerInfo(block)!!.title

}