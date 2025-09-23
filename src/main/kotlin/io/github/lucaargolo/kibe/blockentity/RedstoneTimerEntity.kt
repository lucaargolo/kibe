package io.github.lucaargolo.kibe.blockentity

import io.github.lucaargolo.kibe.utils.SyncableBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class RedstoneTimerEntity(pos: BlockPos, state: BlockState): SyncableBlockEntity(BlockEntityCompendium.REDSTONE_TIMER, pos, state) {

    var current = 0
    var level = 0

    override fun readNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.readNbt(tag, registryLookup)
        current = tag.getInt("current")
        level = tag.getInt("level")
    }

    override fun readClientNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        current = tag.getInt("current")
        level = tag.getInt("level")
    }

    override fun writeNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        tag.putInt("current", current)
        tag.putInt("level", level)
    }

    override fun writeClientNbt(tag: NbtCompound, registryLookup: WrapperLookup): NbtCompound {
        tag.putInt("current", current)
        tag.putInt("level", level)
        return tag
    }

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: RedstoneTimerEntity) {
            val isEnabled = state[Properties.ENABLED]
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