package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.MOD_CONFIG
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import io.github.lucaargolo.kibe.fluids.miscellaneous.LiquidXpFluid
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import kotlin.math.min


object XpUtils {

    const val MB_PER_XP = FluidConstants.BUCKET / 100

    private fun isTankValidXpDonor(tank: Storage<FluidVariant>) =
        tank is SingleVariantStorage<FluidVariant> && tank.variant.fluid is LiquidXpFluid


    fun expToReachNextLvl(nextLevelExp: Int, progress: Float): Int {
        return nextLevelExp - (nextLevelExp * progress).toInt()
    }

    fun convertXpToMilibuckets(xp: Long) = MB_PER_XP * xp

    fun convertMilibucketsToXp(milibuckets: Long) = milibuckets / MB_PER_XP

    fun donateXpAction(player: PlayerEntity, tank: SingleVariantStorage<FluidVariant>): ActionResult {
        if(tank.amount < MB_PER_XP) return ActionResult.FAIL

        val xpToNextLevel = expToReachNextLvl(player.nextLevelExperience, player.experienceProgress)

        val liquidXpToExtract = min(
            convertXpToMilibuckets(xpToNextLevel.toLong()),
            tank.amount
        )

        Transaction.openOuter().also { transaction ->
            var extractedAmount = -1L
            tank.extract(FluidVariant.of(LIQUID_XP), liquidXpToExtract, transaction).let { extractedAmount = it }
            transaction.addCloseCallback { _, result ->
                if(result.wasAborted()) {
                    return@addCloseCallback
                }
                player.addExperience(convertMilibucketsToXp(liquidXpToExtract).toInt())
                if(extractedAmount > 0) {
                    player.world.playSound(
                        null,
                        player.blockPos,
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        SoundCategory.AMBIENT,
                        .5f,
                        player.world.random.nextBetween(3,9).toFloat() / 10)
                }
            }
           transaction.commit()
        }

        return ActionResult.success(true)
    }

    fun canPlayerDrinkXp(tank: Storage<FluidVariant>, player: PlayerEntity, hand: Hand): Boolean {
        return MOD_CONFIG.miscellaneousModule.xpTankDrinkOnRightClick &&
                player.mainHandStack.isEmpty &&
                hand == Hand.MAIN_HAND &&
                isTankValidXpDonor(tank)

    }

}