package io.github.lucaargolo.kibe.recipes.vacuum

import com.google.gson.JsonObject
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.DefaultedList
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry

class VacuumHopperRecipeSerializer : RecipeSerializer<VacuumHopperRecipe> {

    override fun write(buf: PacketByteBuf, recipe: VacuumHopperRecipe) {
        recipe.previewInputs.forEach {
            it.write(buf)
        }
        buf.writeInt(recipe.xpInput)
        buf.writeItemStack(recipe.output)
    }

    override fun read(id: Identifier, json: JsonObject): VacuumHopperRecipe {
        val input = DefaultedList.ofSize(1, Ingredient.EMPTY)
        json.let {
            arrayOf("input").forEachIndexed { index, key ->
                json[key].let {
                    if(it is JsonObject) {
                        input[index] = Ingredient.fromJson(it)
                    }
                }
            }
        }
        val xpInput = json.getAsJsonPrimitive("xpinput").asInt
        val output: ItemStack = json.getAsJsonPrimitive("output").asString.let { itemId ->
            val item = Registry.ITEM.getOrEmpty(Identifier(itemId))
            if(item.isPresent) {
                ItemStack(item.get())
            } else null
        } ?: ItemStack.EMPTY
        return VacuumHopperRecipe(id, output, input, xpInput)
    }

    override fun read(id: Identifier, buf: PacketByteBuf): VacuumHopperRecipe {
        val input = DefaultedList.ofSize(1, Ingredient.EMPTY)
        buf.let {
            0.until(input.size).forEach { index ->
                input[index] = Ingredient.fromPacket(buf)
            }
        }
        val xpInput = buf.readInt()
        val output = buf.readItemStack()

        return VacuumHopperRecipe(id, output ?: ItemStack.EMPTY, input, xpInput)
    }

}