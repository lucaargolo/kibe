package io.github.lucaargolo.kibe.utils.helper

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.blockentity.*
import io.github.lucaargolo.kibe.fluid.FluidCompendium
import io.github.lucaargolo.kibe.item.EntangledBucket
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.item.TankBlockItem
import io.github.lucaargolo.kibe.item.WoodenBucket
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.fluid.Fluids
import net.minecraft.item.ExperienceBottleItem
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.util.DyeColor

object TransferHelper {

    fun initialize() {
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.ENTANGLED_CHEST)
        FluidStorage.SIDED.registerForBlockEntity(EntangledTankEntity.Companion::getFluidStorage, BlockEntityCompendium.ENTANGLED_TANK)
        FluidStorage.SIDED.registerForBlockEntity(FluidHopperBlockEntity.Companion::getFluidStorage, BlockEntityCompendium.FLUID_HOPPER)
        FluidStorage.SIDED.registerForBlockEntity(VacuumHopperEntity.Companion::getFluidStorage, BlockEntityCompendium.VACUUM_HOPPER)
        FluidStorage.SIDED.registerForBlockEntity(TankBlockEntity.Companion::getFluidStorage, BlockEntityCompendium.TANK)
        FluidStorage.combinedItemApiProvider(ItemCompendium.WOODEN_BUCKET).register {
            EmptyItemFluidStorage(it, ItemCompendium.WATER_WOODEN_BUCKET, Fluids.WATER, FluidConstants.BUCKET)
        }
        FluidStorage.GENERAL_COMBINED_PROVIDER.register { context ->
            (context.itemVariant.item as? WoodenBucket)?.let { bucketItem ->
                val bucketFluid = Fluids.WATER
                if (bucketItem == ItemCompendium.WATER_WOODEN_BUCKET) {
                    return@register FullItemFluidStorage(context, ItemCompendium.WOODEN_BUCKET, FluidVariant.of(bucketFluid), FluidConstants.BUCKET)
                }
            }
            return@register null
        }
        FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register {
            EmptyItemFluidStorage(it, Items.EXPERIENCE_BOTTLE, FluidCompendium.LIQUID_XP, FluidConstants.BOTTLE)
        }
        FluidStorage.GENERAL_COMBINED_PROVIDER.register { context ->
            (context.itemVariant.item as? ExperienceBottleItem)?.let { bottleItem ->
                val bottleFluid = FluidCompendium.LIQUID_XP
                if (bottleItem == Items.EXPERIENCE_BOTTLE) {
                    return@register FullItemFluidStorage(context, Items.GLASS_BOTTLE, FluidVariant.of(bottleFluid), FluidConstants.BOTTLE)
                }
            }
            return@register null
        }
        FluidStorage.ITEM.registerForItems({ stack, context -> TankBlockItem.getFluidStorage(stack, context) }, ItemCompendium.TANK)
        FluidStorage.ITEM.registerForItems({ stack, _ ->
            val tag = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA)?.copyNbt() ?: NbtCompound()
            val key = tag.getString("key")
            var colorCode = ""
            (1..8).forEach {
                val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE
                colorCode += dc.id.let { int -> Integer.toHexString(int) }
            }

            @Suppress("DEPRECATION")
            FabricLoader.getInstance().gameInstance.let {
                if (KibeMod.CLIENT && it is MinecraftClient) {
                    if (it.isOnThread) {
                        EntangledBucket.getFluidInv(null, key, colorCode)
                    } else if (it.isIntegratedServerRunning && it.server?.isOnThread == true) {
                        EntangledBucket.getFluidInv(it.server?.overworld, key, colorCode)
                    } else {
                        null
                    }
                } else if (it is MinecraftServer) {
                    EntangledBucket.getFluidInv(it.overworld, key, colorCode)
                } else {
                    null
                }
            } ?: object : SingleVariantStorage<FluidVariant>() {
                override fun getCapacity(variant: FluidVariant?) = 0L
                override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
            }
        }, ItemCompendium.ENTANGLED_TANK, ItemCompendium.ENTANGLED_BUCKET)
    }


}