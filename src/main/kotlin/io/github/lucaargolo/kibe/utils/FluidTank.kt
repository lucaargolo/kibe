package io.github.lucaargolo.kibe.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume

class FluidTank(var volume: FluidVolume, val capacity: FluidAmount) {

    constructor(capacity: FluidAmount) : this(FluidKeys.EMPTY.withAmount(FluidAmount.ZERO), capacity)

}