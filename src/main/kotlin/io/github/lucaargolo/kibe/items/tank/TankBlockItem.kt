package io.github.lucaargolo.kibe.items.tank

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import io.github.lucaargolo.kibe.blocks.TANK
import io.github.lucaargolo.kibe.utils.FluidTank
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.World

class TankBlockItem(settings: Settings): BlockItem(TANK, settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val dummyTank = FluidTank(FluidAmount(16))
        val stackTag = stack.orCreateTag
        val blockEntityTag = stackTag.getCompound("BlockEntityTag")
        val tanksTag = blockEntityTag.getCompound("tanks")
        tanksTag.keys.forEach {  key ->
            val tankTag = tanksTag.getCompound(key)
            val volume = FluidVolume.fromTag(tankTag.getCompound("fluids"))
            dummyTank.volume = volume
        }
        if(!dummyTank.volume.isEmpty)
            tooltip.add(dummyTank.volume.fluidKey.name.shallowCopy().append(LiteralText(": ${Formatting.GRAY}${dummyTank.volume.amount().asInt(1000)}mB")))
        super.appendTooltip(stack, world, tooltip, context)
    }

}