package io.github.lucaargolo.kibe.utils

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.*
import io.github.lucaargolo.kibe.blocks.COOLER
import io.github.lucaargolo.kibe.blocks.ENTANGLED_CHEST
import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.TANK
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
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

var CREATIVE_TAB: ItemGroup? = null

fun initCreativeTab() {
    CREATIVE_TAB = FabricItemGroupBuilder
        .create(Identifier(MOD_ID, "creative_tab"))
        .icon { ItemStack(KIBE) }
        .appendItems { it.addAll(appendItems()) }
        .build()
}

private fun appendItems(): List<ItemStack> {
    val list = mutableListOf<ItemStack>()
    val order = arrayOf(
        KIBE, CURSED_KIBE, GOLDEN_KIBE, DIAMOND_KIBE,
        CURSED_DIRT, CURSED_DROPLETS, CURSED_SEEDS, CURSED_LASSO, GOLDEN_LASSO, DIAMOND_LASSO,
        ENTANGLED_CHEST, ENTANGLED_BAG, ENTANGLED_TANK, ENTANGLED_BUCKET, FLUID_HOPPER, OBSIDIAN_SAND, WITHER_PROOF_BLOCK, WITHER_PROOF_SAND, WITHER_PROOF_GLASS, WITHER_BUILDER, PLACER, BREAKER, IGNITER,
        POCKET_TRASH_CAN, POCKET_CRAFTING_TABLE, REDSTONE_TIMER, TRASH_CAN, VACUUM_HOPPER, LIQUID_XP, XP_SHOWER, XP_DRAIN,
        MAGNET, DIAMOND_RING, ANGEL_RING, MAGMA_RING, WATER_RING, LIGHT_RING, GLIDER_LEFT_WING, GLIDER_RIGHT_WING,
        WHITE_GLIDER, ORANGE_GLIDER, MAGENTA_GLIDER, LIGHT_BLUE_GLIDER, YELLOW_GLIDER, LIME_GLIDER, PINK_GLIDER, GRAY_GLIDER,
        LIGHT_GRAY_GLIDER, CYAN_GLIDER, BLUE_GLIDER, PURPLE_GLIDER, GREEN_GLIDER, BROWN_GLIDER, RED_GLIDER, BLACK_GLIDER,
        WHITE_RUNE, ORANGE_RUNE, MAGENTA_RUNE, LIGHT_BLUE_RUNE, YELLOW_RUNE, LIME_RUNE, PINK_RUNE, GRAY_RUNE,
        LIGHT_GRAY_RUNE, CYAN_RUNE, BLUE_RUNE, PURPLE_RUNE, GREEN_RUNE, BROWN_RUNE, RED_RUNE, BLACK_RUNE,
        VOID_BUCKET, WOODEN_BUCKET, WATER_WOODEN_BUCKET, SLIME_BOOTS, SLIME_SLING, TORCH_SLING, ESCAPE_ROPE, COOLER, BIG_TORCH, HEATER, DEHUMIDIFIER, CHUNK_LOADER, STONE_SPIKES, IRON_SPIKES, GOLD_SPIKES, DIAMOND_SPIKES,
        COBBLESTONE_GENERATOR_MK1, COBBLESTONE_GENERATOR_MK2, COBBLESTONE_GENERATOR_MK3, COBBLESTONE_GENERATOR_MK4, COBBLESTONE_GENERATOR_MK5,
        BASALT_GENERATOR_MK1, BASALT_GENERATOR_MK2, BASALT_GENERATOR_MK3, BASALT_GENERATOR_MK4, BASALT_GENERATOR_MK5,
        WHITE_SLEEPING_BAG, ORANGE_SLEEPING_BAG, MAGENTA_SLEEPING_BAG, LIGHT_BLUE_SLEEPING_BAG, YELLOW_SLEEPING_BAG, LIME_SLEEPING_BAG, PINK_SLEEPING_BAG, GRAY_SLEEPING_BAG,
        LIGHT_GRAY_SLEEPING_BAG, CYAN_SLEEPING_BAG, BLUE_SLEEPING_BAG, PURPLE_SLEEPING_BAG, GREEN_SLEEPING_BAG, BROWN_SLEEPING_BAG, RED_SLEEPING_BAG, BLACK_SLEEPING_BAG,
        REGULAR_CONVEYOR_BELT, FAST_CONVEYOR_BELT, EXPRESS_CONVEYOR_BELT,
        WHITE_ELEVATOR, ORANGE_ELEVATOR, MAGENTA_ELEVATOR, LIGHT_BLUE_ELEVATOR, YELLOW_ELEVATOR, LIME_ELEVATOR, PINK_ELEVATOR, GRAY_ELEVATOR,
        LIGHT_GRAY_ELEVATOR, CYAN_ELEVATOR, BLUE_ELEVATOR, PURPLE_ELEVATOR, GREEN_ELEVATOR, BROWN_ELEVATOR, RED_ELEVATOR, BLACK_ELEVATOR
    )
    order.forEach { element ->
        list.add(when(element) {
            is Item -> ItemStack(element)
            is Block -> ItemStack(element.asItem())
            is Fluid -> ItemStack(getFluidBucket(element))
            else -> ItemStack.EMPTY
        })
    }
    Registry.FLUID.forEach { fluid ->
        val itemStack = ItemStack(TANK)
        if(fluid == Fluids.EMPTY) {
            list.add(itemStack)
        }else if(fluid.isStill(fluid.defaultState)) {
            val key = FluidKeys.get(fluid)
            if(!key.entry.isEmpty) {
                val tag = itemStack.orCreateNbt
                val blockEntityTag = NbtCompound()
                val fluidInv = SimpleFixedFluidInv(1, FluidAmount.ofWhole(16))
                fluidInv.setInvFluid(0, key.withAmount(FluidAmount.ofWhole(16)), Simulation.ACTION)
                blockEntityTag.put("fluidInv", fluidInv.toTag())
                tag.put("BlockEntityTag", blockEntityTag)
                list.add(itemStack)
            }
        }
    }
    return list
}


