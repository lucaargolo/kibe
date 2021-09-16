package io.github.lucaargolo.kibe.items.tank

import io.github.lucaargolo.kibe.blocks.TANK
import io.github.lucaargolo.kibe.utils.readTank
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class TankBlockItem(settings: Settings): BlockItem(TANK, settings) {

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if(!stack.hasNbt()) stack.orCreateNbt.put("BlockEntityTag", NbtCompound())
        super.inventoryTick(stack, world, entity, slot, selected)
    }

    override fun onCraft(stack: ItemStack, world: World, player: PlayerEntity) {
        stack.orCreateNbt.put("BlockEntityTag", NbtCompound())
        super.onCraft(stack, world, player)
    }

    override fun use(world: World?, user: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack> {
        val tag = user?.getStackInHand(hand)?.nbt
        tag?.let { println(tag.toString()) }
        return super.use(world, user, hand)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val stackTag = stack.nbt ?: return
        val blockEntityTag = stackTag.getCompound("BlockEntityTag")
        val dummyFluidTank = object: SingleVariantStorage<FluidVariant>() {
            override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
            override fun getCapacity(variant: FluidVariant?): Long = FluidConstants.BUCKET * 16
        }
        readTank(blockEntityTag, dummyFluidTank)
        if(!dummyFluidTank.isResourceBlank)
            tooltip.add(TranslatableText(dummyFluidTank.resource.fluid.defaultState.blockState.block.translationKey).append(LiteralText(": ${Formatting.GRAY}${dummyFluidTank.amount/81}mB")))
    }

}