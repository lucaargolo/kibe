package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class TankBlockItem(settings: Settings): BlockItem(BlockCompendium.TANK, settings) {

    override fun appendTooltip(stack: ItemStack, context: TooltipContext?, tooltip: MutableList<Text>, type: TooltipType?) {
        super.appendTooltip(stack, context, tooltip, type)
        val blockEntityTag = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA)?.copyNbt() ?: NbtCompound()
        val dummyFluidTank = object: SingleVariantStorage<FluidVariant>() {
            override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
            override fun getCapacity(variant: FluidVariant?): Long = FluidConstants.BUCKET * 16
        }
        FluidHelper.readTank(blockEntityTag, dummyFluidTank)
        if(!dummyFluidTank.isResourceBlank)
            tooltip.add(FluidVariantAttributes.getName(dummyFluidTank.variant).copyContentOnly().append(Text.literal(": ${Formatting.GRAY}${FluidHelper.getMb(dummyFluidTank.amount)}mB")))
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
                    var newStack: ItemStack? = null
                    if(context.amount > 1) {
                        newStack = stack.copy()
                        newStack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(FluidHelper.writeTank(NbtCompound(), this)))
                    }else{
                        stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(FluidHelper.writeTank(NbtCompound(), this)))
                    }
                    Transaction.openNested(transaction).also {
                        context.exchange(ItemVariant.of(stack), Long.MAX_VALUE, it)
                        newStack?.let { newStack ->
                            context.extract(ItemVariant.of(stack), 1, it)
                            context.insert(ItemVariant.of(newStack), 1, it)
                        }
                    }.commit()
                }
            }
            if(stack.contains(DataComponentTypes.BLOCK_ENTITY_DATA)) {
                FluidHelper.readTank(stack.get(DataComponentTypes.BLOCK_ENTITY_DATA)!!.copyNbt(), tank)
            }
            return tank
        }

    }

}