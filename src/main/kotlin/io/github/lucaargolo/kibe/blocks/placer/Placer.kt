package io.github.lucaargolo.kibe.blocks.placer

import io.github.lucaargolo.kibe.utils.BlockScreenHandlerFactory
import io.github.lucaargolo.kibe.utils.FakePlayerEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*

class Placer: BlockWithEntity(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)) {

    init {
        defaultState = stateManager.defaultState.with(Properties.FACING, Direction.NORTH).with(Properties.TRIGGERED, false)
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.FACING)
        stateManager.add(Properties.TRIGGERED)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.FACING, ctx.playerLookDirection.opposite)
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block?, fromPos: BlockPos?, notify: Boolean) {
        val isReceivingPower = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up())
        val triggered = state[DispenserBlock.TRIGGERED]
        if (isReceivingPower && !triggered) {
            world.blockTickScheduler.schedule(pos, this, 4)
            world.setBlockState(pos, state.with(DispenserBlock.TRIGGERED, true) as BlockState, 4)
        } else if (!isReceivingPower && triggered) {
            world.setBlockState(pos, state.with(DispenserBlock.TRIGGERED, false) as BlockState, 4)
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        val facing = state[Properties.FACING]
        val facingPos = pos.offset(facing)
        (world.getBlockEntity(pos) as? PlacerBlockEntity)?.let {
            var index = 0
            while(index < 9 && it.getStack(index).isEmpty) {
                index++
            }
            if(index < 9) {
                val stack = it.getStack(index)
                val item = stack.item as? BlockItem ?: return
                val fakePlayer = FakePlayerEntity(world)
                fakePlayer.setStackInHand(Hand.MAIN_HAND, stack)
                val fakeHitPos = Vec3d(facingPos.x + 0.5, facingPos.y + 0.0, facingPos.z + 0.5)
                item.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(fakeHitPos, facing.opposite, facingPos, false)))
            }
        }
    }

    override fun createBlockEntity(world: BlockView?) = PlacerBlockEntity(this)

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos))
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? Inventory)?.let {
                ItemScatterer.spawn(world, pos, it)
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, notify)
        }
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand?, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos))
        return ActionResult.SUCCESS
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}