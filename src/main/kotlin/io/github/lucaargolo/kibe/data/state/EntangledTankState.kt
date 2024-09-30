package io.github.lucaargolo.kibe.data.state

import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState

class EntangledTankState(val world: ServerWorld?, val key: String): PersistentState() {

    var dirtyColors = mutableListOf<String>()
    var fluidInvMap = mutableMapOf<String, SingleVariantStorage<FluidVariant>>()

    init {
        SERVER_STATES[key] = this
    }

    fun markDirty(colorCode: String) {
        dirtyColors.add(colorCode)
        super.markDirty()
    }

    fun getOrCreateInventory(colorCode: String): SingleVariantStorage<FluidVariant> {
        return fluidInvMap.computeIfAbsent(colorCode) {
            object : SingleVariantStorage<FluidVariant>() {
                override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
                override fun getCapacity(variant: FluidVariant?): Long = FluidConstants.BUCKET * 16

                override fun onFinalCommit() {
                    markDirty(colorCode)
                }
            }
        }
    }

    override fun writeNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup): NbtCompound {
        fluidInvMap.forEach { (colorCode, tank) ->
            tag.put(colorCode, FluidHelper.writeTank(NbtCompound(), tank))
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

        fun getPersistentState(world: ServerWorld, key: String): EntangledTankState {
            return world.server.overworld.persistentStateManager.getOrCreate(getPersistentStateType(world, key), key)
        }

        fun getPersistentStateType(world: ServerWorld?, key: String): Type<EntangledTankState> {
            return Type(
                { EntangledTankState(world, key) },
                { tag, _ ->
                    val state = EntangledTankState(world, key)
                    tag.keys.forEach {
                        FluidHelper.readTank(tag.getCompound(it), state.getOrCreateInventory(it))
                    }
                    return@Type state
                },
                null
            )
        }


    }

}