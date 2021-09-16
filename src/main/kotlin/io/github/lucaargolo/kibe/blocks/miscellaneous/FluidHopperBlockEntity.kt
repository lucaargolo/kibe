package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.readTank
import io.github.lucaargolo.kibe.utils.writeTank
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.block.BlockState
import net.minecraft.block.HopperBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class FluidHopperBlockEntity(block: FluidHopper, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(block), pos, state) {

    val tank = object: SingleVariantStorage<FluidVariant>() {
        override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
        override fun getCapacity(variant: FluidVariant?): Long = FluidConstants.BUCKET
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        super.writeNbt(tag)
        writeTank(tag, tank)
        return tag
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        readTank(tag, tank)
    }

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: FluidHopperBlockEntity) {
            if(!state[HopperBlock.ENABLED]) return
            val direction = state[HopperBlock.FACING]

            FluidStorage.SIDED.find(world, pos.up(), Direction.DOWN)?.let { extractable ->
                if(entity.tank.isResourceBlank) {
                    StorageUtil.move(extractable, entity.tank, { true }, 50*81, null)
                }else {
                    if(entity.tank.amount <= 950*81) {
                        StorageUtil.move(extractable, entity.tank, { it == entity.tank.variant }, 50*81, null)
                    }else{
                        0
                    }
                }
            }

            FluidStorage.SIDED.find(world, pos.add(direction.vector), direction.opposite)?.let { insertable ->
                if(!entity.tank.isResourceBlank && entity.tank.amount >= 50*81) {
                   StorageUtil.move(entity.tank, insertable, { true }, 50*81, null)
                } else {
                    0
                }
            }


        }
    }
}