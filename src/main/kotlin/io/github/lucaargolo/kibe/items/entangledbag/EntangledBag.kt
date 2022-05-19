package io.github.lucaargolo.kibe.items.entangledbag

import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChest
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChestEntity
import io.github.lucaargolo.kibe.utils.ItemScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound

import net.minecraft.text.Text
import net.minecraft.text.TextColor

import net.minecraft.util.*
import net.minecraft.world.World

class EntangledBag(settings: Settings): Item(settings){

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val tag = getTag(stack)
        val ownerText = Text.translatable("tooltip.kibe.owner")
        if(tag.getString("key") != EntangledChest.DEFAULT_KEY) tooltip.add(ownerText.append(Text.literal(tag.getString("owner")).formatted(Formatting.GRAY)))
        val color = Text.translatable("tooltip.kibe.color")
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE
            val text = Text.literal("■")
            text.style = text.style.withColor(TextColor.fromRgb(dc.mapColor.color))
            color.append(text)
        }
        tooltip.add(color)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if(context.world.getBlockState(context.blockPos).block is EntangledChest && context.player != null && context.player!!.isSneaking) {
            val blockEntity = (context.world.getBlockEntity(context.blockPos) as EntangledChestEntity)
            val blockEntityTag = blockEntity.writeClientNbt(NbtCompound())
            val newTag = NbtCompound()
            newTag.putString("key", blockEntityTag.getString("key"))
            newTag.putString("owner", blockEntityTag.getString("owner"))
            (1..8).forEach {
                newTag.putString("rune$it", blockEntityTag.getString("rune$it"))
            }
            newTag.putString("colorCode", blockEntity.colorCode)
            context.stack.nbt = newTag
            if(!context.world.isClient) context.player!!.sendMessage(Text.translatable("chat.kibe.entangled_bag.success"), true)
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    private fun getTag(stack: ItemStack): NbtCompound {
        return if(stack.hasNbt()) {
            stack.orCreateNbt
        }else{
            val newTag = NbtCompound()
            newTag.putString("key", EntangledChest.DEFAULT_KEY)
            (1..8).forEach {
                newTag.putString("rune$it", DyeColor.WHITE.name)
            }
            newTag.putString("colorCode", "00000000")
            newTag
        }
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val tag = getTag(player.getStackInHand(hand))
        player.openHandledScreen(ItemScreenHandlerFactory(this, hand, tag))
        return TypedActionResult.success(player.getStackInHand(hand))
    }

}