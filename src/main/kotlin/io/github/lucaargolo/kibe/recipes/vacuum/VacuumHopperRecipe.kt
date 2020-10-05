package io.github.lucaargolo.kibe.recipes.vacuum

import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_SERIALIZER
import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_TYPE
import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeFinder
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class VacuumHopperRecipe(private val id: Identifier, private val output: ItemStack, private val input: DefaultedList<Ingredient>, val xpInput: Int) : Recipe<CraftingInventory> {

    override fun getId() = id

    override fun craft(inv: CraftingInventory): ItemStack = output.copy()

    override fun getType() = VACUUM_HOPPER_RECIPE_TYPE

    override fun fits(width: Int, height: Int) = true

    override fun getSerializer() = VACUUM_HOPPER_RECIPE_SERIALIZER

    override fun getOutput(): ItemStack = output

    override fun getRecipeKindIcon(): ItemStack = Items.EXPERIENCE_BOTTLE.defaultStack

    override fun getPreviewInputs() = input

    override fun matches(inv: CraftingInventory, world: World): Boolean {
        val recipeFinder = RecipeFinder()
        var i = 0

        for (j in 0 until inv.size()) {
            val itemStack: ItemStack = inv.getStack(j)
            if (!itemStack.isEmpty) {
                ++i
                recipeFinder.method_20478(itemStack, 1)
            }
        }

        return i == input.size && recipeFinder.findRecipe(this, null as IntList?)
    }
}