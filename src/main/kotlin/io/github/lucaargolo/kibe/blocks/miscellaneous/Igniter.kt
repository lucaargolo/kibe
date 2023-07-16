package io.github.lucaargolo.kibe.blocks.miscellaneous

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.DispenserBlock
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
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
        val facingPos = pos.offset(state[Properties.FACING])
        val facingState = world.getBlockState(facingPos)
        if(facingState.material.isReplaceable && !facingState.isOf(Blocks.FIRE)) {
            world.setBlockState(facingPos, Blocks.FIRE.defaultState)
            world.playSound(null, pos.x+0.0, pos.y+0.0, pos.z+0.0, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.4f + 0.8f,)
        }
    }

}