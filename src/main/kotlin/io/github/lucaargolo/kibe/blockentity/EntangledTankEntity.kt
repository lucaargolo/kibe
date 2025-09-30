@file:Suppress("DEPRECATION", "UNUSED_PARAMETER")

package io.github.lucaargolo.kibe.blockentity

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.block.EntangledTank
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import io.github.lucaargolo.kibe.data.state.EntangledTankState
import io.github.lucaargolo.kibe.utils.SyncableBlockEntity
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.block.BlockState
import net.minecraft.component.ComponentMap
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

    val runeColors : Array<DyeColor> = arrayOf(DyeColor.WHITE, DyeColor.WHITE, DyeColor.WHITE, DyeColor.WHITE, DyeColor.WHITE, DyeColor.WHITE, DyeColor.WHITE, DyeColor.WHITE)
    val colorCode : String
        get() = runeColors.map(DyeColor::getId).joinToString(separator = "", transform = Integer::toHexString)

    var key = EntangledTank.DEFAULT_KEY
    var owner = ""

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
        (0 until runeColors.size).forEach { idx ->
            runeColors[idx] = DyeColor.byName(tag.getString("rune${idx+1}"), DyeColor.WHITE) ?: DyeColor.WHITE
        }
        key = tag.getString("key")
        owner = tag.getString("owner")
        isBeingCompared = tag.getBoolean("isBeingCompared")
        lastComparatorOutput = tag.getInt("lastComparatorOutput")
    }

    override fun readClientNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        (0 until runeColors.size).forEach { idx ->
            runeColors[idx] = DyeColor.byName(tag.getString("rune${idx+1}"), DyeColor.WHITE) ?: DyeColor.WHITE
        }
        key = tag.getString("key")
        owner = tag.getString("owner")
    }

    override fun writeNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.writeNbt(tag, registryLookup)
        runeColors.forEachIndexed { idx, col ->
            tag.putString("rune${idx+1}", col.name)
        }
        tag.putString("key", key)
        tag.putString("owner", owner)
        tag.putBoolean("isBeingCompared", isBeingCompared)
        tag.putInt("lastComparatorOutput", lastComparatorOutput)
    }

    override fun writeClientNbt(tag: NbtCompound, registryLookup: WrapperLookup) = tag.also { writeNbt(it, registryLookup) }

    override fun addComponents(builder: ComponentMap.Builder) {
        builder.add(ComponentTypeCompendium.RUNE_SET, runeColors.toList())
        builder.add(ComponentTypeCompendium.ENTANGLED_KEY, key)
        builder.add(ComponentTypeCompendium.OWNER, owner)
    }

    override fun readComponents(components: ComponentsAccess) {
        components.get(ComponentTypeCompendium.RUNE_SET)?.forEachIndexed { index, component ->
            this.runeColors[index] = component
        }
        components.get(ComponentTypeCompendium.ENTANGLED_KEY)?.let { this.key = it }
        components.get(ComponentTypeCompendium.OWNER)?.let { this.owner = it }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("nbt.remove(\"Items\")"))
    override fun removeFromCopiedStackNbt(nbt: NbtCompound) {
        (1..8).forEach {
            nbt.remove("rune$it")
        }
        nbt.remove("key")
        nbt.remove("owner")
    }

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