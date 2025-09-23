package io.github.lucaargolo.kibe.network

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload

data class SynchronizeDirtyTankStatesPacket(val map: Map<String, Map<String, Pair<FluidVariant, Long>>>): CustomPayload {

    override fun getId() = ID

    companion object {
        val ID: CustomPayload.Id<SynchronizeDirtyTankStatesPacket> = CustomPayload.Id<SynchronizeDirtyTankStatesPacket>(ModIdentifier.of("request_dirty_tank_states"))

        val PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(
                ::LinkedHashMap,
                PacketCodecs.STRING,
                PacketCodecs.map(
                    ::LinkedHashMap,
                    PacketCodecs.STRING,
                    PacketCodec.tuple(FluidVariant.PACKET_CODEC, Pair<FluidVariant, Long>::first, KibeMod.LONG_CODEC, Pair<FluidVariant, Long>::second, ::Pair)
                )
            ),
            SynchronizeDirtyTankStatesPacket::map,
            ::SynchronizeDirtyTankStatesPacket
        )
    }

}