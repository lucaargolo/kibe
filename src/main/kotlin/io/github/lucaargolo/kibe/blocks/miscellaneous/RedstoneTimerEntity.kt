package io.github.lucaargolo.kibe.blocks.miscellaneous

import net.minecraft.block.entity.BlockEntity
import net.minecraft.state.property.Properties
import net.minecraft.util.Tickable

class RedstoneTimerEntity(val timer: RedstoneTimer): BlockEntity(timer.entityType), Tickable {

    var currentDelay = 0;

    override fun tick() {
        val isEnabled = world!!.getBlockState(pos).get(Properties.ENABLED)
        val delay = world!!.getBlockState(pos).get(Properties.LEVEL_15)*4
        currentDelay++;
        if(currentDelay >= delay){
            currentDelay = 0;
            world!!.setBlockState(pos, timer.defaultState.with(Properties.ENABLED, !isEnabled).with(Properties.LEVEL_15, delay/4))
        }

    }

}