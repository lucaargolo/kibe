package io.github.lucaargolo.kibe.items

import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

private val registry = mutableMapOf<Identifier, Item>()

val CURSED_DROPLETS = register(Identifier("cursed_droplets"), Item(Item.Settings().group(ItemGroup.MISC)))
val CURSED_DROPS = register(Identifier("cursed_drops"), Item(Item.Settings().group(ItemGroup.MISC)))
val SOLID_CURSES = register(Identifier("solid_curses"), Item(Item.Settings().group(ItemGroup.MISC)))

private fun register(identifier: Identifier, item: Item): Item {
    registry[identifier] = item
    return item;
}

fun initItems() {
    registry.forEach{ Registry.register(Registry.ITEM, it.key, it.value) }
}
