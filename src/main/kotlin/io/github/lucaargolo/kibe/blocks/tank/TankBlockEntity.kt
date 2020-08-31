package io.github.lucaargolo.kibe.blocks.tank

import alexiil.mc.lib.attributes.ListenerRemovalToken
import alexiil.mc.lib.attributes.ListenerToken
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FixedFluidInv
import alexiil.mc.lib.attributes.fluid.FluidInvTankChangeListener
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.FluidTank
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.CompoundTag
import net.minecraft.state.property.Properties
import net.minecraft.util.Tickable
import net.minecraft.util.math.Direction

class TankBlockEntity(val tank: Tank): BlockEntity(getEntityType(tank)), FixedFluidInv, Tickable, BlockEntityClientSerializable {

    val tanks = listOf(FluidTank(FluidAmount(16)))
    var lastRenderedFluid = 0f
    var tickDelay = 0

    override fun getTankCount() = tanks.size

    override fun isFluidValidForTank(tank: Int, fluidKey: FluidKey?) = tanks[tank].volume.fluidKey == fluidKey || tanks[tank].volume.fluidKey.isEmpty

    override fun getMaxAmount_F(tank: Int) = tanks[tank].capacity

    override fun getInvFluid(tank: Int) = tanks[tank].volume

    override fun setInvFluid(tank: Int, to: FluidVolume, simulation: Simulation?): Boolean {
        return if (isFluidValidForTank(tank, to.fluidKey)) {
            if (simulation?.isAction == true)
                tanks[tank].volume = to
            markDirtyAndSync()
            true
        } else false
    }

    override fun addListener(p0: FluidInvTankChangeListener?, p1: ListenerRemovalToken?) = ListenerToken {}

    override fun tick() {
        if(tickDelay++ < 10) return else tickDelay = 0
        val world = world ?: return
        Direction.values().forEach {
            if(it == Direction.UP) return@forEach
            val be = (world.getBlockEntity(pos.add(it.vector)) as? TankBlockEntity) ?: return@forEach
            if(it == Direction.DOWN && (be.tanks[0].volume.isEmpty || be.tanks[0].volume.fluidKey == tanks[0].volume.fluidKey)) {
                if(be.tanks[0].volume.amount().add(tanks[0].volume.amount()) <= be.tanks[0].capacity) {
                    be.tanks[0].volume = tanks[0].volume.fluidKey.withAmount(be.tanks[0].volume.amount().add(tanks[0].volume.amount()))
                    be.markDirtyAndSync()
                    tanks[0].volume = FluidKeys.EMPTY.withAmount(FluidAmount.ZERO)
                    markDirtyAndSync()
                }else{
                    val dif = be.tanks[0].capacity.sub(be.tanks[0].volume.amount())
                    be.tanks[0].volume = be.tanks[0].volume.fluidKey.withAmount(be.tanks[0].capacity)
                    be.markDirtyAndSync()
                    tanks[0].volume = tanks[0].volume.fluidKey.withAmount(tanks[0].volume.amount().sub(dif))
                    markDirtyAndSync()
                }
            }
            if((be.tanks[0].volume.isEmpty || be.tanks[0].volume.fluidKey == tanks[0].volume.fluidKey) && tanks[0].volume.amount() > be.tanks[0].volume.amount()) {
                var dif = be.tanks[0].volume.amount().add(tanks[0].volume.amount()).div(2).sub(be.tanks[0].volume.amount())
                if(dif < FluidAmount.of(10, 1000)) return@forEach
                if(dif > FluidAmount.BOTTLE) dif = FluidAmount.BOTTLE
                be.tanks[0].volume = tanks[0].volume.fluidKey.withAmount(be.tanks[0].volume.amount().add(dif))
                be.markDirtyAndSync()
                tanks[0].volume = tanks[0].volume.fluidKey.withAmount(tanks[0].volume.amount().sub(dif))
                markDirtyAndSync()
            }
        }
        val volume = tanks[0].volume
        val fluid = volume.fluidKey.rawFluid ?: Fluids.EMPTY
        val blockState = fluid.defaultState.blockState
        val l = blockState.luminance
        val p = volume.amount().asLong(1L)/16f
        val x = (l*p).toInt()
        if(x != cachedState[Properties.LEVEL_15]) {
            world.setBlockState(pos, cachedState.with(Properties.LEVEL_15, x))
        }
    }

    fun markDirtyAndSync() {
        markDirty()
        if(world?.isClient == false)
            sync()
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        val tanksTag = CompoundTag()
        tanks.forEachIndexed { index, tank ->
            val tankTag = CompoundTag()
            tankTag.put("fluids", tank.volume.toTag())
            tanksTag.put(index.toString(), tankTag)
        }
        tag.put("tanks", tanksTag)
        return super.toTag(tag)
    }


    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        val tanksTag = tag.getCompound("tanks")
        tanksTag.keys.forEachIndexed { idx, key ->
            val tankTag = tanksTag.getCompound(key)
            val volume = FluidVolume.fromTag(tankTag.getCompound("fluids"))
            tanks[idx].volume = volume
        }
    }

    override fun toClientTag(tag: CompoundTag) = toTag(tag)

    override fun fromClientTag(tag: CompoundTag) = fromTag(tank.defaultState, tag)

}