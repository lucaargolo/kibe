package io.github.lucaargolo.kibe.recipes

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipe
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.Registry
import net.minecraft.registry.Registries

lateinit var VACUUM_HOPPER_RECIPE_TYPE: RecipeType<VacuumHopperRecipe>

@Suppress("SameParameterValue")
private fun <T : Recipe<*>> register(id: String): RecipeType<T> {
    return Registry.register(Registries.RECIPE_TYPE, "$MOD_ID:$id", object : RecipeType<T> { override fun toString() = "$MOD_ID:$id" })
}

fun initRecipeTypes() {
    VACUUM_HOPPER_RECIPE_TYPE = register("vacuum_hopper")
}