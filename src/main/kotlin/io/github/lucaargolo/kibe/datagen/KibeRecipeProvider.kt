package io.github.lucaargolo.kibe.datagen

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.fluid.FluidCompendium
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import java.util.concurrent.CompletableFuture

class KibeRecipeProvider(output: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>) : FabricRecipeProvider(output, registryLookup) {

    override fun generate(exporter: RecipeExporter) {
        generateRecipes(exporter)
    }

    private fun generateRecipes(exporter: RecipeExporter) = exporter.apply {

        shaped(RecipeCategory.TOOLS, ItemCompendium.ANGEL_RING) {
            pattern("#E#", "IPI", "#E#")
            inputs('#' to ConventionalItemTags.NETHER_STARS, 'E' to Items.END_CRYSTAL, 'I' to Items.ELYTRA, 'P' to ItemCompendium.DIAMOND_RING)
            criterion(Items.ELYTRA)
        }

        generator(BlockCompendium.BASALT_GENERATOR_MK1, BlockCompendium.BASALT_GENERATOR_MK2, BlockCompendium.BASALT_GENERATOR_MK3, BlockCompendium.BASALT_GENERATOR_MK4, BlockCompendium.BASALT_GENERATOR_MK5) {
            pattern("###", "WIL", "#C#")
            inputs('#' to Items.SOUL_SOIL, 'W' to Items.BLUE_ICE, 'I' to ConventionalItemTags.STORAGE_BLOCKS_IRON, 'L' to ConventionalItemTags.LAVA_BUCKETS, 'C' to ConventionalItemTags.CHESTS)
            criterion(Items.SOUL_SOIL)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.BIG_TORCH) {
            pattern("#", "O", "O")
            inputs('#' to ConventionalItemTags.STORAGE_BLOCKS_COAL, 'O' to ItemTags.LOGS)
            criterion(ConventionalItemTags.STORAGE_BLOCKS_COAL)
        }

        BlockCompendium.ELEVATORS.associations.forEach { (color, elevator) ->
            shaped(RecipeCategory.DECORATIONS, elevator) {
                pattern("#E#", "iPi", "#i#")
                inputs('#' to wool(color), 'E' to ConventionalItemTags.ENDER_PEARLS, 'i' to ConventionalItemTags.IRON_INGOTS, 'P' to Items.PISTON)
                criterion(ConventionalItemTags.ENDER_PEARLS)
            }
            shapeless(RecipeCategory.DECORATIONS, elevator, name = ModIdentifier.of("${color.getName()}_elevator_from_elevator")) {
                inputs(ItemCompendium.ELEVATORS.key, dyes(color))
                criterion(ItemCompendium.ELEVATORS.key)
            }
        }

        ItemCompendium.GLIDERS.associations.forEach { (color, glider) ->
            shaped(RecipeCategory.TOOLS, glider) {
                pattern(" # ", "LiD")
                inputs('#' to dyes(color), 'L' to ItemCompendium.GLIDER_LEFT_WING, 'i' to ConventionalItemTags.IRON_INGOTS, 'D' to ItemCompendium.GLIDER_RIGHT_WING)
                criterion(ItemTags.WOOL)
            }
            shapeless(RecipeCategory.TOOLS, glider, name = ModIdentifier.of("${color.getName()}_glider_from_glider")) {
                inputs(ItemCompendium.GLIDERS.key, dyes(color))
                criterion(ItemCompendium.GLIDERS.key)
            }
        }

        ItemCompendium.SLEEPING_BAGS.associations.forEach { (color, sleepingBag) ->
            shaped(RecipeCategory.TOOLS, sleepingBag) {
                pattern("###", "SSS")
                inputs('#' to wool(color), 'S' to ConventionalItemTags.STRINGS)
                criterion(ItemTags.WOOL)
            }
            shapeless(RecipeCategory.TOOLS, sleepingBag, name = ModIdentifier.of("${color.getName()}_sleeping_bag_from_sleeping_bag")) {
                inputs(ItemCompendium.SLEEPING_BAGS.key, dyes(color))
                criterion(ItemCompendium.SLEEPING_BAGS.key)
            }
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.BREAKER) {
            pattern("#D#", "RZR", "CBC")
            inputs('#' to Items.SMOOTH_STONE, 'D' to Items.DIAMOND_PICKAXE, 'R' to ConventionalItemTags.REDSTONE_DUSTS, 'Z' to Items.DISPENSER, 'C' to ConventionalItemTags.NORMAL_COBBLESTONES, 'B' to ConventionalItemTags.CHESTS)
            criterion(Items.DISPENSER)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.CHUNK_LOADER) {
            pattern("###", "GOG", "OEO")
            inputs('#' to Items.ENDER_EYE, 'G' to ConventionalItemTags.STORAGE_BLOCKS_GOLD, 'O' to ConventionalItemTags.OBSIDIANS, 'E' to Items.ENCHANTING_TABLE)
            criterion(Items.DIAMOND_PICKAXE)
        }

        generator(BlockCompendium.COBBLESTONE_GENERATOR_MK1, BlockCompendium.COBBLESTONE_GENERATOR_MK2, BlockCompendium.COBBLESTONE_GENERATOR_MK3, BlockCompendium.COBBLESTONE_GENERATOR_MK4, BlockCompendium.COBBLESTONE_GENERATOR_MK5) {
            pattern("###", "WIL", "#C#")
            inputs('#' to Items.COBBLESTONE, 'W' to ConventionalItemTags.WATER_BUCKETS, 'I' to ConventionalItemTags.STORAGE_BLOCKS_IRON, 'L' to ConventionalItemTags.LAVA_BUCKETS, 'C' to ConventionalItemTags.CHESTS)
            criterion(ConventionalItemTags.LAVA_BUCKETS)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.COOLER) {
            pattern("#W#", "SCS", "#S#")
            inputs('#' to Items.BLUE_ICE, 'W' to Items.WHITE_STAINED_GLASS, 'S' to Items.SNOW_BLOCK, 'C' to ConventionalItemTags.CHESTS)
            criterion(Items.BLUE_ICE)
        }

        shaped(RecipeCategory.FOOD, ItemCompendium.CURSED_KIBE, 8) {
            pattern(" C ", "C#C", " C ")
            inputs('#' to ItemCompendium.CURSED_DROPLETS, 'C' to ItemCompendium.KIBE)
            criterion(ItemCompendium.CURSED_DROPLETS)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.CURSED_LASSO) {
            pattern("###", "#L#", "###")
            inputs('#' to ItemCompendium.CURSED_DROPLETS, 'L' to ItemCompendium.GOLDEN_LASSO)
            criterion(ItemCompendium.CURSED_DROPLETS)
        }

        shaped(RecipeCategory.MISC, ItemCompendium.CURSED_SEEDS) {
            pattern(" # ", "#W#", " # ")
            inputs('#' to ItemCompendium.CURSED_DROPLETS, 'W' to ConventionalItemTags.SEEDS)
            criterion(ItemCompendium.CURSED_DROPLETS)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.DEHUMIDIFIER) {
            pattern("#S#", "LFL", "#N#")
            inputs('#' to ConventionalItemTags.NORMAL_COBBLESTONES, 'S' to Items.SPONGE, 'L' to ConventionalItemTags.LAPIS_GEMS, 'F' to Items.FURNACE, 'N' to ConventionalItemTags.SANDS)
            criterion(Items.SPONGE)
        }

        shaped(RecipeCategory.FOOD, ItemCompendium.DIAMOND_KIBE, 8) {
            pattern("#C#", "C#C", "#C#")
            inputs('#' to ConventionalItemTags.DIAMOND_GEMS, 'C' to ItemCompendium.GOLDEN_KIBE)
            criterion(ItemCompendium.GOLDEN_KIBE)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.DIAMOND_LASSO) {
            pattern(" # ", "DLD", " S ")
            inputs('#' to ItemCompendium.GOLDEN_LASSO, 'D' to ConventionalItemTags.DIAMOND_GEMS, 'L' to Items.LEAD, 'S' to ItemCompendium.CURSED_LASSO)
            criterion(ItemCompendium.GOLDEN_LASSO)
        }

        shaped(RecipeCategory.MISC, ItemCompendium.DIAMOND_RING) {
            pattern("#D#", "g g", " g ")
            inputs('#' to Items.GOLD_NUGGET, 'D' to ConventionalItemTags.DIAMOND_GEMS, 'g' to ConventionalItemTags.GOLD_INGOTS)
            criterion(ConventionalItemTags.DIAMOND_GEMS)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.DIAMOND_SPIKES, 4) {
            pattern(" # ", "#_#")
            inputs('#' to Items.DIAMOND_SWORD, '_' to Ingredient.ofItems(BlockCompendium.IRON_SPIKES, BlockCompendium.GOLD_SPIKES))
            criterion(BlockCompendium.STONE_SPIKES)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.DRAWBRIDGE) {
            pattern("gGg", "PDP", "PRP")
            inputs('g' to ConventionalItemTags.GOLD_INGOTS, 'G' to Items.GOLDEN_PICKAXE, 'P' to Items.POLISHED_BASALT, 'D' to Items.DISPENSER, 'R' to ConventionalItemTags.REDSTONE_DUSTS)
            criterion(Items.DISPENSER)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.ENTANGLED_BAG) {
            pattern(" # ", "#E#", "###")
            inputs('#' to ConventionalItemTags.LEATHERS, 'E' to ItemCompendium.ENTANGLED_CHEST)
            criterion(ItemCompendium.ENTANGLED_CHEST)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.ENTANGLED_BUCKET) {
            pattern("g", "S")
            inputs('g' to ItemCompendium.ENTANGLED_TANK, 'S' to ItemCompendium.VOID_BUCKET)
            criterion(ItemCompendium.ENTANGLED_TANK)
        }

        shaped(RecipeCategory.DECORATIONS, ItemCompendium.ENTANGLED_CHEST) {
            pattern("/S/", "ENE", "/G/")
            inputs('/' to ConventionalItemTags.BLAZE_RODS, 'S' to Items.STONE_BRICKS, 'E' to Items.ENDER_EYE, 'N' to Items.ENDER_CHEST, 'G' to ConventionalItemTags.STORAGE_BLOCKS_GOLD)
            criterion(Items.ENDER_EYE)
        }

        shaped(RecipeCategory.DECORATIONS, ItemCompendium.ENTANGLED_TANK) {
            pattern("/S/", "ECE", "/G/")
            inputs('/' to ConventionalItemTags.BLAZE_RODS, 'S' to Items.STONE_BRICKS, 'E' to Items.ENDER_EYE, 'C' to Items.CAULDRON, 'G' to ConventionalItemTags.STORAGE_BLOCKS_GOLD)
            criterion(Items.ENDER_EYE)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.ESCAPE_ROPE) {
            pattern("## ", "## ", "##i")
            inputs('#' to ConventionalItemTags.STRINGS, 'i' to ConventionalItemTags.IRON_INGOTS)
            criterion(ConventionalItemTags.STRINGS)
        }

        vacuum(Ingredient.ofItems(Items.GLASS_BOTTLE), 333, Items.EXPERIENCE_BOTTLE, 20)

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.EXPRESS_CONVEYOR_BELT, 8) {
            pattern("###", "SLS", "iii")
            inputs('#' to ConventionalItemTags.BLUE_DYES, 'S' to BlockCompendium.FAST_CONVEYOR_BELT, 'L' to Items.SUGAR, 'i' to ConventionalItemTags.IRON_INGOTS)
            criterion(BlockCompendium.FAST_CONVEYOR_BELT)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.FAST_CONVEYOR_BELT, 8) {
            pattern("###", "SLS", "iii")
            inputs('#' to ConventionalItemTags.RED_DYES, 'S' to BlockCompendium.REGULAR_CONVEYOR_BELT, 'L' to Items.SUGAR, 'i' to ConventionalItemTags.IRON_INGOTS)
            criterion(BlockCompendium.REGULAR_CONVEYOR_BELT)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.FLUID_HOPPER) {
            pattern("i i", "iCi", " i ")
            inputs('i' to ConventionalItemTags.IRON_INGOTS, 'C' to Items.CAULDRON)
            criterion(Items.CAULDRON)
        }

