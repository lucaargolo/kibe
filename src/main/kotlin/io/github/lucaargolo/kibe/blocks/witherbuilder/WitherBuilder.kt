package io.github.lucaargolo.kibe.blocks.witherbuilder

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
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

class WitherBuilder: BlockWithEntity(FabricBlockSettings.copyOf(Blocks.OBSIDIAN)) {

    init {
        defaultState = stateManager.defaultState
            .with(Properties.HORIZONTAL_FACING, Direction.NORTH)
            .with(VERTICAL, false)
            .with(VERTICAL_FACING, Direction.UP)
            .with(Properties.TRIGGERED, false)
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.HORIZONTAL_FACING)
        stateManager.add(VERTICAL)
        stateManager.add(VERTICAL_FACING)
        stateManager.add(Properties.TRIGGERED)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return when(ctx.playerLookDirection.opposite) {
            Direction.UP -> defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing.opposite).with(VERTICAL, true).with(VERTICAL_FACING, Direction.UP)
            Direction.DOWN -> defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing.opposite).with(VERTICAL, true).with(VERTICAL_FACING, Direction.DOWN)
            else -> defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing.opposite).with(VERTICAL, false)
        }
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
        val facing = state[Properties.HORIZONTAL_FACING]
        val facingPos = pos.offset(if(state[VERTICAL]) state[VERTICAL_FACING] else facing, if(state[VERTICAL_FACING] == Direction.DOWN) 3 else 1)
        val xOffset = if(facing.axis == Direction.Axis.Z) 1 else 0
        val zOffset = if(facing.axis == Direction.Axis.X) 1 else 0

        val shouldRun = arrayOf(
            facingPos,
            facingPos.up(),
            facingPos.add(xOffset, 1, zOffset),
            facingPos.add(-xOffset, 1, -zOffset),
            facingPos.add(0, 2, 0),
            facingPos.add(xOffset, 2, zOffset),
            facingPos.add(-xOffset, 2, -zOffset)
        ).map { world.getBlockState(it).material.isReplaceable }.none { !it }

        if(shouldRun) {
            (world.getBlockEntity(pos) as? WitherBuilderBlockEntity)?.let { blockEntity ->
                val minCount = (blockEntity.inventory.map { if (it.isEmpty) 0 else it.count }.minOrNull() ?: 0)
                if(minCount > 0) {
                    val fakePlayer = FakePlayerEntity(world)

                    (0..6).forEach { slot ->
                        val stack = blockEntity.getStack(slot)
                        val item = stack.item as? BlockItem ?: return
                        fakePlayer.setStackInHand(Hand.MAIN_HAND, stack)
                        when(slot) {
                            0 -> item.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(Vec3d(facingPos.x + 0.5, facingPos.y + 0.0, facingPos.z + 0.5), facing.opposite, facingPos, false)))
                            1 -> item.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(Vec3d(facingPos.x + 0.5, facingPos.y + 1.0, facingPos.z + 0.5), Direction.UP, facingPos.up(), false)))
                            2 -> item.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(Vec3d(facingPos.x + 0.5 + xOffset, facingPos.y + 1.0, facingPos.z + 0.5 + zOffset), Direction.get(Direction.AxisDirection.POSITIVE, facing.axis), facingPos.add(xOffset, 1, zOffset), false)))
                            3 -> item.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(Vec3d(facingPos.x + 0.5 - xOffset, facingPos.y + 1.0, facingPos.z + 0.5 - zOffset), Direction.get(Direction.AxisDirection.NEGATIVE, facing.axis), facingPos.add(-xOffset, 1, -zOffset), false)))
                            4 -> item.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(Vec3d(facingPos.x + 0.5, facingPos.y + 2.0, facingPos.z + 0.5), Direction.UP, facingPos.add(0, 2, 0), false)))
                            5 -> item.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(Vec3d(facingPos.x + 0.5 + xOffset, facingPos.y + 2.0, facingPos.z + 0.5 - zOffset), Direction.UP, facingPos.add(xOffset, 1, zOffset), false)))
                            6 -> item.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(Vec3d(facingPos.x + 0.5 - xOffset, facingPos.y + 2.0, facingPos.z + 0.5 - zOffset), Direction.UP, facingPos.add(-xOffset, 1, -zOffset), false)))
                        }
                    }
                }
            }
        }
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = WitherBuilderBlockEntity(this, pos, state)

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

    companion object {
        val VERTICAL = BooleanProperty.of("vertical")
        val VERTICAL_FACING = DirectionProperty.of("vertical_facing", Direction.Type.VERTICAL)
    }

}