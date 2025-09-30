package io.github.lucaargolo.kibe.block

import com.mojang.serialization.MapCodec
import io.github.lucaargolo.kibe.blockentity.BlockEntityCompendium
import io.github.lucaargolo.kibe.blockentity.EntangledTankEntity
import io.github.lucaargolo.kibe.mixin.DyeItemAccessor
import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.DyeItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class EntangledTank(settings: Settings): BlockWithEntity(settings) {

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.LEVEL_15)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.LEVEL_15, 0)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return EntangledTankEntity(blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, blockState: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return validateTicker(blockEntityType, BlockEntityCompendium.ENTANGLED_TANK, EntangledTankEntity::tick)
    }

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos): Int {
        return (world.getBlockEntity(pos) as? EntangledTankEntity)?.getComparatorOutput() ?: 0
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, fromPos: BlockPos, notify: Boolean) {
        (world.getBlockEntity(pos) as? EntangledTankEntity)?.let comparatorCheck@{
            if(it.isBeingCompared) {
                world.getBlockState(pos.north()).let { state ->
                    if(state.isOf(Blocks.COMPARATOR) && state[Properties.HORIZONTAL_FACING] == Direction.SOUTH) return@comparatorCheck
                }
                world.getBlockState(pos.south()).let { state ->
                    if(state.isOf(Blocks.COMPARATOR) && state[Properties.HORIZONTAL_FACING] == Direction.NORTH) return@comparatorCheck
                }
                world.getBlockState(pos.east()).let { state ->
                    if(state.isOf(Blocks.COMPARATOR) && state[Properties.HORIZONTAL_FACING] == Direction.WEST) return@comparatorCheck
                }
                world.getBlockState(pos.west()).let { state ->
                    if(state.isOf(Blocks.COMPARATOR) && state[Properties.HORIZONTAL_FACING] == Direction.EAST) return@comparatorCheck
                }
                it.isBeingCompared = false
            }
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify)
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? EntangledTankEntity)?.let {
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    override fun getCollisionShape(state: BlockState, view: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0)
    }

    override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos?, context: ShapeContext): VoxelShape {
        val isHoldingDye = DyeItemAccessor.getDyes().values.any(context::isHolding)
        if(isHoldingDye) return VoxelShapes.union(EntangledChest.getRunesShape(), createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0))
        if(context.isHolding(Items.DIAMOND) || context.isHolding(Items.GOLD_INGOT)) return VoxelShapes.union(
            VoxelShapes.union(
                createCuboidShape(9.0, 14.0, 7.0, 10.0, 16.0, 9.0),
                createCuboidShape(7.0, 14.0, 6.0, 9.0, 16.0, 10.0)
            ),
            createCuboidShape(6.0, 14.0, 7.0, 7.0, 16.0, 9.0)
        )

        return createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hit: BlockHitResult): ActionResult {
        val hand = Hand.MAIN_HAND
        val poss = player.raycast(4.5, 1.0F, false).pos
        val stack = player.getStackInHand(hand)
        return (world.getBlockEntity(pos) as? EntangledTankEntity)?.let { tank ->
            if ((poss.y - pos.y) > 0.9375) {
                if (stack.item is DyeItem) {
                    val int = EntangledChest.getRuneByPos((poss.x - pos.x), (poss.z - pos.z))
                    if (int != null) {
                        if (!world.isClient) {
                            val oldColor = tank.runeColors[int]
                            val newColor = (stack.item as DyeItem).color
                            if(oldColor != newColor) {
                                tank.runeColors[int] = newColor
                                if(!player.isCreative) {
                                    stack.decrement(1)
                                }
                            }
                        }
                        tank.markDirtyAndSync()
                        return ActionResult.CONSUME
                    }
                }
                if(stack.item == Items.DIAMOND || stack.item == Items.GOLD_INGOT) {
                    val x = poss.x - pos.x
                    val z = poss.z - pos.z
                    if ((x in 0.375..0.4375 && z in 0.4375..0.5625) || (x in 0.4375..0.5625 && z in 0.375..0.625) || (x in 0.5625..0.625 && z in 0.4375..0.5625)) {
                        if(stack.item == Items.DIAMOND && tank.key == DEFAULT_KEY) {
                            if (!world.isClient) {
                                tank.owner = player.name.string
                                tank.key = "entangledtank-${player.uuid}"
                            }
                            tank.markDirtyAndSync()
                            if(!player.isCreative) {
                                Block.dropStack(world, pos.up(), ItemStack(Items.GOLD_INGOT))
                                stack.decrement(1)
                            }
                            return ActionResult.CONSUME
                        }else if(stack.item == Items.GOLD_INGOT && tank.key != DEFAULT_KEY) {
                            if (!world.isClient) {
                                tank.owner = ""
                                tank.key = DEFAULT_KEY
                            }
                            tank.markDirtyAndSync()
                            if(!player.isCreative) {
                                Block.dropStack(world, pos.up(), ItemStack(Items.DIAMOND))
                                stack.decrement(1)
                            }
                            return ActionResult.CONSUME
                        }
                    }
                }
            }
            val tankInteraction = FluidHelper.interactPlayerHand(tank.getTank(), player, hand)
            tank.markDirtyAndSync()
            return tankInteraction
        } ?: ActionResult.FAIL

    }

    override fun getCodec(): MapCodec<EntangledTank> = CODEC

    companion object {
        const val DEFAULT_KEY = "entangledtank-global"
        private val CODEC: MapCodec<EntangledTank> = createCodec(::EntangledTank)
    }


}