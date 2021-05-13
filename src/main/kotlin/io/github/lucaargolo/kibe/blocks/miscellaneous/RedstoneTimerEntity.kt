package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class RedstoneTimerEntity(private val timer: RedstoneTimer, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(timer), pos, state), BlockEntityClientSerializable {

    var current = 0
    var level = 0

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        current = tag.getInt("current")
        level = tag.getInt("level")
    }

    override fun fromClientTag(tag: NbtCompound) {
        current = tag.getInt("current")
        level = tag.getInt("level")
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putInt("current", current)
        tag.putInt("level", level)
        return super.writeNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        tag.putInt("current", current)
        tag.putInt("level", level)
        return tag
    }

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: RedstoneTimerEntity) {
            val isEnabled = world.getBlockState(pos)[Properties.ENABLED]
            val delay = entity.level*4
            entity.current++
            if(entity.current >= delay){
                entity.current = 0
                world.setBlockState(pos, state.with(Properties.ENABLED, !isEnabled))
                entity.level = delay/4
            }
        }
    }

}