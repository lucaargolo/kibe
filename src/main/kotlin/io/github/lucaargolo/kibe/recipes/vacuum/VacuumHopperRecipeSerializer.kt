package io.github.lucaargolo.kibe.recipes.vacuum

import com.google.gson.JsonObject
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class VacuumHopperRecipeSerializer : RecipeSerializer<VacuumHopperRecipe> {

    override fun write(buf: PacketByteBuf, recipe: VacuumHopperRecipe) {
        buf.writeInt(recipe.ticks)
        buf.writeLong(recipe.xpInput)
        recipe.input.write(buf)
        buf.writeItemStack(recipe.output)
    }

    override fun read(id: Identifier, json: JsonObject): VacuumHopperRecipe {
        val ticks = json.getAsJsonPrimitive("ticks").asInt
        val xpInput = json.getAsJsonPrimitive("xp").asLong
        val input = Ingredient.fromJson(json.get("input"))
        val output: ItemStack = json.getAsJsonPrimitive("output").asString.let { itemId ->
            val item = Registry.ITEM.getOrEmpty(Identifier(itemId))
            if(item.isPresent) {
                ItemStack(item.get())
            } else null
        } ?: ItemStack.EMPTY
        return VacuumHopperRecipe(id, ticks, xpInput, input, output)
    }

    override fun read(id: Identifier, buf: PacketByteBuf): VacuumHopperRecipe {
        val ticks = buf.readInt()
        val xpInput = buf.readLong()
        val input = Ingredient.fromPacket(buf)
        val output = buf.readItemStack()

        return VacuumHopperRecipe(id, ticks, xpInput, input, output)
    }

}