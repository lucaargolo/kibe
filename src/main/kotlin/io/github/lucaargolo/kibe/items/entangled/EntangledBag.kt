package io.github.lucaargolo.kibe.items.entangled

import io.github.lucaargolo.kibe.blocks.entangled.EntangledChest
import io.github.lucaargolo.kibe.blocks.entangled.EntangledChestEntity
import io.github.lucaargolo.kibe.items.getItemId
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.client.util.TextFormat
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class EntangledBag: Item(Settings().group(ItemGroup.MISC)){

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if(context.world.getBlockState(context.blockPos).block is EntangledChest && context.player != null && context.player!!.isSneaking) {
            val blockEntity = (context.world.getBlockEntity(context.blockPos) as EntangledChestEntity)
            val blockEntityTag = blockEntity.toTag(CompoundTag())
            val newTag = CompoundTag()
            newTag.putString("key", blockEntityTag.getString("key"))
            (1..8).forEach {
                newTag.putString("rune$it", blockEntityTag.getString("rune$it"))
            }
            newTag.putString("colorCode", blockEntity.getColorCode())
            context.stack.tag = newTag
            if(!context.world.isClient) context.player!!.sendMessage(LiteralText("${TextFormat.GOLD}Successfully linked bag!"))
            return ActionResult.SUCCESS
        }
        return ActionResult.FAIL
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if(!world.isClient) {
            val tag = player.getStackInHand(hand).orCreateTag
            ContainerProviderRegistry.INSTANCE.openContainer(getItemId(this), player as ServerPlayerEntity?) { buf -> buf.writeCompoundTag(tag) }
        }
        return TypedActionResult.success(player.getStackInHand(hand))
    }


}