package io.github.lucaargolo.kibe.utils.menu

import io.github.lucaargolo.kibe.KibeMod
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class BlockScreenHandlerFactory<T: ScreenHandler, B: BlockEntity>(val block: Block, val pos: BlockPos, val consumer: (Int, PlayerInventory, B, ScreenHandlerContext) -> T): ExtendedScreenHandlerFactory {

    private val displayName: Text = Text.translatable("screen.${KibeMod.MOD_ID}.${Registries.BLOCK.getId(block).path}")

    override fun createMenu(syncId: Int, playerInv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val world = player.world
        val be = world.getBlockEntity(pos) as B
        return consumer.invoke(syncId, playerInv, be, ScreenHandlerContext.create(world, pos))
    }

    override fun writeScreenOpeningData(p0: ServerPlayerEntity?, p1: PacketByteBuf?) {
        p1?.writeBlockPos(pos)
    }

    override fun getDisplayName() = displayName

}