package io.github.lucaargolo.kibe.blocks.miscellaneous

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.HopperBlock
import net.minecraft.block.Material
import net.minecraft.block.MaterialColor
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class FluidHopper: HopperBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.STONE).requiresTool().strength(3.0F, 4.8F).sounds(BlockSoundGroup.METAL).nonOpaque()), AttributeProvider {

    override fun onUse(state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult {
        return ActionResult.PASS
    }

    override fun createBlockEntity(world: BlockView?) = FluidHopperBlockEntity(this)

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos): Int {
        return (world.getBlockEntity(pos) as? FluidHopperBlockEntity)?.fluidInv?.let {
            val p = it.getInvFluid(0).amount_F.asInt(1000).toFloat()/it.tankCapacity_F.asInt(1000).toFloat()
            return (p*14).toInt() + if (!it.getInvFluid(0).isEmpty) 1 else 0
        } ?: 0
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? FluidHopperBlockEntity)?.let {
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState?, to: AttributeList<*>) {
        (world.getBlockEntity(pos) as? FluidHopperBlockEntity)?.let {
            to.offer(it.fluidInv)
        }
    }
}