package io.github.lucaargolo.kibe.blocks.tank

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import io.github.lucaargolo.kibe.MOD_CONFIG
import io.github.lucaargolo.kibe.blocks.TANK
import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.minus
import io.github.lucaargolo.kibe.utils.plus
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.CompoundTag
import net.minecraft.state.property.Properties
import net.minecraft.util.Tickable
import net.minecraft.util.math.Direction

class TankBlockEntity(tank: Tank): BlockEntity(getEntityType(tank)), Tickable, BlockEntityClientSerializable {

    var lastRenderedFluid = 0f
    var tickDelay = 0

    val fluidInv = SimpleFixedFluidInv(1, FluidAmount.ofWhole(16))

    init {
        fluidInv.addListener ( { _, _, _, _ -> markDirtyAndSync() }, {  })
    }

    var volume: FluidVolume
        get() = fluidInv.getInvFluid(0)
        set(value) {
            fluidInv.setInvFluid(0, value, Simulation.ACTION)
            markDirtyAndSync()
        }

    val fluidKey: FluidKey
        get() = volume.fluidKey

    var amount: FluidAmount
        get() = volume.amount()
        set(value) {
            fluidInv.setInvFluid(0, fluidKey.withAmount(value), Simulation.ACTION)
            markDirtyAndSync()
        }

    val isEmpty: Boolean
        get() = volume.isEmpty

    val capacity: FluidAmount
        get() = fluidInv.tankCapacity_F

    override fun tick() {
        val world = world ?: return
        val fluid = if(this.isEmpty) Fluids.EMPTY else this.fluidKey.rawFluid ?: Fluids.EMPTY
        val luminance = fluid.defaultState.blockState.luminance
        if(luminance != cachedState[Properties.LEVEL_15] && MOD_CONFIG.miscellaneousModule.tanksChangeLights) {
            world.setBlockState(pos, cachedState.with(Properties.LEVEL_15, luminance))
        }
        if(tickDelay++ < 10) return else tickDelay = 0
        Direction.values().forEach {
            if(it == Direction.UP) return@forEach
            val otherTank = (world.getBlockEntity(pos.add(it.vector)) as? TankBlockEntity) ?: return@forEach

            if(it == Direction.DOWN && (otherTank.isEmpty || otherTank.fluidKey == this.fluidKey)) {
                if(otherTank.amount + this.amount <= otherTank.capacity) {
                    otherTank.volume = this.fluidKey.withAmount(otherTank.amount + this.amount)
                    this.amount = FluidAmount.ZERO
                }else{
                    val dif = otherTank.capacity - otherTank.amount
                    otherTank.volume = this.fluidKey.withAmount(otherTank.capacity)
                    this.amount -= dif
                }
            }

            if((otherTank.isEmpty || otherTank.fluidKey == this.fluidKey) && this.amount > otherTank.amount) {
                var dif = (this.amount - otherTank.amount).roundedDiv(2L)
                dif = FluidAmount.of(dif.asLong(1000), 1000)
                if(dif > FluidAmount.of(10, 1000)) {
                    if(dif > FluidAmount.BOTTLE) dif = FluidAmount.BOTTLE
                    otherTank.volume = this.fluidKey.withAmount(otherTank.amount + dif)
                    this.amount -= dif
                }
            }
        }
    }

    fun markDirtyAndSync() {
        markDirty()
        if(world?.isClient == false)
            sync()
    }

    private var broken = false

    fun markBroken() {
        broken = true
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.put("fluidInv", fluidInv.toTag())
        return if(broken) tag else super.toTag(tag)
    }


    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        fluidInv.fromTag(tag.getCompound("fluidInv"))
    }

    override fun toClientTag(tag: CompoundTag) = toTag(tag)

    override fun fromClientTag(tag: CompoundTag) = fromTag(TANK.defaultState, tag)

}
