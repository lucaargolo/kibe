package io.github.lucaargolo.kibe.block

import com.mojang.serialization.MapCodec
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class Elevator(settings: Settings): Block(settings) {

    override fun getCodec(): MapCodec<Elevator> = CODEC

    companion object {
        private val CODEC = createCodec(::Elevator)

        fun isElevatorValid(world: World, pos: BlockPos): Boolean {
            return (world.getBlockState(pos.up()).getCollisionShape(world, pos.up()).isEmpty && world.getBlockState(pos.up().up()).getCollisionShape(world, pos.up().up()).isEmpty)
        }
    }

}