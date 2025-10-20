package io.github.lucaargolo.kibe.block

import com.mojang.serialization.MapCodec
import io.github.lucaargolo.kibe.blockentity.DrawbridgeBlockEntity
import io.github.lucaargolo.kibe.menu.DrawbridgeScreenHandler
import io.github.lucaargolo.kibe.utils.menu.BlockScreenHandlerFactory
import net.fabricmc.fabric.api.entity.FakePlayer
import net.minecraft.block.*
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.*
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class Drawbridge(settings: Settings): BlockWithEntity(settings) {

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
            world.scheduleBlockTick(pos, this, 8)
            world.setBlockState(pos, state.with(DispenserBlock.TRIGGERED, true), 4)
        } else if (!isReceivingPower && triggered) {
            world.scheduleBlockTick(pos, this, 8)
            world.setBlockState(pos, state.with(DispenserBlock.TRIGGERED, false), 4)
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        val facing = state[Properties.FACING]
        val triggered = state[DispenserBlock.TRIGGERED]
        (world.getBlockEntity(pos) as? DrawbridgeBlockEntity)?.let {
            if(triggered) {
                val stack = it.getStack(0)
                val front = world.getBlockState(pos.offset(facing))
                if(front.isAir || isSameBlock(stack, world, pos.offset(facing), front)) {
                    val distance = sameBlockDistance(stack, world, pos, facing)
                    if (!stack.isEmpty && distance < stack.maxCount) {
                        placeBlock(stack, world, pos.offset(facing, distance + 1), facing)
                        world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 1f, world.random.nextFloat() * 0.25f + 0.6f)
                        world.scheduleBlockTick(pos, this, 8)
                    }
                }
            }else{
                val stack = it.getStack(0).let { stack ->
                    if(stack.isEmpty) {
                        val blockStack = stackFromBlock(world, pos.offset(facing))
                        return@let blockStack
                    } else {
                        return@let stack
                    }
                }
                val distance = sameBlockDistance(stack, world, pos, facing)
                if(distance > 0 && !stack.isEmpty && stack.count < stack.maxCount) {
                    breakBlock(stack, world, pos.offset(facing, distance), it)
                    world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 1f, world.random.nextFloat() * 0.25f + 0.6f)
                    world.scheduleBlockTick(pos, this, 8)
                }
            }
        }
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = DrawbridgeBlockEntity(pos, state)

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos))
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? Inventory)?.let {
                ItemScatterer.spawn(world, pos, it)
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, notify)
        }
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos, ::DrawbridgeScreenHandler))
        return ActionResult.SUCCESS
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getCodec(): MapCodec<Placer> = CODEC

    companion object {
        private val CODEC: MapCodec<Placer> = createCodec(::Placer)

        private fun placeBlock(stack: ItemStack, world: ServerWorld, pos: BlockPos, facing: Direction) {
            val fakePlayer = FakePlayer.get(world)
            fakePlayer.setStackInHand(Hand.MAIN_HAND, stack)
            val fakeHitPos = Vec3d(pos.x + 0.5, pos.y + 0.0, pos.z + 0.5)
            (stack.item as? BlockItem)?.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(fakeHitPos, facing.opposite, pos, false)))
        }

        private fun breakBlock(stack: ItemStack, world: World, pos: BlockPos, blockEntity: DrawbridgeBlockEntity) {
            world.breakBlock(pos, false)
            if(stack == blockEntity.getStack(0)) {
                stack.increment(1)
            }else{
                blockEntity.setStack(0, stack.copy())
            }
        }

        private fun sameBlockDistance(stack: ItemStack, world: ServerWorld, pos: BlockPos, facing: Direction): Int {
            var distance = 0
            while(distance < stack.maxCount && isSameBlock(stack, world, pos.offset(facing, distance+1))) {
                distance++
            }
            return distance
        }

        private fun isSameBlock(stack: ItemStack, world: ServerWorld, pos: BlockPos): Boolean {
            return ItemStack.areItemsAndComponentsEqual(stack, stackFromBlock(world, pos))
        }

        private fun isSameBlock(stack: ItemStack, world: ServerWorld, pos: BlockPos, state: BlockState): Boolean {
            return ItemStack.areItemsAndComponentsEqual(stack, stackFromBlock(world, pos, state))
        }

        private fun stackFromBlock(world: ServerWorld, pos: BlockPos): ItemStack {
            val state = world.getBlockState(pos)
            return stackFromBlock(world, pos, state)
        }

        private fun stackFromBlock(world: ServerWorld, pos: BlockPos, state: BlockState): ItemStack {
            val entity = world.getBlockEntity(pos)
            val list = getDroppedStacks(state, world, pos, entity, null, silkTouchStack(world, state))
            if(list.size == 1) {
                val stack = list.first()
                if(stack.count == 1 && stack.item is BlockItem) {
                    return stack
                }
            }
            return ItemStack.EMPTY
        }

        private fun silkTouchStack(world: World, state: BlockState): ItemStack {
            val item = Items.STICK
            state.streamTags().forEach {
                when(it) {
                    BlockTags.SHOVEL_MINEABLE -> Items.NETHERITE_SHOVEL
                    BlockTags.AXE_MINEABLE -> Items.NETHERITE_AXE
                    BlockTags.PICKAXE_MINEABLE -> Items.NETHERITE_PICKAXE
                    BlockTags.HOE_MINEABLE -> Items.NETHERITE_HOE
                }
            }
            return item.defaultStack.also {
                it.addEnchantment(world.registryManager.get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SILK_TOUCH).get(), 1)
            }
        }

    }

}