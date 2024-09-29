@file:Suppress("DEPRECATION", "UnstableApiUsage", "UNUSED_PARAMETER")

package io.github.lucaargolo.kibe.blockentity

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.block.EntangledTank
import io.github.lucaargolo.kibe.data.EntangledTankState
import io.github.lucaargolo.kibe.utils.SyncableBlockEntity
import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class EntangledTankEntity(pos: BlockPos, state: BlockState): SyncableBlockEntity(BlockEntityCompendium.ENTANGLED_TANK, pos, state) {

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
            return EntangledTankState.getPersistentState(serverWorld, key)
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
        return StorageUtil.calculateComparatorOutput(getPersistentState().getOrCreateInventory(colorCode))
    }

    override fun readNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.readNbt(tag, registryLookup)
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE
        }
        updateColorCode()
        key = tag.getString("key")
        owner = tag.getString("owner")
        isBeingCompared = tag.getBoolean("isBeingCompared")
        lastComparatorOutput = tag.getInt("lastComparatorOutput")
    }

    override fun readClientNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE
        }
        updateColorCode()
        key = tag.getString("key")
        owner = tag.getString("owner")
        FluidHelper.readTank(tag, getTank())
    }

    override fun writeNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.writeNbt(tag, registryLookup)
        (1..8).forEach {
            tag.putString("rune$it", runeColors[it]?.getName() ?: "white")
        }
        tag.putString("key", key)
        tag.putString("owner", owner)
        tag.putBoolean("isBeingCompared", isBeingCompared)
        tag.putInt("lastComparatorOutput", lastComparatorOutput)
        FluidHelper.writeTank(tag, getTank())
    }

    override fun writeClientNbt(tag: NbtCompound, registryLookup: WrapperLookup) = tag.also { writeNbt(it, registryLookup) }

    companion object {
        fun getFluidStorage(be: EntangledTankEntity, dir: Direction?): Storage<FluidVariant> {
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
            if(luminance != state[Properties.LEVEL_15] && KibeMod.CONFIG.miscellaneousModule.tanksChangeLights) {
                world.setBlockState(pos, state.with(Properties.LEVEL_15, luminance))
            }
        }

    }
}