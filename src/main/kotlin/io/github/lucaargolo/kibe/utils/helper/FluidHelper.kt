@file:Suppress("UnstableApiUsage")

package io.github.lucaargolo.kibe.utils.helper

import io.github.lucaargolo.kibe.utils.XpUtils
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand

object FluidHelper {

    fun readTank(tag: NbtCompound, tank: SingleVariantStorage<FluidVariant>) {
        val optional = FluidVariant.CODEC.decode(NbtOps.INSTANCE, tag.getCompound("variant")).result()
        if(optional.isPresent) {
            tank.variant = optional.get().first
        }else{
            tank.variant = FluidVariant.blank()
        }
        tank.amount = tag.getLong("amount")
    }

    fun writeTank(tag: NbtCompound, tank: SingleVariantStorage<FluidVariant>): NbtCompound {
        val optional = FluidVariant.CODEC.encode(tank.variant, NbtOps.INSTANCE, NbtCompound()).result()
        if(optional.isPresent) {
            tag.put("variant", optional.get())
        }
        tag.putLong("amount", tank.amount)
        return tag
    }

    fun interactPlayerHand(tank: Storage<FluidVariant>, player: PlayerEntity, hand: Hand): ActionResult {
        return if (FluidStorageUtil.interactWithFluidStorage(tank, player, hand)) {
            ActionResult.success(player.world.isClient)
        } else if (!player.world.isClient && XpUtils.canPlayerDrinkXp(tank, player, hand)) {
            XpUtils.donateXpAction(player, tank as SingleVariantStorage)
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

}