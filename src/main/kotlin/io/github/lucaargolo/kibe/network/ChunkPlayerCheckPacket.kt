package io.github.lucaargolo.kibe.network

import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.BlockPos

data class ChunkPlayerCheckPacket(val pos: BlockPos): CustomPayload {

    override fun getId() = ID

    companion object {
        val ID: CustomPayload.Id<ChunkPlayerCheckPacket> = CustomPayload.Id<ChunkPlayerCheckPacket>(ModIdentifier.of("chunk_player_check"))
        val PACKET_CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, ChunkPlayerCheckPacket::pos, ::ChunkPlayerCheckPacket)
    }

}