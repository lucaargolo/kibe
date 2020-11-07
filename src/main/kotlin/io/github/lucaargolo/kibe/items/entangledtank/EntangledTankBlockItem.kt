package io.github.lucaargolo.kibe.items.entangledtank

import io.github.lucaargolo.kibe.REQUEST_ENTANGLED_TANK_SYNC_C2S
import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTank
import io.github.lucaargolo.kibe.utils.EntangledTankCache
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.TranslatableText
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity
import net.minecraft.world.World

class EntangledTankBlockItem(settings: Settings): BlockItem(ENTANGLED_TANK, settings.rarity(Rarity.RARE)) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val tag = if(stack.hasTag() && stack.tag!!.contains("BlockEntityTag") ) {
            stack.orCreateTag.get("BlockEntityTag") as CompoundTag
        }else{
            val newTag = CompoundTag()
            newTag.putString("key", EntangledTank.DEFAULT_KEY)
            (1..8).forEach {
                newTag.putString("rune$it", DyeColor.WHITE.name)
            }
            newTag.putString("colorCode", "00000000")
            newTag
        }
        val ownerText = TranslatableText("tooltip.kibe.owner")
        if(tag.getString("key") != EntangledTank.DEFAULT_KEY) tooltip.add(ownerText.append(LiteralText(tag.getString("owner")).formatted(Formatting.GRAY)))
        val color = TranslatableText("tooltip.kibe.color")
        var colorCode = ""
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
            colorCode += dc.id.let { int -> Integer.toHexString(int) }
            val text = LiteralText("■")
            text.style = text.style.withColor(TextColor.fromRgb(dc.mapColor.color))
            color.append(text)
        }
        tooltip.add(color)
        val key = tag.getString("key")
        if(EntangledTankCache.isDirty(key, colorCode)) {
            val passedData = PacketByteBuf(Unpooled.buffer())
            passedData.writeString(key)
            passedData.writeString(colorCode)
            ClientSidePacketRegistry.INSTANCE.sendToServer(REQUEST_ENTANGLED_TANK_SYNC_C2S, passedData)
        }
        val fluidInv = EntangledTankCache.getOrCreateClientFluidInv(key, colorCode, tag)
        fluidInv.toTag(tag)
        if(!fluidInv.getInvFluid(0).isEmpty)
            tooltip.add(fluidInv.getInvFluid(0).fluidKey.name.shallowCopy().append(LiteralText(": ${Formatting.GRAY}${fluidInv.getInvFluid(0).amount().asInt(1000)}mB")))
    }

}