package io.github.lucaargolo.kibe.blocks.entangledtank

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState

class EntangledTankState(val world: ServerWorld?, val key: String): PersistentState() {

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

    override fun writeNbt(tag: CompoundTag): CompoundTag {
        fluidInvMap.forEach { (colorCode, fluidInv) ->
            tag.put(colorCode, fluidInv.toTag())
        }
        return tag
    }

    companion object {

        val SERVER_STATES = mutableMapOf<String, EntangledTankState>()
        val CLIENT_STATES = mutableMapOf<String, EntangledTankState>()
        val SERVER_PLAYER_REQUESTS = mutableMapOf<ServerPlayerEntity, LinkedHashSet<Pair<String, String>>>()
        val CLIENT_PLAYER_REQUESTS = mutableMapOf<ClientPlayerEntity, LinkedHashSet<Pair<String, String>>>()

        fun getOrCreateClientState(key: String): EntangledTankState {
            CLIENT_STATES[key]?.let {
                return it
            }
            EntangledTankState(null, key).let {
                CLIENT_STATES[key] = it
                return it
            }
        }

        fun createFromTag(tag: CompoundTag, world: ServerWorld, key: String): EntangledTankState {
            val state = EntangledTankState(world, key)
            tag.keys.forEach {
                val tempFluidInv = SimpleFixedFluidInv(1, FluidAmount.ofWhole(16))
                tempFluidInv.fromTag(tag.getCompound(it))
                state.fluidInvMap[it] = tempFluidInv
            }
            return state
        }


    }

}