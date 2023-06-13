package io.github.lucaargolo.kibe.utils

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

abstract class SyncableBlockEntity(type: BlockEntityType<BlockEntity>, pos: BlockPos, state: BlockState): BlockEntity(type, pos, state) {

    abstract fun writeClientNbt(tag: NbtCompound): NbtCompound

    abstract fun readClientNbt(tag: NbtCompound)

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this) { writeClientNbt(NbtCompound()) }
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return writeClientNbt(NbtCompound())
    }

    fun sync() {
        ((this as? BlockEntity)?.world as? ServerWorld)?.chunkManager?.markForUpdate(this.pos)
    }

}