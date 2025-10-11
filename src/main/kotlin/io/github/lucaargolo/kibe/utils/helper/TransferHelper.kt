package io.github.lucaargolo.kibe.utils.helper

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.block.EntangledTank
import io.github.lucaargolo.kibe.blockentity.*
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
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
import net.minecraft.client.MinecraftClient
import net.minecraft.fluid.Fluids
import net.minecraft.item.ExperienceBottleItem
import net.minecraft.item.Items
import net.minecraft.server.MinecraftServer
import net.minecraft.util.DyeColor
import net.neoforged.neoforge.server.ServerLifecycleHooks
import thedarkcolour.kotlinforforge.neoforge.forge.runForDist


object TransferHelper {

    fun initialize() {
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.BIG_TORCH)
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.BLOCK_GENERATOR)
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.BREAKER)
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.COOLER)
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.DRAWBRIDGE)
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.ENTANGLED_CHEST)
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.PLACER)
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.TRASH_CAN)
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.VACUUM_HOPPER)
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of, BlockEntityCompendium.WITHER_BUILDER)

        FluidStorage.SIDED.registerForBlockEntity(EntangledTankEntity.Companion::getFluidStorage, BlockEntityCompendium.ENTANGLED_TANK)
        FluidStorage.SIDED.registerForBlockEntity(FluidHopperBlockEntity.Companion::getFluidStorage, BlockEntityCompendium.FLUID_HOPPER)
        FluidStorage.SIDED.registerForBlockEntity(VacuumHopperEntity.Companion::getFluidStorage, BlockEntityCompendium.VACUUM_HOPPER)
        FluidStorage.SIDED.registerForBlockEntity(TankBlockEntity.Companion::getFluidStorage, BlockEntityCompendium.TANK)
        FluidStorage.combinedItemApiProvider(ItemCompendium.WOODEN_BUCKET).register {
            EmptyItemFluidStorage(it, ItemCompendium.WOODEN_WATER_BUCKET, Fluids.WATER, FluidConstants.BUCKET)
        }
        FluidStorage.GENERAL_COMBINED_PROVIDER.register { context ->
            (context.itemVariant.item as? WoodenBucket)?.let { bucketItem ->
                val bucketFluid = Fluids.WATER
                if (bucketItem == ItemCompendium.WOODEN_WATER_BUCKET) {
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
            val key = stack.get(ComponentTypeCompendium.ENTANGLED_KEY) ?: EntangledTank.DEFAULT_KEY
            val colorCode = (stack.get(ComponentTypeCompendium.RUNE_SET) ?: KibeMod.DEFAULT_RUNE_SET).map(DyeColor::getId).joinToString(separator = "", transform = Integer::toHexString)

            (runForDist(clientTarget = { MinecraftClient.getInstance() }, serverTarget = { ServerLifecycleHooks.getCurrentServer() })).let {
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