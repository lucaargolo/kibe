package io.github.lucaargolo.kibe.blocks.entangled

import io.github.lucaargolo.kibe.MOD_ID
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.function.Supplier

class EntangledChest: BlockWithEntity(FabricBlockSettings.of(Material.STONE).build()) {

    val id: Identifier = Identifier(MOD_ID, "entangled_chest")
    val entityType = BlockEntityType.Builder.create(Supplier { this.createBlockEntity(null) }, this).build(null)

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.ENTITYBLOCK_ANIMATED
    }

    override fun createBlockEntity(view: BlockView?): BlockEntity = EntangledChestEntity(this)

    override fun onUse(state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult {
        if (!world!!.isClient) {
            ContainerProviderRegistry.INSTANCE.openContainer(id, player as ServerPlayerEntity?) { buf -> buf.writeBlockPos(pos) }
        }
        return ActionResult.SUCCESS
    }

}