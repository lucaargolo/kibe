package io.github.lucaargolo.kibe.recipes.vacuum

import io.github.lucaargolo.kibe.blockentity.VacuumHopperEntity
import io.github.lucaargolo.kibe.recipes.RecipeSerializerCompendium
import io.github.lucaargolo.kibe.recipes.RecipeTypeCompendium
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class VacuumHopperRecipe(val ticks: Int, val xp: Long, val input: Ingredient, val output: ItemStack) : Recipe<VacuumHopperEntity.Input> {

    override fun matches(input: VacuumHopperEntity.Input, world: World): Boolean {
        val parent = input.getParent()
        val inputStack = parent.getStack(9)
        val hasSpace = parent.getStack(10).let {
            it.isEmpty || (ItemStack.areItemsAndComponentsEqual(it, output) && it.count < it.maxCount)
        }
        return this.input.test(inputStack) && parent.tank.amount >= xp * 81 && hasSpace
    }

    override fun craft(input: VacuumHopperEntity.Input, lookup: RegistryWrapper.WrapperLookup): ItemStack {
        val parent = input.getParent()
        parent.getStack(9).decrement(1)
        parent.tank.amount -= xp * 81
        if(parent.getStack(10).isEmpty) {
            parent.setStack(10, output.copy())
        }else{
            parent.getStack(10).increment(1)
        }
        parent.markDirty()
        return output.copy()
    }

    override fun getType() = RecipeTypeCompendium.VACUUM_HOPPER

    override fun fits(width: Int, height: Int) = true

    override fun getResult(registriesLookup: RegistryWrapper.WrapperLookup): ItemStack = output

    override fun getSerializer() = RecipeSerializerCompendium.VACUUM_HOPPER

    override fun createIcon(): ItemStack = Items.EXPERIENCE_BOTTLE.defaultStack

    override fun getIngredients(): DefaultedList<Ingredient> = DefaultedList.ofSize(1, input)

    override fun isIgnoredInRecipeBook(): Boolean = true

}