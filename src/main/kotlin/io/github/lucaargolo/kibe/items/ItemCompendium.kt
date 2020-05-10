package io.github.lucaargolo.kibe.items

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.items.miscellaneous.CursedSeeds
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

private val registry = mutableMapOf<Identifier, Item>()

val CURSED_DROPLETS = register(Identifier(MOD_ID, "cursed_droplets"), Item(Item.Settings().group(ItemGroup.MISC)))
val CURSED_DROPS = register(Identifier(MOD_ID, "cursed_drops"), Item(Item.Settings().group(ItemGroup.MISC)))
val SOLID_CURSES = register(Identifier(MOD_ID, "solid_curses"), Item(Item.Settings().group(ItemGroup.MISC)))

val CURSED_SEEDS = register(Identifier(MOD_ID, "cursed_seeds"), CursedSeeds(Item.Settings().group(ItemGroup.MISC)))

val BLANK_RUNE = register(Identifier(MOD_ID, "blank_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val WHITE_RUNE = register(Identifier(MOD_ID, "white_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val ORANGE_RUNE = register(Identifier(MOD_ID, "orange_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val MAGENTA_RUNE = register(Identifier(MOD_ID, "magenta_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val LIGHT_BLUE_RUNE = register(Identifier(MOD_ID, "light_blue_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val YELLOW_RUNE = register(Identifier(MOD_ID, "yellow_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val LIME_RUNE = register(Identifier(MOD_ID, "lime_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val PINK_RUNE = register(Identifier(MOD_ID, "pink_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val GRAY_RUNE = register(Identifier(MOD_ID, "gray_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val LIGHT_GRAY_RUNE = register(Identifier(MOD_ID, "light_gray_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val CYAN_RUNE = register(Identifier(MOD_ID, "cyan_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val BLUE_RUNE = register(Identifier(MOD_ID, "blue_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val PURPLE_RUNE = register(Identifier(MOD_ID, "purple_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val GREEN_RUNE = register(Identifier(MOD_ID, "green_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val BROWN_RUNE = register(Identifier(MOD_ID, "brown_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val RED_RUNE = register(Identifier(MOD_ID, "red_rune"), Item(Item.Settings().group(ItemGroup.MISC)))
val BLACK_RUNE = register(Identifier(MOD_ID, "black_rune"), Item(Item.Settings().group(ItemGroup.MISC)))


private fun register(identifier: Identifier, item: Item): Item {
    registry[identifier] = item
    return item;
}

fun initItems() {
    registry.forEach{ Registry.register(Registry.ITEM, it.key, it.value) }
}
