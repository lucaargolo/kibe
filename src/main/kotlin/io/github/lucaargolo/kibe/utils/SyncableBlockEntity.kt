package io.github.lucaargolo.kibe.utils

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

abstract class SyncableBlockEntity(type: BlockEntityType<out BlockEntity>, pos: BlockPos, state: BlockState): BlockEntity(type, pos, state) {

    abstract fun writeClientNbt(tag: NbtCompound, registryLookup: WrapperLookup): NbtCompound

    abstract fun readClientNbt(tag: NbtCompound, registryLookup: WrapperLookup)

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this) { _, registryLookup ->
            writeClientNbt(NbtCompound(), registryLookup)
        }
    }

    override fun toInitialChunkDataNbt(registryLookup: WrapperLookup): NbtCompound {
        return writeClientNbt(NbtCompound(), registryLookup)
    }

    fun sync() {
        ((this as? BlockEntity)?.world as? ServerWorld)?.chunkManager?.markForUpdate(this.pos)
    }

}