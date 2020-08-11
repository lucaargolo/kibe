package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.items.WOODEN_BUCKET
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.BucketItem
import net.minecraft.item.ItemStack

open class WoodenBucket(fluid: Fluid, settings: Settings): BucketItem(fluid, settings) {

    override fun getEmptiedStack(stack: ItemStack, player: PlayerEntity): ItemStack {
        return if (!player.abilities.creativeMode) ItemStack(WOODEN_BUCKET) else stack
    }

    class Empty(settings: Settings): WoodenBucket(Fluids.EMPTY, settings)
    class Water(settings: Settings): WoodenBucket(Fluids.WATER, settings)

}