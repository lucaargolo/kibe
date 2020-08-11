package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.blocks.*
import io.github.lucaargolo.kibe.items.*
import net.minecraft.block.Block
import net.minecraft.item.ItemConvertible
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

val tooltipRegistry = mutableMapOf<ItemConvertible, List<Text>>()

fun initTooltip() {
    registerTooltip(CURSED_LASSO, 2)
    registerTooltip(GOLDEN_LASSO)
    registerTooltip(DIAMOND_LASSO)

    registerTooltip(LIGHT_SOURCE)

    registerTooltip(IRON_SPIKES)
    registerTooltip(DIAMOND_SPIKES)

    registerTooltip(BIG_TORCH)
    registerTooltip(CHUNK_LOADER)

    registerTooltip(KIBE)
    registerTooltip(GOLDEN_KIBE)
    registerTooltip(CURSED_KIBE)
    registerTooltip(DIAMOND_KIBE)
}

fun registerTooltip(item: ItemConvertible) {
    val id = getIdentifier(item)
    tooltipRegistry[item] = arrayListOf(TranslatableText("tooltip.kibe.lore.${id!!.path}"))
}

fun registerTooltip(item: ItemConvertible, number: Int) {
    val id = getIdentifier(item)
    val list = mutableListOf<Text>()
    (1..number).forEach {
        list.add(TranslatableText("tooltip.kibe.lore.${id!!.path}.${it}"))
    }
    tooltipRegistry[item] = list
}

private fun getIdentifier(item: ItemConvertible): Identifier? {
    return if(item is Block) {
        getBlockId(item)
    }else{
        getItemId(item.asItem())
    }
}