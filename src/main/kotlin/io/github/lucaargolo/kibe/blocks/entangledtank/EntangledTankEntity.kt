@file:Suppress("DEPRECATION", "UnstableApiUsage", "UNUSED_PARAMETER")

package io.github.lucaargolo.kibe.blocks.entangledtank

import io.github.lucaargolo.kibe.MOD_CONFIG
import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.readTank
import io.github.lucaargolo.kibe.utils.writeTank
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class EntangledTankEntity(chest: EntangledTank, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(chest), pos, state), BlockEntityClientSerializable {

    var lastRenderedFluid = 0f
    var runeColors = mutableMapOf<Int, DyeColor>()
    var key = EntangledTank.DEFAULT_KEY
    var owner = ""

    init {
        (1..8).forEach {
            runeColors[it] = DyeColor.WHITE
        }
        updateColorCode()
    }

    var colorCode = "00000000"

    fun getPersistentState(): EntangledTankState {
        val serverWorld = world as? ServerWorld
        if (serverWorld != null) {
            return serverWorld.server.overworld.persistentStateManager.getOrCreate( {EntangledTankState.createFromTag(it, serverWorld, key )}, { EntangledTankState(serverWorld, key) }, key)
        } else {
            return EntangledTankState.getOrCreateClientState(key)
        }
    }

    fun getTank(): SingleVariantStorage<FluidVariant> {
        return getPersistentState().getOrCreateInventory(colorCode)
    }

    fun updateColorCode() {
        var code = ""
        (1..8).forEach {
            code += runeColors[it]?.id?.let { int -> Integer.toHexString(int) }
        }
        colorCode = code
    }

    fun markDirtyAndSync() {
        super.markDirty()
        if(world?.isClient == false)
            sync()
    }

    override fun markDirty() {
        getPersistentState().markDirty(colorCode)
        super.markDirty()
    }

    private var lastComparatorOutput = 0
    var isBeingCompared = false

    fun getComparatorOutput(): Int {
        val comparatorOutput = calculateComparatorOutput()
        isBeingCompared = true
        lastComparatorOutput = comparatorOutput
        return comparatorOutput
    }

    private fun calculateComparatorOutput(): Int {
        return StorageUtil.calculateComparatorOutput(getPersistentState().getOrCreateInventory(colorCode), null)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
        }
        updateColorCode()
        key = tag.getString("key")
        owner = tag.getString("owner")
        isBeingCompared = tag.getBoolean("isBeingCompared")
        lastComparatorOutput = tag.getInt("lastComparatorOutput")
    }

    override fun fromClientTag(tag: NbtCompound) {
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
        }
        updateColorCode()
        key = tag.getString("key")
        owner = tag.getString("owner")
        readTank(tag, getTank())
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        super.writeNbt(tag)
        (1..8).forEach {
            tag.putString("rune$it", runeColors[it]?.getName() ?: "white")
        }
        tag.putString("key", key)
        tag.putString("owner", owner)
        tag.putBoolean("isBeingCompared", isBeingCompared)
        tag.putInt("lastComparatorOutput", lastComparatorOutput)
        writeTank(tag, getTank())
        return tag
    }

    override fun toClientTag(tag: NbtCompound) = writeNbt(tag)

    companion object {
        fun getFluidStorage(be: EntangledTankEntity, dir: Direction): Storage<FluidVariant> {
            return be.getTank()
        }

        fun tick(world: World, pos: BlockPos, state: BlockState, entity: EntangledTankEntity) {
            if(!world.isClient && entity.isBeingCompared) {
                val comparatorOutput = entity.calculateComparatorOutput()
                if(comparatorOutput != entity.lastComparatorOutput) {
                    world.updateComparators(pos, state.block)
                }
            }
            val fluid = entity.getPersistentState().getOrCreateInventory(entity.colorCode).variant.fluid
            val luminance = fluid.defaultState.blockState.luminance
            if(luminance != state[Properties.LEVEL_15] && MOD_CONFIG.miscellaneousModule.tanksChangeLights) {
                world.setBlockState(pos, state.with(Properties.LEVEL_15, luminance))
            }
        }

    }
}