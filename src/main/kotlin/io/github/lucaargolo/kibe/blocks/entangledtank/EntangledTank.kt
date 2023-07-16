package io.github.lucaargolo.kibe.blocks.entangledtank

import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChest
import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.items.itemRegistry
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import io.github.lucaargolo.kibe.utils.interactPlayerHand
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class EntangledTank: BlockWithEntity(FabricBlockSettings.of(Material.STONE).requiresTool().strength(22.0F, 600.0F).luminance { state -> state[Properties.LEVEL_15] }) {

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.LEVEL_15)
        stateManager.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.LEVEL_15, 0).with(Properties.HORIZONTAL_FACING, ctx.playerFacing)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState? {
        return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state[Properties.HORIZONTAL_FACING]))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState? {
        return state.rotate(mirror.getRotation(state[Properties.HORIZONTAL_FACING]))
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return EntangledTankEntity(this, blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, blockState: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return checkType(blockEntityType, getEntityType(this)) { wrld, pos, state, blockEntity -> EntangledTankEntity.tick(wrld, pos, state, blockEntity as EntangledTankEntity) }
    }

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos): Int {
        return (world.getBlockEntity(pos) as? EntangledTankEntity)?.getComparatorOutput() ?: 0
    }

    @Suppress("DEPRECATION")
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

    @Suppress("DEPRECATION")
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
        var isHoldingRune = false
        itemRegistry.forEach { (_, modItem) -> if(modItem.item is Rune && context.isHolding(modItem.item)) isHoldingRune = true }

        if(isHoldingRune) return VoxelShapes.union(
            EntangledChest.getRunesShape(),
            createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0)
        )

        if(context.isHolding(Items.DIAMOND) || context.isHolding(Items.GOLD_INGOT)) return VoxelShapes.union(
            VoxelShapes.union(
                createCuboidShape(9.0, 14.0, 7.0, 10.0, 16.0, 9.0),
                createCuboidShape(7.0, 14.0, 6.0, 9.0, 16.0, 10.0)
            ),
            createCuboidShape(6.0, 14.0, 7.0, 7.0, 16.0, 9.0)
        )

        return createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val poss = player.raycast(4.5, 1.0F, false).pos

        return (world.getBlockEntity(pos) as? EntangledTankEntity)?.let { tank ->
            if ((poss.y - pos.y) > 0.9375) {
                if (player.getStackInHand(hand).item is Rune) {
                    val int = EntangledChest.getRuneByPos((poss.x - pos.x), (poss.z - pos.z), state[Properties.HORIZONTAL_FACING])
                    if (int != null) {
                        if (!world.isClient) {
                            val oldColor = tank.runeColors[int]
                            val newColor = (player.getStackInHand(hand).item as Rune).color
                            if(oldColor != newColor) {
                                tank.runeColors[int] = newColor
                                tank.updateColorCode()
                                if(!player.isCreative) {
                                    oldColor?.let(Rune::getRuneByColor)?.let {
                                        Block.dropStack(world, pos.up(), ItemStack(it))
                                    }
                                    player.getStackInHand(hand).decrement(1)
                                }
                            }
                        }
                        tank.markDirtyAndSync()
                        return ActionResult.CONSUME
                    }
                }
                if(player.getStackInHand(hand).item == Items.DIAMOND || player.getStackInHand(hand).item == Items.GOLD_INGOT) {
                    val x = poss.x - pos.x
                    val z = poss.z - pos.z
                    if ((x in 0.375..0.4375 && z in 0.4375..0.5625) || (x in 0.4375..0.5625 && z in 0.375..0.625) || (x in 0.5625..0.625 && z in 0.4375..0.5625)) {
                        if(player.getStackInHand(hand).item == Items.DIAMOND && tank.key == DEFAULT_KEY) {
                            if (!world.isClient) {
                                tank.owner = player.name.string
                                tank.key = "entangledtank-${player.uuid}"
                            }
                            tank.markDirtyAndSync()
                            if(!player.isCreative) {
                                Block.dropStack(world, pos.up(), ItemStack(Items.GOLD_INGOT))
                                player.getStackInHand(hand).decrement(1)
                            }
                            return ActionResult.CONSUME
                        }else if(player.getStackInHand(hand).item == Items.GOLD_INGOT && tank.key != DEFAULT_KEY) {
                            if (!world.isClient) {
                                tank.owner = ""
                                tank.key = DEFAULT_KEY
                            }
                            tank.markDirtyAndSync()
                            if(!player.isCreative) {
                                Block.dropStack(world, pos.up(), ItemStack(Items.DIAMOND))
                                player.getStackInHand(hand).decrement(1)
                            }
                            return ActionResult.CONSUME
                        }
                    }
                }
            }
            val tankInteraction = interactPlayerHand(tank.getTank(), player, hand)
            tank.markDirtyAndSync()
            return tankInteraction
        } ?: ActionResult.FAIL

    }

    companion object {
        const val DEFAULT_KEY = "entangledtank-global"
    }


}