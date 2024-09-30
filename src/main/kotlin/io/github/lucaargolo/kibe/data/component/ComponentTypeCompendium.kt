package io.github.lucaargolo.kibe.data.component

import com.mojang.serialization.Codec
import io.github.lucaargolo.kibe.item.MeasuringTape
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.component.ComponentType
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.Registries
import net.minecraft.util.DyeColor

object ComponentTypeCompendium: RegistryCompendium<ComponentType<*>>(Registries.DATA_COMPONENT_TYPE) {

    val ENABLED = register("enabled", ComponentType.Builder<Boolean>().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build())
    val UNIQUE = register("unique", ComponentType.Builder<Boolean>().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build())

    val ENTANGLED_KEY = register("entangled_key", ComponentType.Builder<String>().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build())
    val RUNE_SET = register("rune_set", ComponentType.Builder<List<DyeColor>>().codec(Codec.list(DyeColor.CODEC)).packetCodec(PacketCodecs.collection(::ArrayList, DyeColor.PACKET_CODEC)).build())
    val OWNER = register("owner", ComponentType.Builder<String>().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build())
    val COLOR_CODE = register("color_code", ComponentType.Builder<String>().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build())

    val MEASURING_FROM = register("measuring_from", ComponentType.Builder<MeasuringTape.MeasuringData>().codec(MeasuringTape.MeasuringData.CODEC).packetCodec(MeasuringTape.MeasuringData.PACKET_CODEC).build())
    val MEASURING_TO = register("measuring_to", ComponentType.Builder<MeasuringTape.MeasuringData>().codec(MeasuringTape.MeasuringData.CODEC).packetCodec(MeasuringTape.MeasuringData.PACKET_CODEC).build())

}