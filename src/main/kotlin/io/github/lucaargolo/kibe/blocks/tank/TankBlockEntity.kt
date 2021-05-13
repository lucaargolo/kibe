package io.github.lucaargolo.kibe.blocks.tank

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.minus
import io.github.lucaargolo.kibe.utils.plus
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class TankBlockEntity(tank: Tank, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(tank), pos, state), BlockEntityClientSerializable {

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

    fun markDirtyAndSync() {
        markDirty()
        if(world?.isClient == false)
            sync()
    }

    private var broken = false

    fun markBroken() {
        broken = true
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.put("fluidInv", fluidInv.toTag())
        return if(broken) tag else super.writeNbt(tag)
    }


    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        fluidInv.fromTag(tag.getCompound("fluidInv"))
    }

    override fun toClientTag(tag: NbtCompound) = writeNbt(tag)

    override fun fromClientTag(tag: NbtCompound) = readNbt(tag)

    companion object {

        fun tick(world: World, pos: BlockPos, state: BlockState, entity: TankBlockEntity) {
            val fluid = if(entity.isEmpty) Fluids.EMPTY else entity.fluidKey.rawFluid ?: Fluids.EMPTY
            val luminance = fluid.defaultState.blockState.luminance
            if(luminance != state[Properties.LEVEL_15]) {
                world.setBlockState(pos, state.with(Properties.LEVEL_15, luminance))
            }
            if(entity.tickDelay++ < 10) return else entity.tickDelay = 0
            Direction.values().forEach {
                if(it == Direction.UP) return@forEach
                val otherTank = (world.getBlockEntity(pos.add(it.vector)) as? TankBlockEntity) ?: return@forEach

                if(it == Direction.DOWN && (otherTank.isEmpty || otherTank.fluidKey == entity.fluidKey)) {
                    if(otherTank.amount + entity.amount <= otherTank.capacity) {
                        otherTank.volume = entity.fluidKey.withAmount(otherTank.amount + entity.amount)
                        entity.amount = FluidAmount.ZERO
                    }else{
                        val dif = otherTank.capacity - otherTank.amount
                        otherTank.volume = entity.fluidKey.withAmount(otherTank.capacity)
                        entity.amount -= dif
                    }
                }

                if((otherTank.isEmpty || otherTank.fluidKey == entity.fluidKey) && entity.amount > otherTank.amount) {
                    var dif = (entity.amount - otherTank.amount).roundedDiv(2L)
                    dif = FluidAmount.of(dif.asLong(1000), 1000)
                    if(dif > FluidAmount.of(10, 1000)) {
                        if(dif > FluidAmount.BOTTLE) dif = FluidAmount.BOTTLE
                        otherTank.volume = entity.fluidKey.withAmount(otherTank.amount + dif)
                        entity.amount -= dif
                    }
                }
            }
        }

    }

}
