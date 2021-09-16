package io.github.lucaargolo.kibe.utils

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand

fun readTank(tag: NbtCompound, tank: SingleVariantStorage<FluidVariant>) {
    tank.variant = FluidVariant.fromNbt(tag.getCompound("variant"))
    tank.amount = tag.getLong("amount")
}

fun writeTank(tag: NbtCompound, tank: SingleVariantStorage<FluidVariant>): NbtCompound {
    tag.put("variant", tank.variant.toNbt())
    tag.putLong("amount", tank.amount)
    return tag
}

fun interactPlayerHand(tank: Storage<FluidVariant>, player: PlayerEntity, hand: Hand): ActionResult {
    val interacted = interactPlayerHandInner(tank, player, hand)
    if (interacted) {
        return ActionResult.success(player.world.isClient)
    } else {
        return ActionResult.PASS
    }
}

private fun interactPlayerHandInner(tank: Storage<FluidVariant>, player: PlayerEntity, hand: Hand): Boolean {
    val backupStack = if(player !is FakePlayerEntity && player.isCreative) { player.getStackInHand(hand).copy() } else null
    val handStorage = ContainerItemContext.ofPlayerHand(player, hand).find(FluidStorage.ITEM) ?: return false
    // Move from hand to tank
    if (StorageUtil.move(handStorage, tank, { true }, Long.MAX_VALUE, null) > 0) {
        backupStack?.let { player.setStackInHand(hand, backupStack) }
        return true
    }
    // Move from tank to hand
    if (StorageUtil.move(tank, handStorage, { true }, Long.MAX_VALUE, null) > 0) {
        backupStack?.let { player.setStackInHand(hand, backupStack) }
        return true
    }

    return false;
}

fun getMb(amount: Long): String {
    if (amount == 0L) {
        return "0"
    } else if (amount < 81) {
        return "< 1"
    } else {
        return "" + amount / 81;
    }
}