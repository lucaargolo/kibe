package io.github.lucaargolo.kibe.blockentity

import io.github.lucaargolo.kibe.utils.SyncableBlockEntity
import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World


class TankBlockEntity(pos: BlockPos, state: BlockState): SyncableBlockEntity(BlockEntityCompendium.TANK, pos, state) {

    var lastRenderedFluid = 0f
    var tickDelay = 0
    val tank = object: SingleVariantStorage<FluidVariant>() {
        override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
        override fun getCapacity(variant: FluidVariant?): Long = FluidConstants.BUCKET * 16

        override fun onFinalCommit() {
            markDirtyAndSync()
        }
    }

    val isEmpty: Boolean
        get() = tank.amount == 0L

    fun markDirtyAndSync() {
        markDirty()
        if(world?.isClient == false)
            sync()
    }

    override fun writeNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        FluidHelper.writeTank(tag, tank)
    }

    override fun readNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.readNbt(tag, registryLookup)
        FluidHelper.readTank(tag, tank)
    }

    override fun writeClientNbt(tag: NbtCompound, registryLookup: WrapperLookup) = tag.also { writeNbt(it, registryLookup) }

    override fun readClientNbt(tag: NbtCompound, registryLookup: WrapperLookup) = readNbt(tag, registryLookup)

    companion object {
        fun getFluidStorage(be: TankBlockEntity, dir: Direction?): Storage<FluidVariant> {
            return be.tank
        }

        fun tick(world: World, pos: BlockPos, state: BlockState, entity: TankBlockEntity) {
            val fluid = if(entity.isEmpty) Fluids.EMPTY else entity.tank.variant.fluid ?: Fluids.EMPTY
            val luminance = fluid.defaultState.blockState.luminance
            if(luminance != state[Properties.LEVEL_15]) {
                world.setBlockState(pos, state.with(Properties.LEVEL_15, luminance))
            }
            if(entity.tickDelay++ < 10) return else entity.tickDelay = 0
            Direction.values().forEach {
                if(it == Direction.UP) return@forEach
                val otherTank = (world.getBlockEntity(pos.add(it.vector)) as? TankBlockEntity) ?: return@forEach

                if(it == Direction.DOWN) {
                    // Move down as much fluid as we can
                    StorageUtil.move(entity.tank, otherTank.tank, { true }, Long.MAX_VALUE, null)
                }

                if(entity.tank.amount > otherTank.tank.amount) {
                    // Move half the difference if the adjacent tank has less
                    StorageUtil.move(entity.tank, otherTank.tank, { true }, (entity.tank.amount - otherTank.tank.amount) / 2, null)
                }
            }
        }

    }

}
