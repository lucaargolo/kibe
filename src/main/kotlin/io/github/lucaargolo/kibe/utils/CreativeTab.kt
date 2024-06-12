@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.fluid.FluidCompendium
import io.github.lucaargolo.kibe.fluid.miscellaneous.ModdedFluid
import io.github.lucaargolo.kibe.item.ItemCompendium
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.minecraft.block.Block
import net.minecraft.fluid.Fluids
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text

object CreativeTab {

    fun initialize() {
        Registry.register(Registries.ITEM_GROUP, ModIdentifier("creative_tab"), FabricItemGroup.builder()
            .icon { ItemStack(ItemCompendium.KIBE) }
            .displayName(Text.translatable("itemGroup.kibe.creative_tab"))
            .entries { _, entries -> entries.addAll(appendItems()) }
            .build()
        )
    }

    private fun appendItems(): List<ItemStack> {
        val list = mutableListOf<ItemStack>()
        val order = arrayOf(
            ItemCompendium.KIBE, ItemCompendium.CURSED_KIBE, ItemCompendium.GOLDEN_KIBE, ItemCompendium.DIAMOND_KIBE,
            BlockCompendium.CURSED_DIRT, ItemCompendium.CURSED_DROPLETS, ItemCompendium.CURSED_SEEDS, ItemCompendium.CURSED_LASSO, ItemCompendium.GOLDEN_LASSO, ItemCompendium.DIAMOND_LASSO,
            BlockCompendium.ENTANGLED_CHEST, ItemCompendium.ENTANGLED_BAG, BlockCompendium.ENTANGLED_TANK, ItemCompendium.ENTANGLED_BUCKET, BlockCompendium.FLUID_HOPPER, BlockCompendium.OBSIDIAN_SAND, BlockCompendium.WITHER_PROOF_BLOCK,
            BlockCompendium.WITHER_PROOF_SAND, BlockCompendium.WITHER_PROOF_GLASS, BlockCompendium.WITHER_BUILDER, BlockCompendium.PLACER, BlockCompendium.BREAKER, BlockCompendium.IGNITER, BlockCompendium.DRAWBRIDGE,
            ItemCompendium.POCKET_TRASH_CAN, ItemCompendium.POCKET_CRAFTING_TABLE, BlockCompendium.REDSTONE_TIMER, BlockCompendium.TRASH_CAN, BlockCompendium.VACUUM_HOPPER, FluidCompendium.LIQUID_XP, BlockCompendium.XP_SHOWER, BlockCompendium.XP_DRAIN,
            ItemCompendium.MAGNET, ItemCompendium.DIAMOND_RING, ItemCompendium.ANGEL_RING, ItemCompendium.MAGMA_RING, ItemCompendium.WATER_RING, ItemCompendium.LIGHT_RING, ItemCompendium.GLIDER_LEFT_WING, ItemCompendium.GLIDER_RIGHT_WING,
            ItemCompendium.WHITE_GLIDER, ItemCompendium.ORANGE_GLIDER, ItemCompendium.MAGENTA_GLIDER, ItemCompendium.LIGHT_BLUE_GLIDER, ItemCompendium.YELLOW_GLIDER, ItemCompendium.LIME_GLIDER, ItemCompendium.PINK_GLIDER, ItemCompendium.GRAY_GLIDER,
            ItemCompendium.LIGHT_GRAY_GLIDER, ItemCompendium.CYAN_GLIDER, ItemCompendium.BLUE_GLIDER, ItemCompendium.PURPLE_GLIDER, ItemCompendium.GREEN_GLIDER, ItemCompendium.BROWN_GLIDER, ItemCompendium.RED_GLIDER, ItemCompendium.BLACK_GLIDER,
            ItemCompendium.WHITE_RUNE, ItemCompendium.ORANGE_RUNE, ItemCompendium.MAGENTA_RUNE, ItemCompendium.LIGHT_BLUE_RUNE, ItemCompendium.YELLOW_RUNE, ItemCompendium.LIME_RUNE, ItemCompendium.PINK_RUNE, ItemCompendium.GRAY_RUNE,
            ItemCompendium.LIGHT_GRAY_RUNE, ItemCompendium.CYAN_RUNE, ItemCompendium.BLUE_RUNE, ItemCompendium.PURPLE_RUNE, ItemCompendium.GREEN_RUNE, ItemCompendium.BROWN_RUNE, ItemCompendium.RED_RUNE, ItemCompendium.BLACK_RUNE,
            ItemCompendium.VOID_BUCKET, ItemCompendium.WOODEN_BUCKET, ItemCompendium.WATER_WOODEN_BUCKET, ItemCompendium.SLIME_BOOTS, ItemCompendium.SLIME_SLING, ItemCompendium.TORCH_SLING, ItemCompendium.ESCAPE_ROPE, ItemCompendium.COOLER,
            BlockCompendium.BIG_TORCH, BlockCompendium.HEATER, BlockCompendium.DEHUMIDIFIER, BlockCompendium.CHUNK_LOADER, BlockCompendium.STONE_SPIKES, BlockCompendium.IRON_SPIKES, BlockCompendium.GOLD_SPIKES, BlockCompendium.DIAMOND_SPIKES,
            ItemCompendium.MEASURING_TAPE, BlockCompendium.COBBLESTONE_GENERATOR_MK1, BlockCompendium.COBBLESTONE_GENERATOR_MK2, BlockCompendium.COBBLESTONE_GENERATOR_MK3, BlockCompendium.COBBLESTONE_GENERATOR_MK4, BlockCompendium.COBBLESTONE_GENERATOR_MK5,
            BlockCompendium.BASALT_GENERATOR_MK1, BlockCompendium.BASALT_GENERATOR_MK2, BlockCompendium.BASALT_GENERATOR_MK3, BlockCompendium.BASALT_GENERATOR_MK4, BlockCompendium.BASALT_GENERATOR_MK5,
            ItemCompendium.WHITE_SLEEPING_BAG, ItemCompendium.ORANGE_SLEEPING_BAG, ItemCompendium.MAGENTA_SLEEPING_BAG, ItemCompendium.LIGHT_BLUE_SLEEPING_BAG, ItemCompendium.YELLOW_SLEEPING_BAG, ItemCompendium.LIME_SLEEPING_BAG, ItemCompendium.PINK_SLEEPING_BAG, ItemCompendium.GRAY_SLEEPING_BAG,
            ItemCompendium.LIGHT_GRAY_SLEEPING_BAG, ItemCompendium.CYAN_SLEEPING_BAG, ItemCompendium.BLUE_SLEEPING_BAG, ItemCompendium.PURPLE_SLEEPING_BAG, ItemCompendium.GREEN_SLEEPING_BAG, ItemCompendium.BROWN_SLEEPING_BAG, ItemCompendium.RED_SLEEPING_BAG, ItemCompendium.BLACK_SLEEPING_BAG,
            BlockCompendium.REGULAR_CONVEYOR_BELT, BlockCompendium.FAST_CONVEYOR_BELT, BlockCompendium.EXPRESS_CONVEYOR_BELT,
            BlockCompendium.WHITE_ELEVATOR, BlockCompendium.ORANGE_ELEVATOR, BlockCompendium.MAGENTA_ELEVATOR, BlockCompendium.LIGHT_BLUE_ELEVATOR, BlockCompendium.YELLOW_ELEVATOR, BlockCompendium.LIME_ELEVATOR, BlockCompendium.PINK_ELEVATOR, BlockCompendium.GRAY_ELEVATOR,
            BlockCompendium.LIGHT_GRAY_ELEVATOR, BlockCompendium.CYAN_ELEVATOR, BlockCompendium.BLUE_ELEVATOR, BlockCompendium.PURPLE_ELEVATOR, BlockCompendium.GREEN_ELEVATOR, BlockCompendium.BROWN_ELEVATOR, BlockCompendium.RED_ELEVATOR, BlockCompendium.BLACK_ELEVATOR
        )
        order.forEach { element ->
            list.add(
                when (element) {
                    is Item -> ItemStack(element)
                    is Block -> ItemStack(element.asItem())
                    is ModdedFluid -> ItemStack(element.fluidBucket)
                    else -> ItemStack.EMPTY
                }
            )
        }
        Registries.FLUID.indexedEntries.forEach { fluidEntry ->
            val fluid = fluidEntry.value()
            val fluidKey = fluidEntry.key.get()
            val itemStack = ItemStack(BlockCompendium.TANK)
            if (fluid == Fluids.EMPTY) {
                list.add(itemStack)
            } else if (fluid.isStill(fluid.defaultState)) {
                val tag = itemStack.orCreateNbt
                val blockEntityTag = NbtCompound()
                blockEntityTag.put("variant", NbtCompound().also { it.putString("fluid", fluidKey.value.toString()) })
                blockEntityTag.putLong("amount", 16 * FluidConstants.BUCKET)
                tag.put("BlockEntityTag", blockEntityTag)
                list.add(itemStack)
            }
        }
        return list
    }

}