        shaped(RecipeCategory.MISC, ItemCompendium.GLIDER_LEFT_WING) {
            pattern(" #L", "#LL", "LLW")
            inputs('#' to ConventionalItemTags.STRINGS, 'L' to ConventionalItemTags.LEATHERS, 'W' to Items.WHITE_WOOL)
            criterion(ConventionalItemTags.LEATHERS)
        }

        shapeless(RecipeCategory.MISC, ItemCompendium.GLIDER_LEFT_WING, name = ModIdentifier.of("glider_left_wing_from_right")) {
            inputs(ItemCompendium.GLIDER_RIGHT_WING)
            criterion(ItemCompendium.GLIDER_RIGHT_WING)
        }

        shaped(RecipeCategory.MISC, ItemCompendium.GLIDER_RIGHT_WING) {
            pattern("#S ", "##S", "W##")
            inputs('#' to ConventionalItemTags.LEATHERS, 'S' to ConventionalItemTags.STRINGS, 'W' to Items.WHITE_WOOL)
            criterion(ConventionalItemTags.LEATHERS)
        }

        shapeless(RecipeCategory.MISC, ItemCompendium.GLIDER_RIGHT_WING, name = ModIdentifier.of("glider_right_wing_from_left")) {
            inputs(ItemCompendium.GLIDER_LEFT_WING)
            criterion(ItemCompendium.GLIDER_LEFT_WING)
        }

