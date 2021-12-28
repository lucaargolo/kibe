@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.readTank
import io.github.lucaargolo.kibe.utils.writeTank
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.BlockState
import net.minecraft.block.FluidDrainable
import net.minecraft.block.HopperBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import kotlin.math.min

class FluidHopperBlockEntity(block: FluidHopper, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(block), pos, state) {

    companion object {
        private const val CAPACITY = FluidConstants.BUCKET
        private const val TRANSFER_RATE = CAPACITY / 20

        fun serverTick(world: World, pos: BlockPos, state: BlockState, blockEntity: BlockEntity) {
            (blockEntity as FluidHopperBlockEntity).tick(world as ServerWorld, pos, state)
        }
    }

    private var extractableFinder: BlockApiCache<Storage<FluidVariant>, Direction>? = null
    private var insertableFinder: BlockApiCache<Storage<FluidVariant>, Direction>? = null
    private var cachedDirection: Direction? = null

    private var extractionBump = CAPACITY
    private var insertionBump = CAPACITY

    val tank = object : SingleVariantStorage<FluidVariant>() {
        override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
        override fun getCapacity(variant: FluidVariant?): Long = CAPACITY
    }

    override fun writeNbt(tag: NbtCompound) {
        super.writeNbt(tag)
        writeTank(tag, tank)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        readTank(tag, tank)
    }

    private fun tick(world: ServerWorld, pos: BlockPos, state: BlockState) {
        if (!state[HopperBlock.ENABLED]) return
        pull(world, pos.up())
        push(world, pos, state)
    }

    private fun pull(world: ServerWorld, pos: BlockPos) {
        if (tank.amount == tank.capacity) return

        val moved = findExtractable(world, pos)?.let { StorageUtil.move(it, tank, { true }, extractionBump, null) }
            ?: tryDrain(world, pos, extractionBump)

        extractionBump = min(extractionBump - moved + TRANSFER_RATE, CAPACITY)
    }

    private fun findExtractable(world: ServerWorld, pos: BlockPos): Storage<FluidVariant>? {
        if (extractableFinder == null) {
            extractableFinder = BlockApiCache.create(FluidStorage.SIDED, world, pos)
        }
        return extractableFinder?.find(Direction.DOWN)
    }

    private fun tryDrain(world: ServerWorld, pos: BlockPos, amount: Long): Long {
        if (amount != FluidConstants.BUCKET) return 0
        val state = world.getBlockState(pos)
        if (!state.fluidState.isStill) return 0
        val block = state.block as? FluidDrainable ?: return 0
        Transaction.openOuter().use { transaction ->
            val inserted = tank.insert(FluidVariant.of(state.fluidState.fluid), amount, transaction)
            if (inserted != amount || block.tryDrainFluid(world, pos, state).isEmpty) {
                transaction.abort()
                return 0
            }
            transaction.commit()
            return inserted
        }
    }

    private fun push(world: ServerWorld, pos: BlockPos, state: BlockState) {
        if (tank.isResourceBlank) return

        findInsertable(world, pos, state[HopperBlock.FACING])?.let { insertable ->
            val moved = StorageUtil.move(
                tank,
                insertable,
                { true },
                insertionBump,
                null
            )
            insertionBump = min(insertionBump - moved + TRANSFER_RATE, CAPACITY)
        }
    }

    private fun findInsertable(world: ServerWorld, pos: BlockPos, direction: Direction): Storage<FluidVariant>? {
        if (insertableFinder == null || direction != cachedDirection) {
            insertableFinder = BlockApiCache.create(FluidStorage.SIDED, world, pos.offset(direction))
            cachedDirection = direction
        }
        return insertableFinder?.find(direction.opposite)
    }
}