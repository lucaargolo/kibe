package io.github.lucaargolo.kibe.utils.helper

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.item.ItemCompendium
import net.minecraft.block.Block
import net.minecraft.item.ItemConvertible
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object TooltipHelper {

    val tooltipRegistry = mutableMapOf<ItemConvertible, List<Text>>()

    fun initialize() {
        register(ItemCompendium.CURSED_LASSO, 2)
        register(ItemCompendium.GOLDEN_LASSO)
        register(ItemCompendium.DIAMOND_LASSO)

        register(BlockCompendium.LIGHT_SOURCE)

        register(BlockCompendium.STONE_SPIKES)
        register(BlockCompendium.IRON_SPIKES)
        register(BlockCompendium.GOLD_SPIKES)
        register(BlockCompendium.DIAMOND_SPIKES)

        register(BlockCompendium.HEATER)
        register(BlockCompendium.DEHUMIDIFIER)

        register(BlockCompendium.COBBLESTONE_GENERATOR_MK1)
        register(BlockCompendium.COBBLESTONE_GENERATOR_MK2)
        register(BlockCompendium.COBBLESTONE_GENERATOR_MK3)
        register(BlockCompendium.COBBLESTONE_GENERATOR_MK4)
        register(BlockCompendium.COBBLESTONE_GENERATOR_MK5)

        register(BlockCompendium.BASALT_GENERATOR_MK1)
        register(BlockCompendium.BASALT_GENERATOR_MK2)
        register(BlockCompendium.BASALT_GENERATOR_MK3)
        register(BlockCompendium.BASALT_GENERATOR_MK4)
        register(BlockCompendium.BASALT_GENERATOR_MK5)

        register(BlockCompendium.COOLER)
        register(BlockCompendium.BIG_TORCH)
        register(BlockCompendium.CHUNK_LOADER)

        register(ItemCompendium.KIBE)
        register(ItemCompendium.GOLDEN_KIBE)
        register(ItemCompendium.CURSED_KIBE)
        register(ItemCompendium.DIAMOND_KIBE)
    }

    fun register(item: ItemConvertible) {
        val id = getIdentifier(item)
        tooltipRegistry[item] = arrayListOf(Text.translatable("tooltip.kibe.lore.${id.path}"))
    }

    fun register(item: ItemConvertible, number: Int) {
        val id = getIdentifier(item)
        val list = mutableListOf<Text>()
        (1..number).forEach {
            list.add(Text.translatable("tooltip.kibe.lore.${id.path}.${it}"))
        }
        tooltipRegistry[item] = list
    }

    private fun getIdentifier(item: ItemConvertible): Identifier {
        return if (item is Block) {
            Registries.BLOCK.getId(item)
        } else {
            Registries.ITEM.getId(item.asItem())
        }
    }

}