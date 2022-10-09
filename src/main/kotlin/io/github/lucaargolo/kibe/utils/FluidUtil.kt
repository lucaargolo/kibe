@file:Suppress("UnstableApiUsage")

package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.LOGGER
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.registry.Registry
import org.apache.commons.lang3.math.Fraction

fun readTank(tag: NbtCompound, tank: SingleVariantStorage<FluidVariant>) {
    //Read legacy lba values
    ((tag.getCompound("fluidInv").get("tanks") as? NbtList) ?: (tag.get("tanks") as? NbtList))?.let {
        if(it.size > 0) {
            val addendum = if(tag.contains("x") && tag.contains("y") && tag.contains("z")) {
                "(at x: ${tag.getInt("x")}, y: ${tag.getInt("y")}, z: ${tag.getInt("z")})"
            }else{
                "(unknown coordinates, entangled tank?)"
            }
            LOGGER.info("Found old lba tank $addendum. Converting it")
            val oldTank = it.getCompound(0)
            val amount = oldTank.getCompound("AmountF")
            val fluidId = oldTank.getString("ObjName")
            val fluid = Registry.FLUID.get(Identifier(fluidId))
            if(fluid != Fluids.EMPTY) {
                val fraction = Fraction.getFraction(amount.getLong("w").toInt(), amount.getLong("n").toInt(), amount.getLong("d").toInt())
                val newAmount = MathHelper.floor(fraction.toFloat()*81000f)
                if(newAmount <= tank.capacity) {
                    LOGGER.info("Successfully converted tank with $newAmount droplets of $fluidId")
                    tank.variant = FluidVariant.of(fluid)
                    tank.amount = newAmount.toLong()
                    tag.remove("fluidInv")
                    tag.remove("tanks")
                    return
                }else{
                    LOGGER.info("Tank had $newAmount droplets while maximum accepted was ${tank.capacity}")
                }
            }else{
                LOGGER.info("Tank was empty.")
            }
        }
    }
    tank.variant = FluidVariant.fromNbt(tag.getCompound("variant"))
    tank.amount = tag.getLong("amount")
}

fun writeTank(tag: NbtCompound, tank: SingleVariantStorage<FluidVariant>): NbtCompound {
    tag.put("variant", tank.variant.toNbt())
    tag.putLong("amount", tank.amount)
    return tag
}

fun interactPlayerHand(tank: Storage<FluidVariant>, player: PlayerEntity, hand: Hand): ActionResult {
    return if (FluidStorageUtil.interactWithFluidStorage(tank, player, hand)) {
        ActionResult.success(player.world.isClient)
    } else {
        ActionResult.PASS
    }
}

fun getMb(amount: Long): String {
    return if (amount == 0L) {
        "0"
    } else if (amount < 81) {
        "< 1"
    } else {
        "" + amount / 81
    }
}