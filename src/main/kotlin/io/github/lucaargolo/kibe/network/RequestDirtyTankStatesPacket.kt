package io.github.lucaargolo.kibe.network

import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload

data class RequestDirtyTankStatesPacket(val set: LinkedHashSet<Pair<String, String>>): CustomPayload {

    override fun getId() = ID

    companion object {
        val ID: CustomPayload.Id<RequestDirtyTankStatesPacket> = CustomPayload.Id<RequestDirtyTankStatesPacket>(ModIdentifier.of("request_dirty_tank_states"))
        val PACKET_CODEC = PacketCodec.tuple(PacketCodecs.collection(::LinkedHashSet, PacketCodec.tuple(PacketCodecs.STRING, Pair<String, String>::first, PacketCodecs.STRING, Pair<String, String>::second, ::Pair)), RequestDirtyTankStatesPacket::set, ::RequestDirtyTankStatesPacket)
    }

}