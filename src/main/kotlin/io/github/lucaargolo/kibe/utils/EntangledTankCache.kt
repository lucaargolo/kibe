package io.github.lucaargolo.kibe.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import net.minecraft.nbt.CompoundTag
import java.util.function.Supplier

object EntangledTankCache {

    private var isDirtyMap = linkedMapOf<Pair<String, String>, Boolean>()
    private var fluidInvMap = linkedMapOf<Pair<String, String>, SimpleFixedFluidInv>()

    fun reset() {
        isDirtyMap = linkedMapOf()
        fluidInvMap = linkedMapOf()
    }

    fun markDirty(key: String, colorCode: String) {
        isDirtyMap[Pair(key, colorCode)] = true
    }

    fun isDirty(key: String, colorCode: String): Boolean {
        return isDirtyMap[Pair(key, colorCode)] ?: true
    }

    fun getOrCreateClientFluidInv(key: String, colorCode: String, compoundTag: CompoundTag): SimpleFixedFluidInv {
        return fluidInvMap[Pair(key, colorCode)] ?: Supplier {
            val fluidInv = SimpleFixedFluidInv(1, FluidAmount(16))
            fluidInv.fromTag(compoundTag)
            fluidInvMap[Pair(key, colorCode)] = fluidInv
            fluidInv
        }.get()
    }

    fun updateClientFluidInv(key: String, colorCode: String, compoundTag: CompoundTag) {
        val fluidInv = SimpleFixedFluidInv(1, FluidAmount(16))
        fluidInv.fromTag(compoundTag)
        fluidInvMap[Pair(key, colorCode)] = fluidInv
        isDirtyMap[Pair(key, colorCode)] = false
    }
}