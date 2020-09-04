package io.github.lucaargolo.kibe.blocks.entangledtank

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import alexiil.mc.lib.attributes.fluid.FixedFluidInv
import alexiil.mc.lib.attributes.fluid.FluidInvUtil
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChest
import io.github.lucaargolo.kibe.blocks.tank.TankBlockEntity
import io.github.lucaargolo.kibe.items.itemRegistry
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.Items
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class EntangledTank: BlockWithEntity(FabricBlockSettings.of(Material.STONE).requiresTool().strength(22.0F, 600.0F)), AttributeProvider {

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun createBlockEntity(world: BlockView?) = EntangledTankEntity(this)

    override fun addAllAttributes(world: World, pos: BlockPos?, state: BlockState?, to: AttributeList<*>) {
        (world.getBlockEntity(pos) as? EntangledTankEntity)?.let {
            to.offer(it.fluidInv)
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

        if(context.isHolding(Items.DIAMOND)) return VoxelShapes.union(
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
                            tank.runeColors[int] = (player.getStackInHand(hand).item as Rune).color
                        }
                        tank.markDirtyAndSync()
                        return ActionResult.CONSUME
                    }
                }
                if (player.getStackInHand(hand).item == Items.DIAMOND) {
                    val x = poss.x - pos.x
                    val z = poss.z - pos.z
                    if ((x in 0.375..0.4375 && z in 0.4375..0.5625) || (x in 0.4375..0.5625 && z in 0.375..0.625) || (x in 0.5625..0.625 && z in 0.4375..0.5625)) {
                        if (tank.key == DEFAULT_KEY) {
                            if (!world.isClient) {
                                tank.owner = player.name.asString()
                                tank.key = "entangledtank-${player.uuid}"
                            }
                            tank.markDirtyAndSync()
                            return ActionResult.CONSUME
                        }
                    }
                }
            }
            val tankInteraction = FluidInvUtil.interactHandWithTank(tank.fluidInv as FixedFluidInv, player, hand)
            tank.markDirtyAndSync()
            tankInteraction.asActionResult()
        } ?: ActionResult.FAIL

    }

    companion object {
        const val DEFAULT_KEY = "entangledtank-global"
    }


}