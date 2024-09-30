package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.block.EntangledChest
import io.github.lucaargolo.kibe.block.EntangledTank
import io.github.lucaargolo.kibe.blockentity.EntangledChestEntity
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import io.github.lucaargolo.kibe.menu.EntangledBagScreenHandler
import io.github.lucaargolo.kibe.utils.menu.ItemScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.*
import net.minecraft.world.World

class EntangledBag(settings: Settings): Item(settings){

    override fun appendTooltip(stack: ItemStack, context: TooltipContext?, tooltip: MutableList<Text>, type: TooltipType?) {
        super.appendTooltip(stack, context, tooltip, type)
        val ownerText = Text.translatable("tooltip.kibe.owner")
        val key = stack.get(ComponentTypeCompendium.ENTANGLED_KEY) ?: EntangledChest.DEFAULT_KEY
        if(key != EntangledTank.DEFAULT_KEY && stack.contains(ComponentTypeCompendium.OWNER))
            tooltip.add(ownerText.append(Text.literal(stack.get(ComponentTypeCompendium.OWNER)).formatted(Formatting.GRAY)))
        val color = Text.translatable("tooltip.kibe.color")
        var colorCode = ""
        if(stack.contains(ComponentTypeCompendium.RUNE_SET)) {
            stack.get(ComponentTypeCompendium.RUNE_SET)?.forEach { dc ->
                colorCode += dc.id.let { int -> Integer.toHexString(int) }
                val text = Text.literal("■")
                text.style = text.style.withColor(TextColor.fromRgb(dc.mapColor.color))
                color.append(text)
            }
        }else{
            colorCode = "00000000"
            color.append(Text.literal("■■■■■■■■"))
        }
        tooltip.add(color)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if(context.world.getBlockState(context.blockPos).block is EntangledChest && context.player != null && context.player!!.isSneaking) {
            val blockEntity = (context.world.getBlockEntity(context.blockPos) as EntangledChestEntity)
            val blockEntityTag = blockEntity.writeClientNbt(NbtCompound(), context.world.registryManager)
            val runeSet = mutableListOf<DyeColor>()
            (1..8).forEach {
                runeSet.add(DyeColor.byName(blockEntityTag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE)
            }
            context.stack.set(ComponentTypeCompendium.ENTANGLED_KEY, blockEntityTag.getString("key"))
            context.stack.set(ComponentTypeCompendium.OWNER, blockEntityTag.getString("owner"))
            context.stack.set(ComponentTypeCompendium.RUNE_SET, runeSet)
            context.stack.set(ComponentTypeCompendium.COLOR_CODE, blockEntity.colorCode)
            if(!context.world.isClient) context.player!!.sendMessage(Text.translatable("chat.kibe.entangled_bag.success"), true)
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        player.openHandledScreen(ItemScreenHandlerFactory(this, hand, player.getStackInHand(hand), ::EntangledBagScreenHandler))
        return TypedActionResult.success(player.getStackInHand(hand))
    }

}