package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.state.property.Properties
import net.minecraft.util.Tickable

class RedstoneTimerEntity(val timer: RedstoneTimer): BlockEntity(getEntityType(timer)), Tickable {

    var current = 0;
    var level = 0

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        current = tag.getInt("current")
        level = tag.getInt("level")
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putInt("current", current)
        tag.putInt("level", level)
        return super.toTag(tag)
    }

    override fun tick() {
        val isEnabled = world!!.getBlockState(pos)[Properties.ENABLED]
        val delay = level*4
        current++;
        if(current >= delay){
            current = 0;
            world!!.setBlockState(pos, timer.defaultState.with(Properties.ENABLED, !isEnabled))
            level = delay/4
        }

    }

}