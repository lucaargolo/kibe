package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.blocks.CURSED_DIRT
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult

class CursedSeeds(settings: Settings): Item(settings) {

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val blockState = context.world.getBlockState(context.blockPos)
        if(isSpreadableInBlockState(blockState) && context.world.getLightLevel(context.blockPos.up()) <= 7) {
            if(!context.world.isClient) {
                context.world.setBlockState(context.blockPos, CURSED_DIRT.defaultState)
                context.player?.playSound(SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1f, 0.8f)
            }
// I dont know how to spawn particles
//            (0..10).forEach { _ ->
//                val vx = (context.world.random.nextDouble()-0.5)/5.0
//                val vy = (context.world.random.nextDouble()-0.5)/5.0
//                val vz = (context.world.random.nextDouble()-0.5)/5.0
//                val vvy = (context.world.random.nextDouble())/30.0
//                context.world.addImportantParticle(ParticleTypes.SOUL_FIRE_FLAME, true, context.blockPos.x+0.5+vx, context.blockPos.y+0.5+vy+1.0, context.blockPos.z+0.5+vz, 0.0, 0.0+vvy, 0.0)
//            }
            context.stack.decrement(1)
            return ActionResult.SUCCESS
        }
        return super.useOnBlock(context)
    }

    private fun isSpreadableInBlockState(blockState: BlockState): Boolean {
        return blockState.block == Blocks.DIRT || blockState.block == Blocks.GRASS_BLOCK
    }

}