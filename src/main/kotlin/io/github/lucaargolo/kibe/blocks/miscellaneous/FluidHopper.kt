package io.github.lucaargolo.kibe.blocks.miscellaneous

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FluidHopper: HopperBlock(FabricBlockSettings.of(Material.METAL, MapColor.STONE_GRAY).requiresTool().strength(3.0F, 4.8F).sounds(BlockSoundGroup.METAL).nonOpaque()), AttributeProvider {

    override fun onUse(state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult {
        return ActionResult.PASS
    }

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return FluidHopperBlockEntity(this, blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, blockState: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return BlockEntityTicker { wrld, pos, state, blockEntity -> FluidHopperBlockEntity.tick(wrld, pos, state, blockEntity as FluidHopperBlockEntity) }
    }

    override fun hasComparatorOutput(state: BlockState?) = false

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState?, to: AttributeList<*>) {
        (world.getBlockEntity(pos) as? FluidHopperBlockEntity)?.let {
            to.offer(it.fluidInv)
        }
    }
}