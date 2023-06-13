package io.github.lucaargolo.kibe.recipes

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipe
import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipeSerializer
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.registry.Registry
import net.minecraft.registry.Registries

lateinit var VACUUM_HOPPER_RECIPE_SERIALIZER: RecipeSerializer<VacuumHopperRecipe>

@Suppress("SameParameterValue")
private fun <S: RecipeSerializer<T>, T: Recipe<*>>register(id: String, serializer: S): S {
    return Registry.register(Registries.RECIPE_SERIALIZER, "$MOD_ID:$id", serializer)
}

fun initRecipeSerializers() {
    VACUUM_HOPPER_RECIPE_SERIALIZER = register("vacuum_hopper", VacuumHopperRecipeSerializer())
}