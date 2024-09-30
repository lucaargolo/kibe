package io.github.lucaargolo.kibe.menu

import io.github.lucaargolo.kibe.client.screen.*
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import io.github.lucaargolo.kibe.utils.menu.ItemScreenHandlerFactory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object ScreenHandlerCompendium : RegistryCompendium<ScreenHandlerType<*>>(Registries.SCREEN_HANDLER) {

    val ENTANGLED_CHEST = register("entangled_chest", blockHandler(::EntangledChestScreenHandler))
    val TRASH_CAN = register("trash_can", blockHandler(::TrashCanScreenHandler))
    val VACUUM_HOPPER = register("vacuum_hopper", blockHandler(::VacuumHopperScreenHandler))
    val BIG_TORCH = register("big_torch", blockHandler(::BigTorchScreenHandler))
    val COOLER = register("cooler", blockHandler(::CoolerScreenHandler))
    val DRAWBRIDGE = register("drawbridge", blockHandler(::DrawbridgeScreenHandler))
    val WITHER_BUILDER = register("wither_builder", blockHandler(::WitherBuilderScreenHandler))
    val PLACER = register("placer", blockHandler(::PlacerScreenHandler))
    val BREAKER = register("breaker", blockHandler(::BreakerScreenHandler))
    val BLOCK_GENERATOR = register("block_generator", blockHandler(::BlockGeneratorScreenHandler))
    val POCKET_TRASH_CAN = register("pocket_trash_can", itemHandler(::PocketTrashCanScreenHandler))
    val ENTANGLED_BAG = register("entangled_bag", itemHandler(::EntangledBagScreenHandler))
    val COOLER_ITEM = register("cooler_item", itemHandler(::CoolerBlockItemScreenHandler))

    @Suppress("UNCHECKED_CAST")
    private fun <T: ScreenHandler, B: BlockEntity> blockHandler(consumer: (Int, PlayerInventory, B, ScreenHandlerContext) -> T): ExtendedScreenHandlerType<T, BlockPos> {
        return ExtendedScreenHandlerType({ i, playerInventory, pos ->
            val player = playerInventory.player
            val world = player.world
            val be = world.getBlockEntity(pos) as B
            consumer.invoke(i, playerInventory, be, ScreenHandlerContext.create(world, pos))
        }, BlockPos.PACKET_CODEC)
    }

    private fun <T: ScreenHandler> itemHandler(consumer: (Int, PlayerInventory, Hand, World, ItemStack) -> T): ExtendedScreenHandlerType<T, ItemScreenHandlerFactory.Data> {
        return ExtendedScreenHandlerType({ i, playerInventory, data -> consumer.invoke(i, playerInventory, data.hand, playerInventory.player.world, data.stack)}, ItemScreenHandlerFactory.Data.PACKET_CODEC)
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