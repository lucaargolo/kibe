package io.github.lucaargolo.kibe.recipes

import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipeSerializer
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.registry.Registries

object RecipeSerializerCompendium: RegistryCompendium<RecipeSerializer<*>>(Registries.RECIPE_SERIALIZER) {

    val VACUUM_HOPPER by register("vacuum_hopper") { VacuumHopperRecipeSerializer() }

}