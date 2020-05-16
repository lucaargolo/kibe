package io.github.lucaargolo.kibe.blocks.vacuum

import io.github.lucaargolo.kibe.blocks.getBlockId
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class VacuumHopper: BlockWithEntity(FabricBlockSettings.of(Material.GLASS).nonOpaque()) {

    override fun createBlockEntity(view: BlockView?) = VacuumHopperEntity(this)

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (!world.isClient) {
            ContainerProviderRegistry.INSTANCE.openContainer(getBlockId(this), player as ServerPlayerEntity?) { buf -> buf.writeBlockPos(pos) }
        }
        return ActionResult.SUCCESS
    }

    override fun isTranslucent(state: BlockState?, view: BlockView?, pos: BlockPos?): Boolean {
        return true
    }

    override fun isSimpleFullBlock(state: BlockState?, view: BlockView?, pos: BlockPos?): Boolean {
        return false
    }

    override fun hasSidedTransparency(state: BlockState?): Boolean {
        return true
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

}