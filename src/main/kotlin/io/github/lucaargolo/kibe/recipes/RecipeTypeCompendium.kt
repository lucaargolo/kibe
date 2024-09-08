package io.github.lucaargolo.kibe.recipes

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipe
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.Registries
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.ObjectHolderDelegate

object RecipeTypeCompendium: RegistryCompendium<RecipeType<*>>(ForgeRegistries.RECIPE_TYPES) {

    val VACUUM_HOPPER by register<VacuumHopperRecipe>("vacuum_hopper")

    private fun <T : Recipe<*>> register(id: String): ObjectHolderDelegate<RecipeType<T>> {
        return register(id, { object : RecipeType<T> { override fun toString() = "${KibeMod.MOD_ID}:$id" } })
    }

}