        shaped(RecipeCategory.FOOD, ItemCompendium.GOLDEN_KIBE) {
            pattern("###", "#C#", "###")
            inputs('#' to ConventionalItemTags.GOLD_NUGGETS, 'C' to ItemCompendium.KIBE)
            criterion(ItemCompendium.KIBE)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.GOLDEN_LASSO) {
            pattern("#E#", "gLg", "#E#")
            inputs('#' to Items.GOLD_NUGGET, 'E' to Items.ENDER_EYE, 'g' to ConventionalItemTags.GOLD_INGOTS, 'L' to Items.LEAD)
            criterion(Items.ENDER_EYE)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.GOLD_SPIKES, 4) {
            pattern(" # ", "#_#")
            inputs('#' to Items.GOLDEN_SWORD, '_' to BlockCompendium.STONE_SPIKES)
            criterion(BlockCompendium.STONE_SPIKES)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.HEATER) {
            pattern("#B#", "BRB", "#B#")
            inputs('#' to ConventionalItemTags.NORMAL_COBBLESTONES, 'B' to Items.BLAZE_POWDER, 'R' to Items.REDSTONE_LAMP)
            criterion(Items.BLAZE_POWDER)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.IGNITER) {
            pattern("#", "D", "N")
            inputs('#' to Items.FLINT_AND_STEEL, 'D' to Items.DISPENSER, 'N' to Items.NETHERRACK)
            criterion(Items.DISPENSER)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.IRON_SPIKES, 4) {
            pattern(" # ", "#_#")
            inputs('#' to Items.IRON_SWORD, '_' to BlockCompendium.STONE_SPIKES)
            criterion(BlockCompendium.STONE_SPIKES)
        }

