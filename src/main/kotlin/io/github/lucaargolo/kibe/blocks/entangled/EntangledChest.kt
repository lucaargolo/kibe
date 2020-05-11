package io.github.lucaargolo.kibe.blocks.entangled

import io.github.lucaargolo.kibe.blocks.getId
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.*
import net.minecraft.entity.EntityContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class EntangledChest: BlockWithEntity(FabricBlockSettings.of(Material.STONE)) {

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.ENTITYBLOCK_ANIMATED
    }

    override fun hasSidedTransparency(state: BlockState?): Boolean {
        return true
    }

    override fun getCollisionShape(state: BlockState?, view: BlockView?, pos: BlockPos?, context: EntityContext?): VoxelShape {
        return createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0)
    }

    override fun getOutlineShape(state: BlockState?, view: BlockView?, pos: BlockPos?, context: EntityContext?): VoxelShape {
        return createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0)
    }

    override fun createBlockEntity(view: BlockView?) = EntangledChestEntity(this)

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        return if(world.getBlockState(pos.up()).isAir) {
            if (!world.isClient) {
                ContainerProviderRegistry.INSTANCE.openContainer(getId(this), player as ServerPlayerEntity?) { buf -> buf.writeBlockPos(pos) }
            }
            ActionResult.SUCCESS
        }else{
            ActionResult.FAIL
        }
    }

}