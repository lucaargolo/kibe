package io.github.lucaargolo.kibe.datagen

import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipe
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.advancement.AdvancementCriterion
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.advancement.criterion.InventoryChangedCriterion
import net.minecraft.advancement.criterion.InventoryChangedCriterion.Conditions.Slots
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import java.util.*

internal fun RecipeExporter.shaped(category: RecipeCategory, item: ItemConvertible, count: Int = 1, name: Identifier = Registries.ITEM.getId(item.asItem()), build: ShapedRecipeJsonBuilder.() -> Unit) {
    ShapedRecipeJsonBuilder.create(category, item, count).apply(build).offerTo(this, name)
}

internal fun ShapedRecipeJsonBuilder.pattern(vararg rows: String) = rows.forEach { pattern(it) }

internal fun ShapedRecipeJsonBuilder.inputs(vararg pairs: Pair<Char, Any>) {
    pairs.forEach { (symbol, item) ->
        @Suppress("UNCHECKED_CAST")
        when (item) {
            is Ingredient -> input(symbol, item)
            is ItemConvertible -> input(symbol, item)
            is TagKey<*> -> input(symbol, item as TagKey<Item>)
        }
    }
}

internal fun ShapedRecipeJsonBuilder.criterion(item: ItemConvertible) {
    criterion(hasItem(item), conditionsFromItem(item))
}

internal fun ShapedRecipeJsonBuilder.criterion(tag: TagKey<Item>) {
    criterion("has_"+tag.id.path, conditionsFromTag(tag))
}

internal fun RecipeExporter.shapeless(category: RecipeCategory, item: ItemConvertible, count: Int = 1, name: Identifier = Registries.ITEM.getId(item.asItem()), build: ShapelessRecipeJsonBuilder.() -> Unit) {
    ShapelessRecipeJsonBuilder.create(category, item, count).apply(build).offerTo(this, name)
}

internal fun ShapelessRecipeJsonBuilder.inputs(vararg items: Any) {
    items.forEach { item  ->
        @Suppress("UNCHECKED_CAST")
        when (item) {
            is Ingredient -> input(item)
            is ItemConvertible -> input(item)
            is TagKey<*> -> input(item as TagKey<Item>)
        }
    }
}

internal fun ShapelessRecipeJsonBuilder.criterion(item: ItemConvertible) {
    criterion(hasItem(item), conditionsFromItem(item))
}

internal fun ShapelessRecipeJsonBuilder.criterion(tag: TagKey<Item>) {
    criterion("has_"+tag.id.path, conditionsFromTag(tag))
}

internal fun RecipeExporter.vacuum(input: Ingredient, xp: Long, output: ItemConvertible, ticks: Int = 60, name: Identifier = ModIdentifier.of(Registries.ITEM.getId(output.asItem()).path)) {
    accept(name, VacuumHopperRecipe(ticks, xp, input, ItemStack(output)), null)
}

internal fun RecipeExporter.smelting(input: Ingredient, category: RecipeCategory, output: ItemConvertible, experience: Float = 0.35F, time: Int = 200, name: Identifier = Registries.ITEM.getId(output.asItem()), build: CookingRecipeJsonBuilder.() -> Unit) {
    CookingRecipeJsonBuilder.createSmelting(input, category, output, experience, time).apply(build).offerTo(this, name)
}

internal fun CookingRecipeJsonBuilder.criterion(item: ItemConvertible) {
    criterion(hasItem(item), conditionsFromItem(item))
}

internal fun CookingRecipeJsonBuilder.criterion(tag: TagKey<Item>) {
    criterion("has_"+tag.id.path, conditionsFromTag(tag))
}

internal fun wool(color: DyeColor): ItemConvertible {
    return when (color) {
        DyeColor.WHITE -> Items.WHITE_WOOL
        DyeColor.ORANGE -> Items.ORANGE_WOOL
        DyeColor.MAGENTA -> Items.MAGENTA_WOOL
        DyeColor.LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL
        DyeColor.YELLOW -> Items.YELLOW_WOOL
        DyeColor.LIME -> Items.LIME_WOOL
        DyeColor.PINK -> Items.PINK_WOOL
        DyeColor.GRAY -> Items.GRAY_WOOL
        DyeColor.LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL
        DyeColor.CYAN -> Items.CYAN_WOOL
        DyeColor.PURPLE -> Items.PURPLE_WOOL
        DyeColor.BLUE -> Items.BLUE_WOOL
        DyeColor.BROWN -> Items.BROWN_WOOL
        DyeColor.GREEN -> Items.GREEN_WOOL
        DyeColor.RED -> Items.RED_WOOL
        DyeColor.BLACK -> Items.BLACK_WOOL
    }
}

internal fun dyes(color: DyeColor): TagKey<Item> {
    return when (color) {
        DyeColor.WHITE -> ConventionalItemTags.WHITE_DYES
        DyeColor.ORANGE -> ConventionalItemTags.ORANGE_DYES
        DyeColor.MAGENTA -> ConventionalItemTags.MAGENTA_DYES
        DyeColor.LIGHT_BLUE -> ConventionalItemTags.LIGHT_BLUE_DYES
        DyeColor.YELLOW -> ConventionalItemTags.YELLOW_DYES
        DyeColor.LIME -> ConventionalItemTags.LIME_DYES
        DyeColor.PINK -> ConventionalItemTags.PINK_DYES
        DyeColor.GRAY -> ConventionalItemTags.GRAY_DYES
        DyeColor.LIGHT_GRAY -> ConventionalItemTags.LIGHT_GRAY_DYES
        DyeColor.CYAN -> ConventionalItemTags.CYAN_DYES
        DyeColor.PURPLE -> ConventionalItemTags.PURPLE_DYES
        DyeColor.BLUE -> ConventionalItemTags.BLUE_DYES
        DyeColor.BROWN -> ConventionalItemTags.BROWN_DYES
        DyeColor.GREEN -> ConventionalItemTags.GREEN_DYES
        DyeColor.RED -> ConventionalItemTags.RED_DYES
        DyeColor.BLACK -> ConventionalItemTags.BLACK_DYES
    }
}

private fun conditionsFromItem(item: ItemConvertible): AdvancementCriterion<InventoryChangedCriterion.Conditions> {
    return conditionsFromPredicates(ItemPredicate.Builder.create().items(item))
}

private fun conditionsFromTag(tag: TagKey<Item>): AdvancementCriterion<InventoryChangedCriterion.Conditions> {
    return conditionsFromPredicates(ItemPredicate.Builder.create().tag(tag))
}

private fun conditionsFromPredicates(vararg predicates: ItemPredicate.Builder): AdvancementCriterion<InventoryChangedCriterion.Conditions> {
    return conditionsFromItemPredicates(*predicates.map(ItemPredicate.Builder::build).toTypedArray())
}

private fun conditionsFromItemPredicates(vararg predicates: ItemPredicate): AdvancementCriterion<InventoryChangedCriterion.Conditions> {
    return Criteria.INVENTORY_CHANGED.create(InventoryChangedCriterion.Conditions(Optional.empty(), Slots.ANY, predicates.toList()))
}

private fun hasItem(item: ItemConvertible): String {
    return "has_" + getItemPath(item)
}

private fun getItemPath(item: ItemConvertible): String {
    return Registries.ITEM.getId(item.asItem()).getPath()
}