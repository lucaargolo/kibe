package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.storage.base.*
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.item.Items

class ExperiencePotionEmptyStorage(private val context: ContainerItemContext): InsertionOnlyStorage<FluidVariant> {

    override fun insert(resource: FluidVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount)

        if (!context.itemVariant.isOf(Items.GLASS_BOTTLE)) return 0

        if (resource != null && resource.isOf(LIQUID_XP)) {
            if (maxAmount >= FluidConstants.BOTTLE) {
                val newVariant = ItemVariant.of(Items.EXPERIENCE_BOTTLE, context.itemVariant.nbt)
                if (context.exchange(newVariant, 1, transaction) == 1L) {
                    return FluidConstants.BOTTLE
                }
            }
        }

        return 0
    }

    override fun iterator(transaction: TransactionContext?): MutableIterator<StorageView<FluidVariant>> {
        return SingleViewIterator.create(BlankVariantView(FluidVariant.blank(), FluidConstants.BOTTLE), transaction)
    }

}