        shaped(RecipeCategory.FOOD, ItemCompendium.KIBE, 4) {
            pattern(" # ", "CWC", " # ")
            inputs('#' to Items.BONE_MEAL, 'C' to Items.COOKED_MUTTON, 'W' to Items.WHEAT)
            criterion(Items.COOKED_MUTTON)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.LIGHT_RING) {
            pattern("#G#", "GOG", "#G#")
            inputs('#' to Items.SOUL_TORCH, 'G' to ConventionalItemTags.GLOWSTONE_DUSTS, 'O' to ItemCompendium.DIAMOND_RING)
            criterion(ItemCompendium.DIAMOND_RING)
        }

        vacuum(Ingredient.ofItems(Items.BUCKET), 1000, FluidCompendium.LIQUID_XP.fluidBucket!!)

        shaped(RecipeCategory.TOOLS, ItemCompendium.MAGMA_RING) {
            pattern(" # ", "MGM", "NON")
            inputs('#' to Items.NETHER_STAR, 'M' to Items.MAGMA_CREAM, 'G' to ItemCompendium.DIAMOND_RING, 'N' to Items.NETHER_WART, 'O' to ConventionalItemTags.OBSIDIANS)
            criterion(ItemCompendium.DIAMOND_RING)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.MAGNET) {
            pattern("#ii", "iE ", "#ii")
            inputs('#' to ConventionalItemTags.RED_DYES, 'i' to ConventionalItemTags.IRON_INGOTS, 'E' to Items.ENDER_EYE)
            criterion(Items.ENDER_EYE)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.MEASURING_TAPE) {
            pattern(" l ", "lil", " ly")
            inputs('y' to ConventionalItemTags.YELLOW_DYES, 'i' to ConventionalItemTags.IRON_INGOTS, 'l' to ConventionalItemTags.LAPIS_GEMS)
            criterion(ConventionalItemTags.LAPIS_GEMS)
        }

