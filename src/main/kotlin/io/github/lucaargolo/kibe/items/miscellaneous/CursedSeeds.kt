package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.blocks.CURSED_DIRT
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult


class CursedSeeds(settings: Settings): Item(settings) {

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val blockState = context.world.getBlockState(context.blockPos)
        if(blockState.block == Blocks.DIRT || blockState.block == Blocks.GRASS_BLOCK && context.world.getLightLevel(context.blockPos.up()) <= 7) {
            if(!context.world.isClient) {
                context.world.setBlockState(context.blockPos, CURSED_DIRT.defaultState)
            }
            return ActionResult.CONSUME
        }
        return super.useOnBlock(context)
    }


}