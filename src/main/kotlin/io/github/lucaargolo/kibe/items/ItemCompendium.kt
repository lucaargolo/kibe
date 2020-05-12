package io.github.lucaargolo.kibe.items

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.items.miscellaneous.CursedSeeds
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

val itemRegistry = mutableMapOf<Identifier, Item>()

val CURSED_DROPLETS = register(Identifier(MOD_ID, "cursed_droplets"), Item(Item.Settings().group(ItemGroup.MISC)))
val CURSED_DROPS = register(Identifier(MOD_ID, "cursed_drops"), Item(Item.Settings().group(ItemGroup.MISC)))
val SOLID_CURSES = register(Identifier(MOD_ID, "solid_curses"), Item(Item.Settings().group(ItemGroup.MISC)))

val CURSED_SEEDS = register(Identifier(MOD_ID, "cursed_seeds"), CursedSeeds(Item.Settings().group(ItemGroup.MISC)))

val BLANK_RUNE = register(Identifier(MOD_ID, "blank_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val WHITE_RUNE = register(Identifier(MOD_ID, "white_rune"), Rune(DyeColor.WHITE, Item.Settings().group(ItemGroup.MISC)))
val ORANGE_RUNE = register(Identifier(MOD_ID, "orange_rune"), Rune(DyeColor.ORANGE, Item.Settings().group(ItemGroup.MISC)))
val MAGENTA_RUNE = register(Identifier(MOD_ID, "magenta_rune"), Rune(DyeColor.MAGENTA, Item.Settings().group(ItemGroup.MISC)))
val LIGHT_BLUE_RUNE = register(Identifier(MOD_ID, "light_blue_rune"), Rune(DyeColor.LIGHT_BLUE, Item.Settings().group(ItemGroup.MISC)))
val YELLOW_RUNE = register(Identifier(MOD_ID, "yellow_rune"), Rune(DyeColor.YELLOW, Item.Settings().group(ItemGroup.MISC)))
val LIME_RUNE = register(Identifier(MOD_ID, "lime_rune"), Rune(DyeColor.LIME, Item.Settings().group(ItemGroup.MISC)))
val PINK_RUNE = register(Identifier(MOD_ID, "pink_rune"), Rune(DyeColor.PINK, Item.Settings().group(ItemGroup.MISC)))
val GRAY_RUNE = register(Identifier(MOD_ID, "gray_rune"), Rune(DyeColor.GRAY, Item.Settings().group(ItemGroup.MISC)))
val LIGHT_GRAY_RUNE = register(Identifier(MOD_ID, "light_gray_rune"), Rune(DyeColor.LIGHT_GRAY, Item.Settings().group(ItemGroup.MISC)))
val CYAN_RUNE = register(Identifier(MOD_ID, "cyan_rune"), Rune(DyeColor.CYAN, Item.Settings().group(ItemGroup.MISC)))
val BLUE_RUNE = register(Identifier(MOD_ID, "blue_rune"), Rune(DyeColor.BLUE, Item.Settings().group(ItemGroup.MISC)))
val PURPLE_RUNE = register(Identifier(MOD_ID, "purple_rune"), Rune(DyeColor.PURPLE, Item.Settings().group(ItemGroup.MISC)))
val GREEN_RUNE = register(Identifier(MOD_ID, "green_rune"), Rune(DyeColor.GREEN, Item.Settings().group(ItemGroup.MISC)))
val BROWN_RUNE = register(Identifier(MOD_ID, "brown_rune"), Rune(DyeColor.BROWN, Item.Settings().group(ItemGroup.MISC)))
val RED_RUNE = register(Identifier(MOD_ID, "red_rune"), Rune(DyeColor.RED, Item.Settings().group(ItemGroup.MISC)))
val BLACK_RUNE = register(Identifier(MOD_ID, "black_rune"), Rune(DyeColor.BLACK, Item.Settings().group(ItemGroup.MISC)))


private fun register(identifier: Identifier, item: Item): Item {
    itemRegistry[identifier] = item
    return item;
}

fun initItems() {
    itemRegistry.forEach{ Registry.register(Registry.ITEM, it.key, it.value) }
}
