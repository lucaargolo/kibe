package io.github.lucaargolo.kibe.recipes

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipe
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.Registries
import net.neoforged.neoforge.registries.DeferredHolder
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object RecipeTypeCompendium: RegistryCompendium<RecipeType<*>>(Registries.RECIPE_TYPE) {

    val VACUUM_HOPPER by register<VacuumHopperRecipe>("vacuum_hopper")

    private fun <T : Recipe<*>> register(id: String): Lazy<RecipeType<T>> {
        return register(id) {
            object : RecipeType<T> {
                override fun toString() = "${KibeMod.MOD_ID}:$id"
            }
        }
    }

}