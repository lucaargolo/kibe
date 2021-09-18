@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.kibe.items.tank

import io.github.lucaargolo.kibe.blocks.TANK
import io.github.lucaargolo.kibe.utils.getMb
import io.github.lucaargolo.kibe.utils.readTank
import io.github.lucaargolo.kibe.utils.writeTank
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class TankBlockItem(settings: Settings): BlockItem(TANK, settings) {

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if(!stack.hasNbt()) stack.orCreateNbt.put("BlockEntityTag", NbtCompound())
        super.inventoryTick(stack, world, entity, slot, selected)
    }

    override fun onCraft(stack: ItemStack, world: World, player: PlayerEntity) {
        stack.orCreateNbt.put("BlockEntityTag", NbtCompound())
        super.onCraft(stack, world, player)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val stackTag = stack.nbt ?: return
        val blockEntityTag = stackTag.getCompound("BlockEntityTag")
        val dummyFluidTank = object: SingleVariantStorage<FluidVariant>() {
            override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
            override fun getCapacity(variant: FluidVariant?): Long = FluidConstants.BUCKET * 16
        }
        readTank(blockEntityTag, dummyFluidTank)
        if(!dummyFluidTank.isResourceBlank)
            tooltip.add(FluidVariantRendering.getName(dummyFluidTank.variant).shallowCopy().append(LiteralText(": ${Formatting.GRAY}${getMb(dummyFluidTank.amount)}mB")))
    }

    companion object {

        fun getFluidStorage(stack: ItemStack, context: ContainerItemContext): Storage<FluidVariant> {
            val tank = object: SingleVariantStorage<FluidVariant>() {
                override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
                override fun getCapacity(variant: FluidVariant?): Long = FluidConstants.BUCKET * 16

                override fun insert(insertedVariant: FluidVariant?, maxAmount: Long, transaction: TransactionContext): Long {
                    return super.insert(insertedVariant, maxAmount, transaction).also { updateItem(transaction) }
                }

                override fun extract(extractedVariant: FluidVariant?, maxAmount: Long, transaction: TransactionContext): Long {
                    return super.extract(extractedVariant, maxAmount, transaction).also { updateItem(transaction) }
                }

                private fun updateItem(transaction: TransactionContext) {
                    stack.orCreateNbt.put("BlockEntityTag", writeTank(NbtCompound(), this))
                    Transaction.openNested(transaction).also {
                        context.exchange(ItemVariant.of(stack), Long.MAX_VALUE, it)
                    }.commit()
                }
            }
            if(stack.hasNbt() && stack.orCreateNbt.contains("BlockEntityTag")) {
                readTank(stack.orCreateNbt.getCompound("BlockEntityTag"), tank)
            }
            return tank
        }

    }

}