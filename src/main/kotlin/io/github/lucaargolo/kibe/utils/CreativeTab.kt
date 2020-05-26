package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.*
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import io.github.lucaargolo.kibe.items.*
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

var CREATIVE_TAB: ItemGroup? = null

fun initCreativeTab() {
    val order = arrayOf(
        CURSED_DIRT, BLESSED_DIRT, CURSED_DROPLETS, BLESSED_DROPLETS, CURSED_BOTTLE, BLESSED_BOTTLE, CURSED_STAR, BLESSED_STAR, CURSED_SEEDS, BLESSED_SEEDS,
        CURSED_LASSO, BLESSED_LASSO, GOLDEN_LASSO, ENTANGLED_CHEST, ENTANGLED_BAG, POCKET_TRASH_CAN, POCKET_CRAFTING_TABLE, REDSTONE_TIMER, TRASH_CAN, VACUUM_HOPPER, LIQUID_XP,
        MAGNET, EMERALD_RING, DIAMOND_RING, ANGEL_RING, MAGMA_RING, WATER_RING, LIGHT_RING, ENDER_RING, DRAGON_RING, POTION_RING,
        BLANK_RUNE, WHITE_RUNE, ORANGE_RUNE, MAGENTA_RUNE, LIGHT_BLUE_RUNE, YELLOW_RUNE, LIME_RUNE, PINK_RUNE, GRAY_RUNE,
        LIGHT_GRAY_RUNE, CYAN_RUNE, BLUE_RUNE, PURPLE_RUNE, GREEN_RUNE, BROWN_RUNE, RED_RUNE, BLACK_RUNE,
        SLIME_BOOTS, SLIME_SLING, SLEEPING_BAG, IRON_SPIKES, DIAMOND_SPIKES, REGULAR_CONVEYOR_BELT, FAST_CONVEYOR_BELT, EXPRESS_CONVEYOR_BELT,
        WHITE_ELEVATOR, ORANGE_ELEVATOR, MAGENTA_ELEVATOR, LIGHT_BLUE_ELEVATOR, YELLOW_ELEVATOR, LIME_ELEVATOR, PINK_ELEVATOR, GRAY_ELEVATOR,
        LIGHT_GRAY_ELEVATOR, CYAN_ELEVATOR, BLUE_ELEVATOR, PURPLE_ELEVATOR, GREEN_ELEVATOR, BROWN_ELEVATOR, RED_ELEVATOR, BLACK_ELEVATOR
    )
    CREATIVE_TAB = FabricItemGroupBuilder
        .create(Identifier(MOD_ID, "creative_tab"))
        .icon { ItemStack(Items.BEDROCK) }
        .appendItems{stacks -> order.forEach {
            val itemStack = when(it) {
                is Item -> ItemStack(it)
                is Block -> ItemStack(Registry.ITEM.get(getBlockId(it)))
                is Identifier -> ItemStack(Registry.ITEM.get(Identifier(it.namespace, "${it.path}_bucket")))
                else -> ItemStack.EMPTY
            }
            stacks.add(itemStack)
        }}.build()
}

