package io.github.lucaargolo.kibe.blocks.breaker

import io.github.lucaargolo.kibe.utils.BlockScreenHandlerFactory
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class Breaker: BlockWithEntity(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)) {

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

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(DispenserBlock.FACING, rotation.rotate(state[DispenserBlock.FACING]))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.rotate(mirror.getRotation(state[DispenserBlock.FACING]))
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block?, fromPos: BlockPos?, notify: Boolean) {
        val isReceivingPower = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up())
        val triggered = state[DispenserBlock.TRIGGERED]
        if (isReceivingPower && !triggered) {
            world.scheduleBlockTick(pos, this, 4)
            world.setBlockState(pos, state.with(DispenserBlock.TRIGGERED, true) as BlockState, 4)
        } else if (!isReceivingPower && triggered) {
            world.setBlockState(pos, state.with(DispenserBlock.TRIGGERED, false) as BlockState, 4)
        }
    }
    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        val facing = state[Properties.FACING]
        val facingPos = pos.offset(facing)
        val facingState = world.getBlockState(facingPos)
        if(facingState.getHardness(world, facingPos) >= 0) {
            (world.getBlockEntity(pos) as? BreakerBlockEntity)?.let {
                val stackList = Block.getDroppedStacks(facingState, world, facingPos, world.getBlockEntity(facingPos))
                val stackIterator = stackList.iterator()
                while(stackIterator.hasNext()) {
                    val stack = stackIterator.next()
                    (0 until it.size()).forEach { slot ->
                        if (!stack.isEmpty) {
                            val stk = it.getStack(slot)
                            if (stk.isEmpty) {
                                it.setStack(slot, stack.copy())
                                stack.decrement(stack.count)
                            } else if (ItemStack.canCombine(stack, stk)) {
                                if (stk.count + stack.count < stk.maxCount) {
                                    stk.increment(stack.count)
                                    stack.decrement(stack.count)
                                } else {
                                    val value = stk.maxCount - stk.count
                                    stk.increment(value)
                                    stack.decrement(value)
                                }
                            }
                        }
                    }
                    if(!stack.isEmpty) {
                        ItemScatterer.spawn(world, pos.x+facing.offsetX+0.5, pos.y+facing.offsetY+0.5, pos.z+facing.offsetZ+0.5, stack)
                    }
                }
                world.breakBlock(facingPos, false)
                it.markDirty()
            }
        }
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = BreakerBlockEntity(this, pos, state)

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