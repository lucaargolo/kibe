package io.github.lucaargolo.kibe.recipes.vacuum

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.lucaargolo.kibe.KibeMod
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer

class VacuumHopperRecipeSerializer : RecipeSerializer<VacuumHopperRecipe> {

    override fun codec(): MapCodec<VacuumHopperRecipe> = CODEC

    override fun packetCodec(): PacketCodec<RegistryByteBuf, VacuumHopperRecipe> = PACKET_CODEC

    companion object {
        private val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("ticks").forGetter(VacuumHopperRecipe::ticks),
                Codec.LONG.fieldOf("xp").forGetter(VacuumHopperRecipe::xp),
                Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input").forGetter(VacuumHopperRecipe::input),
                ItemStack.VALIDATED_CODEC.fieldOf("output").forGetter(VacuumHopperRecipe::output),
            ).apply(instance, ::VacuumHopperRecipe)
        }

        private val PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, VacuumHopperRecipe::ticks,
            KibeMod.LONG_CODEC, VacuumHopperRecipe::xp,
            Ingredient.PACKET_CODEC, VacuumHopperRecipe::input,
            ItemStack.PACKET_CODEC, VacuumHopperRecipe::output,
            ::VacuumHopperRecipe
        )
    }



}