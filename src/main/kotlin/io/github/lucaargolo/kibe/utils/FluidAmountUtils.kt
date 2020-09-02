package io.github.lucaargolo.kibe.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount

operator fun FluidAmount.plus(amount: FluidAmount): FluidAmount = this.add(amount)

operator fun FluidAmount.minus(amount: FluidAmount): FluidAmount = this.sub(amount)