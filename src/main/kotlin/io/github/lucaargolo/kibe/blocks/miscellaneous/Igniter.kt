package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.utils.FakePlayerEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.DispenserBlock
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class Igniter: Block(FabricBlockSettings.copyOf(Blocks.COBBLESTONE)) {

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
            world.createAndScheduleBlockTick(pos, this, 4)
            world.setBlockState(pos, state.with(DispenserBlock.TRIGGERED, true) as BlockState, 4)
        } else if (!isReceivingPower && triggered) {
            world.setBlockState(pos, state.with(DispenserBlock.TRIGGERED, false) as BlockState, 4)
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        val facing = state[Properties.FACING]
        val fakePlayer = FakePlayerEntity(world)
        fakePlayer.setStackInHand(Hand.MAIN_HAND, Items.FLINT_AND_STEEL.defaultStack)
        var facingPos = pos.offset(facing)
        val facingState = world.getBlockState(facingPos)
        if(!facingState.isAir) {
            val fakeHitPos = Vec3d(facingPos.x + 0.5, facingPos.y + 0.5, facingPos.z + 0.5)
            Items.FLINT_AND_STEEL.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(fakeHitPos, facing.opposite, facingPos, false)))
        }else{
            facingPos = facingPos.offset(facing)
            val fakeHitPos = Vec3d(facingPos.x + 0.5, facingPos.y + 0.5, facingPos.z + 0.5)
            Items.FLINT_AND_STEEL.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(fakeHitPos, facing.opposite, facingPos, false)))
        }
    }

}