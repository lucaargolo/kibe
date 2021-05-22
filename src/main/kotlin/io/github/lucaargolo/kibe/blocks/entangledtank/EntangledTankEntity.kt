package io.github.lucaargolo.kibe.blocks.entangledtank

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import io.github.lucaargolo.kibe.MOD_CONFIG
import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.DyeColor
import net.minecraft.util.Tickable

class EntangledTankEntity(chest: EntangledTank): BlockEntity(getEntityType(chest)), BlockEntityClientSerializable, Tickable {

    var lastRenderedFluid = 0f
    var fluidInv = object: SimpleFixedFluidInv(1, FluidAmount.ofWhole(16)) {
        override fun getInvFluid(tank: Int): FluidVolume {
            return persistentState?.getOrCreateInventory(colorCode)?.getInvFluid(tank) ?: super.getInvFluid(tank)
        }
        override fun setInvFluid(tank: Int, to: FluidVolume?, simulation: Simulation?): Boolean {
            val bl = persistentState?.getOrCreateInventory(colorCode)?.setInvFluid(tank, to, simulation) ?: super.setInvFluid(tank, to, simulation)
            markDirty()
            return bl
        }
    }

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

    val persistentState: EntangledTankState?
        get() = (world as? ServerWorld)?.let { serverWorld ->
            serverWorld.server.overworld.persistentStateManager.getOrCreate( { EntangledTankState(serverWorld, key) }, key)
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
        persistentState?.markDirty(colorCode)
        super.markDirty()
    }

    private var lastComparatorOutput = 0
    var isBeingCompared = false

    fun getComparatorOutput(): Int {
        val comparatorOutput = fluidInv.let {
            val p = it.getInvFluid(0).amount_F.asInt(1000).toFloat()/it.tankCapacity_F.asInt(1000).toFloat()
            (p*14).toInt() + if (!it.getInvFluid(0).isEmpty) 1 else 0
        }
        isBeingCompared = true
        lastComparatorOutput = comparatorOutput
        return comparatorOutput
    }

    override fun tick() {
        val world = world ?: return
        if(!world.isClient && isBeingCompared) {
            val comparatorOutput = fluidInv.let {
                val p = it.getInvFluid(0).amount_F.asInt(1000).toFloat()/it.tankCapacity_F.asInt(1000).toFloat()
                (p*14).toInt() + if (!it.getInvFluid(0).isEmpty) 1 else 0
            }
            if(comparatorOutput != lastComparatorOutput) {
                world.updateComparators(pos, cachedState.block)
            }
        }
        val fluid = if(fluidInv.getInvFluid(0).isEmpty) Fluids.EMPTY else fluidInv.getInvFluid(0).rawFluid ?: Fluids.EMPTY
        val luminance = fluid.defaultState.blockState.luminance
        if(luminance != cachedState[Properties.LEVEL_15] && MOD_CONFIG.miscellaneousModule.tanksChangeLights) {
            world.setBlockState(pos, cachedState.with(Properties.LEVEL_15, luminance))
        }
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
        }
        updateColorCode()
        key = tag.getString("key")
        owner = tag.getString("owner")
        isBeingCompared = tag.getBoolean("isBeingCompared")
        lastComparatorOutput = tag.getInt("lastComparatorOutput")
    }

    override fun fromClientTag(tag: CompoundTag) {
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
        }
        updateColorCode()
        key = tag.getString("key")
        owner = tag.getString("owner")
        fluidInv.fromTag(tag)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        (1..8).forEach {
            tag.putString("rune$it", runeColors[it]?.getName() ?: "white")
        }
        tag.putString("key", key)
        tag.putString("owner", owner)
        tag.putBoolean("isBeingCompared", isBeingCompared)
        tag.putInt("lastComparatorOutput", lastComparatorOutput)
        if(persistentState != null) {
            var subTag = CompoundTag()
            subTag = persistentState!!.toTag(subTag)
            subTag = subTag.getCompound(colorCode)
            tag.put("tanks", subTag.get("tanks") ?: ListTag())
        }
        else fluidInv.toTag(tag)
        return tag
    }

    override fun toClientTag(tag: CompoundTag) = toTag(tag)

}