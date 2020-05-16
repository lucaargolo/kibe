package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.blocks.CURSED_DIRT
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult

class CursedSeeds(settings: Settings): InfectedSeeds(settings) {
    override fun isSpreadableInBlockState(blockState: BlockState): Boolean {
        return blockState.block == Blocks.DIRT || blockState.block == Blocks.GRASS_BLOCK
    }
    override fun getBlockState(): BlockState = CURSED_DIRT.defaultState
}

class BlessedSeeds(settings: Settings): InfectedSeeds(settings) {
    override fun isSpreadableInBlockState(blockState: BlockState): Boolean {
        return blockState.block == Blocks.DIRT || blockState.block == Blocks.GRASS_BLOCK || blockState.block == CURSED_DIRT
    }
    override fun getBlockState(): BlockState = CURSED_DIRT.defaultState
}

abstract class InfectedSeeds(settings: Settings): Item(settings) {

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val blockState = context.world.getBlockState(context.blockPos)
        if(isSpreadableInBlockState(blockState) && context.world.getLightLevel(context.blockPos.up()) <= 7) {
            if(!context.world.isClient) {
                context.world.setBlockState(context.blockPos, getBlockState())
            }
            return ActionResult.CONSUME
        }
        return super.useOnBlock(context)
    }

    abstract fun isSpreadableInBlockState(blockState: BlockState): Boolean
    abstract fun getBlockState(): BlockState

}