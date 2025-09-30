package io.github.lucaargolo.kibe.network

import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload

data class EntangledChestAnimationStatePacket(val opened: Set<Pair<String, String>>): CustomPayload {

    override fun getId() = ID

    companion object {
        val ID: CustomPayload.Id<EntangledChestAnimationStatePacket> = CustomPayload.Id<EntangledChestAnimationStatePacket>(ModIdentifier.of("entangled_chest_animation_state"))

        val PAIR_CODEC = PacketCodec.tuple(PacketCodecs.STRING, Pair<String, String>::first, PacketCodecs.STRING, Pair<String, String>::second, ::Pair)
        val PACKET_CODEC = PacketCodec.tuple(PacketCodecs.collection({ i -> mutableSetOf<Pair<String, String>>() }, PAIR_CODEC), EntangledChestAnimationStatePacket::opened, ::EntangledChestAnimationStatePacket)
    }

}