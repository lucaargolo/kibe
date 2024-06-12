package io.github.lucaargolo.kibe.menu

import io.github.lucaargolo.kibe.client.screen.*
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Hand
import net.minecraft.world.World

object ScreenHandlerCompendium : RegistryCompendium<ScreenHandlerType<*>>(Registries.SCREEN_HANDLER) {

    val ENTANGLED_CHEST = register("entangled_chest", blockEntityHandler(::EntangledChestScreenHandler))
    val TRASH_CAN = register("trash_can", blockEntityHandler(::TrashCanScreenHandler))
    val VACUUM_HOPPER = register("vacuum_hopper", blockEntityHandler(::VacuumHopperScreenHandler))
    val BIG_TORCH = register("big_torch", blockEntityHandler(::BigTorchScreenHandler))
    val COOLER = register("cooler", blockEntityHandler(::CoolerScreenHandler))
    val DRAWBRIDGE = register("drawbridge", blockEntityHandler(::DrawbridgeScreenHandler))
    val WITHER_BUILDER = register("wither_builder", blockEntityHandler(::WitherBuilderScreenHandler))
    val PLACER = register("placer", blockEntityHandler(::PlacerScreenHandler))
    val BREAKER = register("breaker", blockEntityHandler(::BreakerScreenHandler))
    val BLOCK_GENERATOR = register("block_generator", blockEntityHandler(::BlockGeneratorScreenHandler))
    val POCKET_TRASH_CAN = register("pocket_trash_can", itemHandler(::PocketTrashCanScreenHandler))
    val ENTANGLED_BAG = register("entangled_bag", itemHandler(::EntangledBagScreenHandler))
    val COOLER_ITEM = register("cooler_item", itemHandler(::CoolerBlockItemScreenHandler))

    private fun <T: ScreenHandler, B: BlockEntity> blockEntityHandler(consumer: (Int, PlayerInventory, B, ScreenHandlerContext) -> T): ExtendedScreenHandlerType<T> {
        return ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
            val pos = packetByteBuf.readBlockPos()
            val player = playerInventory.player
            val world = player.world
            val be = world.getBlockEntity(pos) as B
            consumer.invoke(i, playerInventory, be, ScreenHandlerContext.create(world, pos))
        }
    }

    private fun <T: ScreenHandler> itemHandler(consumer: (Int, PlayerInventory, Hand, World, NbtCompound) -> T): ExtendedScreenHandlerType<T> {
        return ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
            val hand = packetByteBuf.readEnumConstant(Hand::class.java)
            val tag = packetByteBuf.readNbt()!!
            consumer.invoke(i, playerInventory, hand, playerInventory.player.world, tag)
        }
    }

    override fun initializeClient() {
        HandledScreens.register(ENTANGLED_CHEST, ::EntangledChestScreen)
        HandledScreens.register(TRASH_CAN, ::TrashCanScreen)
        HandledScreens.register(VACUUM_HOPPER, ::VacuumHopperScreen)
        HandledScreens.register(BIG_TORCH, ::BigTorchScreen)
        HandledScreens.register(COOLER, ::CoolerScreen)
        HandledScreens.register(DRAWBRIDGE, ::DrawbridgeScreen)
        HandledScreens.register(WITHER_BUILDER, ::WitherBuilderScreen)
        HandledScreens.register(PLACER, ::PlacerScreen)
        HandledScreens.register(BREAKER, ::BreakerScreen)
        HandledScreens.register(BLOCK_GENERATOR, ::BlockGeneratorScreen)
        HandledScreens.register(POCKET_TRASH_CAN, ::PocketTrashCanScreen)
        HandledScreens.register(ENTANGLED_BAG, ::EntangledBagScreen)
        HandledScreens.register(COOLER_ITEM, ::CoolerBlockItemScreen)

    }

}