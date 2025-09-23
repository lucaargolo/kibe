package io.github.lucaargolo.kibe.network

import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.BlockPos

data class ChunkMapClickPacket(val x: Int, val z: Int, val pos: BlockPos): CustomPayload {

    override fun getId() = ID

    companion object {
        val ID: CustomPayload.Id<ChunkMapClickPacket> = CustomPayload.Id<ChunkMapClickPacket>(ModIdentifier.of("chunk_map_click"))
        val PACKET_CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, ChunkMapClickPacket::x, PacketCodecs.INTEGER, ChunkMapClickPacket::z, BlockPos.PACKET_CODEC, ChunkMapClickPacket::pos, ::ChunkMapClickPacket)
    }

}