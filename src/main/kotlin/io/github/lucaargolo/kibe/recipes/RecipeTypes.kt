package io.github.lucaargolo.kibe.recipes

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipe
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

lateinit var VACUUM_HOPPER_RECIPE_TYPE: RecipeType<VacuumHopperRecipe>

@Suppress("SameParameterValue")
private fun <T : Recipe<*>> register(id: String): RecipeType<T> {
    return Registry.register(Registries.RECIPE_TYPE, "${KibeMod.MOD_ID}:$id", object : RecipeType<T> { override fun toString() = "${KibeMod.MOD_ID}:$id" })
}

fun initRecipeTypes() {
    VACUUM_HOPPER_RECIPE_TYPE = register("vacuum_hopper")
}