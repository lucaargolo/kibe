package io.github.lucaargolo.kibe.data.component

import com.mojang.serialization.Codec
import io.github.lucaargolo.kibe.item.MeasuringTape
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.component.ComponentType
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.Registries
import net.minecraft.util.DyeColor
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object ComponentTypeCompendium: RegistryCompendium<ComponentType<*>>(Registries.DATA_COMPONENT_TYPE) {

    val ENABLED by register("enabled", { ComponentType.Builder<Boolean>().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build() })
    val UNIQUE by register("unique", { ComponentType.Builder<Boolean>().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build() })

    val ENTANGLED_KEY by register("entangled_key", { ComponentType.Builder<String>().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build() })
    val RUNE_SET by register("rune_set", { ComponentType.Builder<List<DyeColor>>().codec(Codec.list(DyeColor.CODEC)).packetCodec(PacketCodecs.collection(::ArrayList, DyeColor.PACKET_CODEC)).build() })
    val OWNER by register("owner", { ComponentType.Builder<String>().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build() })
    val COLOR_CODE by register("color_code", { ComponentType.Builder<String>().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build() })

    val MEASURING_FROM by register("measuring_from", { ComponentType.Builder<MeasuringTape.MeasuringData>().codec(MeasuringTape.MeasuringData.CODEC).packetCodec(MeasuringTape.MeasuringData.PACKET_CODEC).build() })
    val MEASURING_TO by register("measuring_to", { ComponentType.Builder<MeasuringTape.MeasuringData>().codec(MeasuringTape.MeasuringData.CODEC).packetCodec(MeasuringTape.MeasuringData.PACKET_CODEC).build() })

}