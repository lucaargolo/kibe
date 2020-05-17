package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.items.WHITE_RUNE
import io.github.lucaargolo.kibe.items.itemRegistry
import net.minecraft.item.Item
import net.minecraft.util.DyeColor

class Rune(val color: DyeColor, settings: Settings): Item(settings) {

    companion object {
        fun getRuneByColor(color: DyeColor): Rune {
            itemRegistry.forEach { (_, modItem) -> if(modItem.item is Rune && (modItem.item as Rune).color == color) return modItem.item as Rune }
            return WHITE_RUNE as Rune
        }
    }

}