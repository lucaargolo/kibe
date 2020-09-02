package io.github.lucaargolo.kibe.items.tank

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import io.github.lucaargolo.kibe.blocks.TANK
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class TankBlockItem(settings: Settings): BlockItem(TANK, settings) {

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if(!stack.hasTag()) stack.orCreateTag.put("BlockEntityTag", CompoundTag())
        super.inventoryTick(stack, world, entity, slot, selected)
    }

    override fun onCraft(stack: ItemStack, world: World, player: PlayerEntity) {
        stack.orCreateTag.put("BlockEntityTag", CompoundTag())
        super.onCraft(stack, world, player)
    }

    override fun use(world: World?, user: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack> {
        val tag = user?.getStackInHand(hand)?.tag
        tag?.let { println(tag.toString()) }
        return super.use(world, user, hand)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val stackTag = stack.tag ?: return
        val blockEntityTag = stackTag.getCompound("BlockEntityTag")
        val dummyFluidInv = SimpleFixedFluidInv(1, FluidAmount(16))
        dummyFluidInv.fromTag(blockEntityTag.getCompound("fluidInv"))
        if(!dummyFluidInv.getInvFluid(0).isEmpty)
            tooltip.add(dummyFluidInv.getInvFluid(0).fluidKey.name.shallowCopy().append(LiteralText(": ${Formatting.GRAY}${dummyFluidInv.getInvFluid(0).amount().asInt(1000)}mB")))
    }

}