        shaped(RecipeCategory.BUILDING_BLOCKS, BlockCompendium.OBSIDIAN_SAND, 2) {
            pattern("#O", "O#")
            inputs('#' to ConventionalItemTags.SANDS, 'O' to ConventionalItemTags.OBSIDIANS)
            criterion(ConventionalItemTags.OBSIDIANS)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.PLACER) {
            pattern("#D#", "RPR", "CLC")
            inputs('#' to Items.SMOOTH_STONE, 'D' to Items.DROPPER, 'R' to ConventionalItemTags.REDSTONE_DUSTS, 'P' to Items.DISPENSER, 'C' to ConventionalItemTags.NORMAL_COBBLESTONES, 'L' to ConventionalItemTags.CHESTS)
            criterion(Items.DISPENSER)
        }

        shapeless(RecipeCategory.TOOLS, ItemCompendium.POCKET_CRAFTING_TABLE) {
            inputs(ItemTags.SIGNS, Items.CRAFTING_TABLE)
            criterion(Items.CRAFTING_TABLE)
        }

        shapeless(RecipeCategory.TOOLS, ItemCompendium.POCKET_TRASH_CAN) {
            inputs(ItemTags.SIGNS, BlockCompendium.TRASH_CAN)
            criterion(BlockCompendium.TRASH_CAN)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.REDSTONE_TIMER) {
            pattern("#RS", "SQR", "HRS")
            inputs('#' to Items.REDSTONE_TORCH, 'R' to Items.REPEATER, 'S' to Items.STONE, 'Q' to Items.QUARTZ_BLOCK, 'H' to ConventionalItemTags.REDSTONE_DUSTS)
            criterion(Items.REPEATER)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.REGULAR_CONVEYOR_BELT, 8) {
            pattern("###", "SLS", "iii")
            inputs('#' to ConventionalItemTags.YELLOW_DYES, 'S' to ConventionalItemTags.LEATHERS, 'L' to Items.SUGAR, 'i' to ConventionalItemTags.IRON_INGOTS)
            criterion(Items.SUGAR)
        }

        shaped(RecipeCategory.COMBAT, ItemCompendium.SLIME_BOOTS) {
            pattern("# #", "S S")
            inputs('#' to Items.SLIME_BALL, 'S' to Items.SLIME_BLOCK)
            criterion(Items.SLIME_BALL)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.SLIME_SLING) {
            pattern("#S#", "W W", " W ")
            inputs('#' to ConventionalItemTags.STRINGS, 'S' to Items.SLIME_BLOCK, 'W' to Items.SLIME_BALL)
            criterion(Items.SLIME_BALL)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.STONE_SPIKES, 4) {
            pattern(" # ", "#_#")
            inputs('#' to Items.STONE_SWORD, '_' to Items.COBBLESTONE_SLAB)
            criterion(ConventionalItemTags.NORMAL_COBBLESTONES)
        }

        shaped(RecipeCategory.DECORATIONS, ItemCompendium.TANK) {
            pattern("#G#", "G G", "#G#")
            inputs('#' to ConventionalItemTags.OBSIDIANS, 'G' to ConventionalItemTags.GLASS_BLOCKS)
            criterion(ConventionalItemTags.OBSIDIANS)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.TORCH_SLING) {
            pattern("#C#", "/ /", " / ")
            inputs('#' to ConventionalItemTags.STRINGS, 'C' to Items.COAL, '/' to Items.STICK)
            criterion(Items.TORCH)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.TRASH_CAN) {
            pattern("###", "SCS", "SFS")
            inputs('#' to Items.STONE, 'S' to Items.STONE_BRICKS, 'C' to ConventionalItemTags.CHESTS, 'F' to Items.CACTUS)
            criterion(Items.CACTUS)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.VACUUM_HOPPER) {
            pattern("#I#", "CEB", "#I#")
            inputs('#' to ConventionalItemTags.NORMAL_OBSIDIANS, 'I' to Items.IRON_BARS, 'C' to ConventionalItemTags.CHESTS, 'E' to Items.ENDER_EYE, 'B' to Items.BUCKET)
            criterion(Items.ENDER_EYE)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.VOID_BUCKET) {
            pattern(" # ", "OBO", " O ")
            inputs('#' to Items.ENDER_EYE, 'O' to ConventionalItemTags.NORMAL_OBSIDIANS, 'B' to Items.BUCKET)
            criterion(Items.ENDER_EYE)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.WATER_RING) {
            pattern(" # ", "PGP", "NTN")
            inputs('#' to Items.NETHER_STAR, 'P' to Items.PUFFERFISH, 'G' to ItemCompendium.DIAMOND_RING, 'N' to Items.NETHER_WART, 'T' to Items.LILY_PAD)
            criterion(ItemCompendium.DIAMOND_RING)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.WITHER_BUILDER) {
            pattern("###", "ODO", "ONO")
            inputs('#' to Ingredient.ofItems(Items.SOUL_SAND, Items.SOUL_SOIL), 'O' to BlockCompendium.WITHER_PROOF_BLOCK, 'D' to Items.DISPENSER, 'N' to ConventionalItemTags.NETHER_STARS)
            criterion(Items.DISPENSER)
        }

