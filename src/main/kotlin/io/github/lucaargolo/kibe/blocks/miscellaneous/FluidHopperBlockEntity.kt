@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.readTank
import io.github.lucaargolo.kibe.utils.writeTank
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.block.BlockState
import net.minecraft.block.HopperBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class FluidHopperBlockEntity(block: FluidHopper, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(block), pos, state) {

    var extractableBump = 1L
    var insertableBump = 1L

    val tank = object: SingleVariantStorage<FluidVariant>() {
        override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
        override fun getCapacity(variant: FluidVariant?): Long = FluidConstants.BUCKET
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        super.writeNbt(tag)
        writeTank(tag, tank)
        tag.putByte("extractableBump", extractableBump.toByte())
        tag.putByte("insertableBump", insertableBump.toByte())
        return tag
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        readTank(tag, tank)
        extractableBump = tag.getByte("extractableBump").toLong()
        insertableBump = tag.getByte("insertableBump").toLong()
    }

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: FluidHopperBlockEntity) {
            if(!state[HopperBlock.ENABLED]) return
            val direction = state[HopperBlock.FACING]

            FluidStorage.SIDED.find(world, pos.up(), Direction.DOWN)?.let { extractable ->
                val moved = if(entity.tank.isResourceBlank) {
                    StorageUtil.move(extractable, entity.tank, { true }, (50*entity.extractableBump)*81, null)
                }else {
                    if(entity.tank.amount <= 950*81) {
                        StorageUtil.move(extractable, entity.tank, { it == entity.tank.variant }, (50*entity.extractableBump)*81, null)
                    }else{
                        0
                    }
                }
                if(moved == 0L && entity.extractableBump < 20) {
                    entity.extractableBump++
                }else{
                    entity.extractableBump = 1L
                }
            }

            FluidStorage.SIDED.find(world, pos.add(direction.vector), direction.opposite)?.let { insertable ->
                val moved = if(!entity.tank.isResourceBlank && entity.tank.amount >= 50*81) {
                   StorageUtil.move(entity.tank, insertable, { true }, (50*entity.insertableBump)*81, null)
                } else {
                    0
                }
                if(moved == 0L && entity.insertableBump < 20) {
                    entity.insertableBump++
                }else{
                    entity.insertableBump = 1L
                }
            }


        }
    }
}