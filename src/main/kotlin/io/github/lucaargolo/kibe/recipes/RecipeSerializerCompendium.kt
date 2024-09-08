package io.github.lucaargolo.kibe.recipes

import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipeSerializer
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.registry.Registries
import net.minecraftforge.registries.ForgeRegistries

object RecipeSerializerCompendium: RegistryCompendium<RecipeSerializer<*>>(ForgeRegistries.RECIPE_SERIALIZERS) {

    val VACUUM_HOPPER by register("vacuum_hopper", { VacuumHopperRecipeSerializer() })

}