package io.github.lucaargolo.kibe

import io.github.lucaargolo.kibe.entangled.EntangledHandler
import io.github.lucaargolo.kibe.miscellaneous.ConveyorBelt
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

val MOD_ID = "kibe"
val ENTANGLED_HANDLER = EntangledHandler()

@Suppress("unused")
fun init() {

    ENTANGLED_HANDLER.init()

    val REGULAR_CONVEYOR_BELT = ConveyorBelt(0.125F)
    val FAST_CONVEYOR_BELT = ConveyorBelt(0.25F)
    val EXPRESS_CONVEYOR_BELT = ConveyorBelt(0.5F)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "regular_conveyor_belt"), REGULAR_CONVEYOR_BELT)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "regular_conveyor_belt"), BlockItem(REGULAR_CONVEYOR_BELT, Item.Settings().group(ItemGroup.MISC)))
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "fast_conveyor_belt"), FAST_CONVEYOR_BELT)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "fast_conveyor_belt"), BlockItem(FAST_CONVEYOR_BELT, Item.Settings().group(ItemGroup.MISC)))
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "express_conveyor_belt"), EXPRESS_CONVEYOR_BELT)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "express_conveyor_belt"), BlockItem(EXPRESS_CONVEYOR_BELT, Item.Settings().group(ItemGroup.MISC)))

}

@Suppress("unused")
fun initClient() {

    ENTANGLED_HANDLER.initClient()
}

