package io.github.lucaargolo.kibe.block

import com.mojang.serialization.MapCodec
import io.github.lucaargolo.kibe.blockentity.TrashCanEntity
import io.github.lucaargolo.kibe.menu.TrashCanScreenHandler
import io.github.lucaargolo.kibe.utils.menu.BlockScreenHandlerFactory
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class TrashCan(settings: Settings): BlockWithEntity(settings) {

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return TrashCanEntity(blockPos, blockState)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hit: BlockHitResult): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos, ::TrashCanScreenHandler))
        return ActionResult.SUCCESS
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun hasSidedTransparency(state: BlockState?): Boolean {
        return true
    }


    override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape = SHAPE

    override fun getCollisionShape(state: BlockState, view: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape = SHAPE

    override fun getCodec(): MapCodec<TrashCan> = CODEC

    companion object {
        private val SHAPE = createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
        private val CODEC: MapCodec<TrashCan> = createCodec(::TrashCan)
    }

}