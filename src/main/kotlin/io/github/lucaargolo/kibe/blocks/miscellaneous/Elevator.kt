package io.github.lucaargolo.kibe.blocks.miscellaneous

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.MaterialColor
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class Elevator: Block(FabricBlockSettings.of(Material.STONE, MaterialColor.STONE).requiresTool().strength(1.5F, 6.0F)) {

    companion object {
        fun isElevatorValid(world: World, pos: BlockPos): Boolean {
            return (world.getBlockState(pos.up()).getCollisionShape(world, pos.up()).isEmpty && world.getBlockState(pos.up().up()).getCollisionShape(world, pos.up().up()).isEmpty)
        }
    }

}