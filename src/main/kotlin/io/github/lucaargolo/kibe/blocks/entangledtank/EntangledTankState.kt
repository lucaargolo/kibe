package io.github.lucaargolo.kibe.blocks.entangledtank

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState

class EntangledTankState(val world: ServerWorld?, val key: String): PersistentState(key) {

    var dirtyColors = mutableListOf<String>()
    var fluidInvMap = mutableMapOf<String, SimpleFixedFluidInv>()

    init {
        SERVER_STATES[key] = this
    }

    private fun createInventory(colorCode: String): SimpleFixedFluidInv {
        val fluidInv = SimpleFixedFluidInv(1, FluidAmount.ofWhole(16))
        fluidInvMap[colorCode] = fluidInv
        return fluidInv
    }

    fun markDirty(colorCode: String) {
        dirtyColors.add(colorCode)
        super.markDirty()
    }

    fun getOrCreateInventory(colorCode: String): SimpleFixedFluidInv {
        return fluidInvMap[colorCode] ?: createInventory(colorCode)
    }

    override fun fromTag(tag: CompoundTag) {
        tag.keys.forEach {
            val tempFluidInv = SimpleFixedFluidInv(1, FluidAmount.ofWhole(16))
            tempFluidInv.fromTag(tag.getCompound(it))
            fluidInvMap[it] = tempFluidInv
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        fluidInvMap.forEach { (colorCode, fluidInv) ->
            tag.put(colorCode, fluidInv.toTag())
        }
        return tag
    }

    companion object {

        val SERVER_STATES = linkedMapOf<String, EntangledTankState>()
        val CLIENT_STATES = linkedMapOf<String, EntangledTankState>()

        val ALL_TIME_PLAYER_REQUESTS = linkedMapOf<ServerPlayerEntity, LinkedHashSet<Pair<String, String>>>()
        val SERVER_PLAYER_REQUESTS = linkedMapOf<ServerPlayerEntity, LinkedHashSet<Pair<String, String>>>()

        var PAST_CLIENT_PLAYER_REQUESTS = linkedSetOf<Pair<String, String>>()
        var CURRENT_CLIENT_PLAYER_REQUESTS = linkedSetOf<Pair<String, String>>()

        fun getOrCreateClientState(key: String): EntangledTankState {
            CLIENT_STATES[key]?.let {
                return it
            }
            EntangledTankState(null, key).let {
                CLIENT_STATES[key] = it
                return it
            }
        }


    }

}