package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.state.property.Properties
import net.minecraft.util.Tickable

class RedstoneTimerEntity(private val timer: RedstoneTimer): BlockEntity(getEntityType(timer)), BlockEntityClientSerializable, Tickable {

    var current = 0
    var level = 0

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        current = tag.getInt("current")
        level = tag.getInt("level")
    }

    override fun fromClientTag(tag: CompoundTag) {
        current = tag.getInt("current")
        level = tag.getInt("level")
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putInt("current", current)
        tag.putInt("level", level)
        return super.toTag(tag)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.putInt("current", current)
        tag.putInt("level", level)
        return tag
    }

    override fun tick() {
        val isEnabled = world!!.getBlockState(pos)[Properties.ENABLED]
        val delay = level*4
        current++
        if(current >= delay){
            current = 0
            world!!.setBlockState(pos, timer.defaultState.with(Properties.ENABLED, !isEnabled))
            level = delay/4
        }

    }

}