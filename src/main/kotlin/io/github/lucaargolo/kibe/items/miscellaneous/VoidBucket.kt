package io.github.lucaargolo.kibe.items.miscellaneous

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import io.github.lucaargolo.kibe.items.VOID_BUCKET
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.FluidDrainable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.tag.FluidTags
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RayTraceContext
import net.minecraft.world.World

class VoidBucket(settings: Settings): Item(settings) {

    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)
        val hitResult: HitResult = rayTrace(world, user, RayTraceContext.FluidHandling.SOURCE_ONLY)

        return (hitResult as? BlockHitResult)?.let { blockHitResult ->
            val dir = blockHitResult.side
            val pos = blockHitResult.blockPos
            val offsetPos = pos.offset(dir)

            val extractable = FluidAttributes.EXTRACTABLE.get(world, pos)
            val attemptExtraction = extractable.attemptExtraction({true}, FluidAmount.BUCKET, Simulation.SIMULATE)
            if(attemptExtraction.amount() == FluidAmount.BUCKET) {
                val extraction = extractable.attemptExtraction({true}, FluidAmount.BUCKET, Simulation.ACTION)
                user.playSound(if (extraction.rawFluid?.isIn(FluidTags.LAVA) == true) SoundEvents.ITEM_BUCKET_FILL_LAVA else SoundEvents.ITEM_BUCKET_FILL, 1.0f, 1.0f)
                return TypedActionResult.success(itemStack)
            }

            if (world.canPlayerModifyAt(user, pos) && user.canPlaceOn(offsetPos, dir, itemStack)) {
                val blockState = world.getBlockState(pos)
                if (blockState.block is FluidDrainable) {
                    val fluid = (blockState.block as FluidDrainable).tryDrainFluid(world, pos, blockState)
                    if(fluid != Fluids.EMPTY) {
                        user.incrementStat(Stats.USED.getOrCreateStat(this))
                        user.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0f, 1.0f)
                        if (!world.isClient) {
                            Criteria.FILLED_BUCKET.trigger(user as ServerPlayerEntity, ItemStack(VOID_BUCKET))
                        }
                        return TypedActionResult.success(itemStack)
                    }
                }
            }

            TypedActionResult.fail(itemStack)

        } ?: TypedActionResult.pass(itemStack)
    }

}