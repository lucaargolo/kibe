package io.github.lucaargolo.kibe.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.TANK
import io.github.lucaargolo.kibe.blocks.COOLER
import io.github.lucaargolo.kibe.blocks.*
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import io.github.lucaargolo.kibe.fluids.getFluidBucket
import io.github.lucaargolo.kibe.items.*
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.Block
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

var CREATIVE_TAB: ItemGroup? = null

fun initCreativeTab() {
    val order = arrayOf(
        CURSED_DIRT, CURSED_DROPLETS, CURSED_SEEDS, CURSED_LASSO, GOLDEN_LASSO, DIAMOND_LASSO,
        ENTANGLED_CHEST, ENTANGLED_BAG, POCKET_TRASH_CAN, POCKET_CRAFTING_TABLE, REDSTONE_TIMER, TRASH_CAN, VACUUM_HOPPER, LIQUID_XP,
        MAGNET, DIAMOND_RING, ANGEL_RING, MAGMA_RING, WATER_RING, LIGHT_RING, GLIDER_LEFT_WING, GLIDER_RIGHT_WING,
        WHITE_GLIDER, ORANGE_GLIDER, MAGENTA_GLIDER, LIGHT_BLUE_GLIDER, YELLOW_GLIDER, LIME_GLIDER, PINK_GLIDER, GRAY_GLIDER,
        LIGHT_GRAY_GLIDER, CYAN_GLIDER, BLUE_GLIDER, PURPLE_GLIDER, GREEN_GLIDER, BROWN_GLIDER, RED_GLIDER, BLACK_GLIDER,
        WHITE_RUNE, ORANGE_RUNE, MAGENTA_RUNE, LIGHT_BLUE_RUNE, YELLOW_RUNE, LIME_RUNE, PINK_RUNE, GRAY_RUNE,
        LIGHT_GRAY_RUNE, CYAN_RUNE, BLUE_RUNE, PURPLE_RUNE, GREEN_RUNE, BROWN_RUNE, RED_RUNE, BLACK_RUNE, VOID_BUCKET, WOODEN_BUCKET, WATER_WOODEN_BUCKET,
        SLIME_BOOTS, SLIME_SLING, SLEEPING_BAG, COOLER, BIG_TORCH, CHUNK_LOADER, IRON_SPIKES, DIAMOND_SPIKES, REGULAR_CONVEYOR_BELT, FAST_CONVEYOR_BELT, EXPRESS_CONVEYOR_BELT,
        WHITE_ELEVATOR, ORANGE_ELEVATOR, MAGENTA_ELEVATOR, LIGHT_BLUE_ELEVATOR, YELLOW_ELEVATOR, LIME_ELEVATOR, PINK_ELEVATOR, GRAY_ELEVATOR,
        LIGHT_GRAY_ELEVATOR, CYAN_ELEVATOR, BLUE_ELEVATOR, PURPLE_ELEVATOR, GREEN_ELEVATOR, BROWN_ELEVATOR, RED_ELEVATOR, BLACK_ELEVATOR,
        KIBE, CURSED_KIBE, GOLDEN_KIBE, DIAMOND_KIBE
    )
    CREATIVE_TAB = FabricItemGroupBuilder
        .create(Identifier(MOD_ID, "creative_tab"))
        .icon { ItemStack(KIBE) }
        .appendItems{stacks ->
            Registry.FLUID.forEach { fluid ->
                val itemStack = ItemStack(TANK)
                if(fluid == Fluids.EMPTY) {
                    stacks.add(itemStack)
                }else if(fluid.isStill(fluid.defaultState)) {
                    val tag = itemStack.orCreateTag
                    val blockEntityTag = CompoundTag()
                    val tanksTag = CompoundTag()
                    val tankTag = CompoundTag()
                    tankTag.put("fluids", FluidKeys.get(fluid).withAmount(FluidAmount(16)).toTag())
                    tanksTag.put("0", tankTag)
                    blockEntityTag.put("tanks", tanksTag)
                    tag.put("BlockEntityTag", blockEntityTag)
                    stacks.add(itemStack)
                }
            }
            order.forEach {
                val itemStack = when(it) {
                    is Item -> ItemStack(it)
                    is Block -> ItemStack(it.asItem())
                    is Fluid -> ItemStack(getFluidBucket(it))
                    else -> ItemStack.EMPTY
                }
                stacks.add(itemStack)
            }
        }.build()
}