        shaped(RecipeCategory.BUILDING_BLOCKS, BlockCompendium.WITHER_PROOF_BLOCK, 4) {
            pattern("#O#", "OnO", "#O#")
            inputs('#' to Items.CHAIN, 'O' to ConventionalItemTags.NORMAL_OBSIDIANS, 'n' to ConventionalItemTags.NETHERITE_INGOTS)
            criterion(ConventionalItemTags.NETHERITE_INGOTS)
        }

        smelting(Ingredient.ofItems(BlockCompendium.WITHER_PROOF_SAND), RecipeCategory.BUILDING_BLOCKS, BlockCompendium.WITHER_PROOF_GLASS) {
            criterion(ConventionalItemTags.NETHERITE_INGOTS)
        }

        shaped(RecipeCategory.BUILDING_BLOCKS, BlockCompendium.WITHER_PROOF_SAND, 4) {
            pattern("#O#", "OnO", "#O#")
            inputs('#' to Items.CHAIN, 'O' to BlockCompendium.OBSIDIAN_SAND, 'n' to ConventionalItemTags.NETHERITE_INGOTS)
            criterion(ConventionalItemTags.NETHERITE_INGOTS)
        }

        shaped(RecipeCategory.TOOLS, ItemCompendium.WOODEN_BUCKET) {
            pattern("# #", " # ")
            inputs('#' to ItemTags.LOGS)
            criterion(ItemTags.LOGS)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.XP_DRAIN) {
            pattern("iIi", " B ")
            inputs('i' to ConventionalItemTags.IRON_INGOTS, 'I' to Items.IRON_BARS, 'B' to Items.BUCKET)
            criterion(Items.IRON_BARS)
        }

        shaped(RecipeCategory.DECORATIONS, BlockCompendium.XP_SHOWER) {
            pattern("iB", " I")
            inputs('i' to ConventionalItemTags.IRON_INGOTS, 'B' to Items.BUCKET, 'I' to Items.IRON_BARS)
            criterion(Items.IRON_BARS)
        }

    }

    private fun RecipeExporter.generator(mk1: ItemConvertible, mk2: ItemConvertible, mk3: ItemConvertible, mk4: ItemConvertible, mk5: ItemConvertible, build: ShapedRecipeJsonBuilder.() -> Unit) {
        shaped(RecipeCategory.DECORATIONS, mk1, 1, Registries.ITEM.getId(mk1.asItem()), build)
        fun upgrade(input: ItemConvertible, material: TagKey<Item>, output: ItemConvertible) {
            shaped(RecipeCategory.DECORATIONS, output) {
                pattern(" # ", "#G#", " # ")
                inputs('#' to input, 'G' to material)
                criterion(input)
            }
        }
        upgrade(mk1, ConventionalItemTags.STORAGE_BLOCKS_GOLD, mk2)
        upgrade(mk2, ConventionalItemTags.STORAGE_BLOCKS_DIAMOND, mk3)
        upgrade(mk3, ConventionalItemTags.STORAGE_BLOCKS_EMERALD, mk4)
        upgrade(mk4, ConventionalItemTags.STORAGE_BLOCKS_NETHERITE, mk5)
    }

}