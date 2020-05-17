package io.github.lucaargolo.kibe.recipes.vacuum

import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_TYPE
import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_SERIALIZER
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.util.DefaultedList
import net.minecraft.util.Identifier
import net.minecraft.world.World

class VacuumHopperRecipe(private val id: Identifier, private val inputs: DefaultedList<Ingredient>, private val output: ItemStack) : Recipe<Inventory> {

    override fun getId() = id

    override fun craft(inv: Inventory?): ItemStack = output.copy()

    override fun getType() = VACUUM_HOPPER_RECIPE_TYPE

    override fun fits(width: Int, height: Int) = true

    override fun getSerializer() = VACUUM_HOPPER_RECIPE_SERIALIZER

    override fun getOutput(): ItemStack = output

    override fun getRecipeKindIcon(): ItemStack = Items.EXPERIENCE_BOTTLE.stackForRender

    override fun getPreviewInputs() = inputs

    override fun matches(inv: Inventory, world: World): Boolean {
        return inv.invSize == 1 && inputs.withIndex().all {
            it.value.test(inv.getInvStack(it.index))
        }
    }
}