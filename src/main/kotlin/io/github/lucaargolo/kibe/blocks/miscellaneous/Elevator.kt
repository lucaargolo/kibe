package io.github.lucaargolo.kibe.blocks.miscellaneous

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class Elevator: Block(FabricBlockSettings.of(Material.WOOL)) {

    companion object {
        fun isElevatorValid(world: World, pos: BlockPos): Boolean {
            return (world.getBlockState(pos.up()).isAir && world.getBlockState(pos.up().up()).isAir)
        }
    }

}