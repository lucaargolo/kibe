package io.github.lucaargolo.kibe.blocks.vacuum

import io.github.lucaargolo.kibe.blocks.getBlockId
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*

class VacuumHopper: BlockWithEntity(FabricBlockSettings.of(Material.METAL, MaterialColor.IRON).method_29292().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL).nonOpaque()) {

    override fun createBlockEntity(view: BlockView?) = VacuumHopperEntity(this)

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (!world.isClient) {
            ContainerProviderRegistry.INSTANCE.openContainer(
                getBlockId(this),
                player as ServerPlayerEntity?
            ) { buf -> buf.writeBlockPos(pos) }
        }
        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is Inventory) {
                ItemScatterer.spawn(world, pos, blockEntity as Inventory?)
                //world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, notify)
        }
    }

    override fun randomDisplayTick(state: BlockState?, world: World?, pos: BlockPos?, random: Random?) {
        (Blocks.NETHER_PORTAL as NetherPortalBlock).randomDisplayTick(state, world, pos, random)
    }

    override fun isTranslucent(state: BlockState?, view: BlockView?, pos: BlockPos?): Boolean {
        return true
    }

    override fun hasSidedTransparency(state: BlockState?): Boolean {
        return true
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

}