package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.items.WATER_WOODEN_BUCKET
import io.github.lucaargolo.kibe.items.WOODEN_BUCKET
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.FluidDrainable
import net.minecraft.block.FluidFillable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RaycastContext
import net.minecraft.world.World

open class WoodenBucket(val fluid: Fluid, settings: Settings): Item(settings) {

    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack>? {
        val itemStack = user.getStackInHand(hand)
        val hitResult: HitResult = raycast(world, user,
            if (this.fluid === Fluids.EMPTY) RaycastContext.FluidHandling.SOURCE_ONLY else RaycastContext.FluidHandling.NONE
        )

        return (hitResult as? BlockHitResult)?.let { blockHitResult ->
            val dir = blockHitResult.side
            val pos = blockHitResult.blockPos
            val offsetPos = pos.offset(dir)

            if (world.canPlayerModifyAt(user, pos) && user.canPlaceOn(offsetPos, dir, itemStack)) {
                val blockState = world.getBlockState(pos)
                if (this.fluid == Fluids.EMPTY) {
                    if (blockState.block is FluidDrainable) {
                        val fluid = (blockState.block as FluidDrainable).tryDrainFluid(world, pos, blockState)
                        if (fluid == Fluids.WATER) {
                            user.incrementStat(Stats.USED.getOrCreateStat(this))
                            user.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0f, 1.0f)
                            val itemStack2 = ItemUsage.method_30012(itemStack, user, ItemStack(WATER_WOODEN_BUCKET))
                            if (!world.isClient) {
                                Criteria.FILLED_BUCKET.trigger(user as ServerPlayerEntity, ItemStack(WATER_WOODEN_BUCKET))
                            }
                            return TypedActionResult.success(itemStack2, world.isClient())
                        }
                    }
                } else {
                    val interactablePos = if (blockState.block is FluidFillable && this.fluid === Fluids.WATER) pos else offsetPos
                    if (bucketItem.placeFluid(user, world, interactablePos, blockHitResult)) {
                        bucketItem.onEmptied(world, itemStack, interactablePos)
                        if (user is ServerPlayerEntity) {
                            Criteria.PLACED_BLOCK.trigger(user, interactablePos, itemStack)
                        }
                        user.incrementStat(Stats.USED.getOrCreateStat(this))
                        return TypedActionResult.success(this.getEmptiedStack(itemStack, user), world.isClient())
                    }
                }
            }
            TypedActionResult.fail(itemStack)

        } ?: TypedActionResult.pass(itemStack)

    }

    private val bucketItem: BucketItem
        get() = (if(fluid == Fluids.WATER) Items.WATER_BUCKET else Items.BUCKET) as BucketItem

    private fun getEmptiedStack(stack: ItemStack?, player: PlayerEntity): ItemStack? {
        return if (!player.abilities.creativeMode) ItemStack(WOODEN_BUCKET) else stack
    }

    class Empty(settings: Settings): WoodenBucket(Fluids.EMPTY, settings)
    class Water(settings: Settings): WoodenBucket(Fluids.WATER, settings)

}