@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.items.WATER_WOODEN_BUCKET
import io.github.lucaargolo.kibe.items.WOODEN_BUCKET
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.fluid.Fluids

class WoodenBucketEmptyStorage(private val context: ContainerItemContext): InsertionOnlyStorage<FluidVariant> {

    override fun insert(resource: FluidVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount)

		if (!context.itemVariant.isOf(WOODEN_BUCKET)) return 0

		val fullBucket = WATER_WOODEN_BUCKET

        if (resource != null && resource.isOf(Fluids.WATER)) {
            if (maxAmount >= FluidConstants.BUCKET) {
                val newVariant = ItemVariant.of(fullBucket, context.itemVariant.nbt)
                if (context.exchange(newVariant, 1, transaction) == 1L) {
                    return FluidConstants.BUCKET
                }
            }
        }

		return 0
    }

    override fun iterator(transaction: TransactionContext?): MutableIterator<StorageView<FluidVariant>> {
        return SingleViewIterator.create(BlankVariantView(FluidVariant.blank(), FluidConstants.BUCKET), transaction)
    }

}