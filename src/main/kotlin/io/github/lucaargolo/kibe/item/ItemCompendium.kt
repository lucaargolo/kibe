@file:Suppress("UNCHECKED_CAST", "unused")

package io.github.lucaargolo.kibe.item

import io.github.ladysnake.pal.VanillaAbilities
import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.client.item.EntangledChestBlockItemDynamicRenderer
import io.github.lucaargolo.kibe.client.item.EntangledTankBlockItemDynamicRenderer
import io.github.lucaargolo.kibe.client.item.GliderDynamicRenderer
import io.github.lucaargolo.kibe.client.model.EntangledBagBakedModel
import io.github.lucaargolo.kibe.client.model.EntangledBucketBakedModel
import io.github.lucaargolo.kibe.client.model.TankBlockItemBakedModel
import io.github.lucaargolo.kibe.utils.ModIdentifier
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import io.github.lucaargolo.kibe.utils.helper.AbilityHelper
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.block.Block
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.fluid.Fluid
import net.minecraft.item.*
import net.minecraft.item.Item.Settings
import net.minecraft.registry.Registries
import net.minecraft.util.DyeColor
import net.minecraft.util.Rarity

object ItemCompendium: RegistryCompendium<Item>(Registries.ITEM) {

    val RUNES: Array<Rune>
        get() = runes.toTypedArray()
    private val runes = mutableListOf<Rune>()


    val KIBE         = register("kibe", Item(Settings().rarity(Rarity.COMMON).food(FoodComponent.Builder().hunger(6).saturationModifier(0.8F).meat().build())))
    val GOLDEN_KIBE  = register("golden_kibe", Item(Settings().rarity(Rarity.UNCOMMON).food(FoodComponent.Builder().hunger(8).saturationModifier(1.2F).meat().build())))
    val CURSED_KIBE  = register("cursed_kibe", Item(Settings().rarity(Rarity.UNCOMMON).food(FoodComponent.Builder().hunger(10).saturationModifier(1.2F).meat().build())))
    val DIAMOND_KIBE = register("diamond_kibe", Item(Settings().rarity(Rarity.RARE).food(FoodComponent.Builder().hunger(16).saturationModifier(1F).meat().build())))
    
    val CURSED_DROPLETS = register("cursed_droplets", Item(Settings()))
    val CURSED_SEEDS    = register("cursed_seeds", CursedSeeds(Settings()))
    
    val MAGNET = register("magnet", Magnet.create(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
    
    val DIAMOND_RING = register("diamond_ring",  Item(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
    val ANGEL_RING = register("angel_ring", AbilityRing.create(Settings().maxCount(1).rarity(Rarity.EPIC), VanillaAbilities.ALLOW_FLYING))
    val MAGMA_RING = register("magma_ring", AbilityRing.create(Settings().maxCount(1).rarity(Rarity.RARE), AbilityHelper.INFINITE_FIRE_RESISTENCE))
    val WATER_RING = register("water_ring", AbilityRing.create(Settings().maxCount(1).rarity(Rarity.RARE), AbilityHelper.INFINITE_WATER_BREATHING))
    val LIGHT_RING   = register("light_ring",  LightRing(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
    
    val GOLDEN_LASSO  = register("golden_lasso",  Lasso.GoldenLasso(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
    val CURSED_LASSO  = register("cursed_lasso",  Lasso.CursedLasso(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
    val DIAMOND_LASSO = register("diamond_lasso",  Lasso.DiamondLasso(Settings().maxCount(1).rarity(Rarity.RARE)))
    
    val WHITE_RUNE      = registerRune("white_rune",  Rune(DyeColor.WHITE, Settings()))
    val ORANGE_RUNE     = registerRune("orange_rune",  Rune(DyeColor.ORANGE, Settings()))
    val MAGENTA_RUNE    = registerRune("magenta_rune",  Rune(DyeColor.MAGENTA, Settings()))
    val LIGHT_BLUE_RUNE = registerRune("light_blue_rune",  Rune(DyeColor.LIGHT_BLUE, Settings()))
    val YELLOW_RUNE     = registerRune("yellow_rune",  Rune(DyeColor.YELLOW, Settings()))
    val LIME_RUNE       = registerRune("lime_rune",  Rune(DyeColor.LIME, Settings()))
    val PINK_RUNE       = registerRune("pink_rune",  Rune(DyeColor.PINK, Settings()))
    val GRAY_RUNE       = registerRune("gray_rune",  Rune(DyeColor.GRAY, Settings()))
    val LIGHT_GRAY_RUNE = registerRune("light_gray_rune",  Rune(DyeColor.LIGHT_GRAY, Settings()))
    val CYAN_RUNE       = registerRune("cyan_rune",  Rune(DyeColor.CYAN, Settings()))
    val BLUE_RUNE       = registerRune("blue_rune",  Rune(DyeColor.BLUE, Settings()))
    val PURPLE_RUNE     = registerRune("purple_rune",  Rune(DyeColor.PURPLE, Settings()))
    val GREEN_RUNE      = registerRune("green_rune",  Rune(DyeColor.GREEN, Settings()))
    val BROWN_RUNE      = registerRune("brown_rune",  Rune(DyeColor.BROWN, Settings()))
    val RED_RUNE        = registerRune("red_rune",  Rune(DyeColor.RED, Settings()))
    val BLACK_RUNE      = registerRune("black_rune",  Rune(DyeColor.BLACK, Settings()))
    
    val SLIME_BOOTS = register("slime_boots",  SlimeBoots(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
    val SLIME_SLING = register("slime_sling",  SlimeSling(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
    
    val TORCH_SLING = register("torch_sling",  TorchSling(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
    val ESCAPE_ROPE = register("escape_rope",  EscapeRope(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
    
    val WOODEN_BUCKET = register("wooden_bucket", WoodenBucket.Empty(Settings().maxCount(16)))
    val WATER_WOODEN_BUCKET = register("water_wooden_bucket", WoodenBucket.Water(Settings().maxCount(1)))
    
    val GLIDER_LEFT_WING = register("glider_left_wing", Item(Settings()))
    val GLIDER_RIGHT_WING = register("glider_right_wing", Item(Settings()))
    
    val WHITE_GLIDER = register("white_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val ORANGE_GLIDER = register("orange_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val MAGENTA_GLIDER = register("magenta_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val LIGHT_BLUE_GLIDER = register("light_blue_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val YELLOW_GLIDER = register("yellow_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val LIME_GLIDER = register("lime_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val PINK_GLIDER = register("pink_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val GRAY_GLIDER = register("gray_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val LIGHT_GRAY_GLIDER = register("light_gray_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val CYAN_GLIDER = register("cyan_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val BLUE_GLIDER = register("blue_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val PURPLE_GLIDER = register("purple_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val GREEN_GLIDER = register("green_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val BROWN_GLIDER = register("brown_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val RED_GLIDER = register("red_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    val BLACK_GLIDER = register("black_glider", Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)))
    
    val VOID_BUCKET = register("void_bucket", VoidBucket(Settings().maxCount(1).rarity(Rarity.RARE)))
    
    val POCKET_CRAFTING_TABLE = register("pocket_crafting_table",  PocketCraftingTable(Settings().maxCount(1)))
    val POCKET_TRASH_CAN = register("pocket_trash_can",  PocketTrashCan(Settings().maxCount(1)))
    
    val ENTANGLED_CHEST = register("entangled_chest", EntangledChestBlockItem(Settings()))
    val ENTANGLED_TANK = register("entangled_tank", EntangledTankBlockItem(Settings()))
    val ENTANGLED_BAG = register("entangled_bag",  EntangledBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val ENTANGLED_BUCKET = register("entangled_bucket",  EntangledBucket(Settings().maxCount(1).rarity(Rarity.RARE)))
    val COOLER = register("cooler", CoolerBlockItem(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
    val TANK = register("tank", TankBlockItem(Settings()))
    
    val WHITE_SLEEPING_BAG = register("white_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val ORANGE_SLEEPING_BAG = register("orange_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val MAGENTA_SLEEPING_BAG = register("magenta_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val LIGHT_BLUE_SLEEPING_BAG = register("light_blue_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val YELLOW_SLEEPING_BAG = register("yellow_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val LIME_SLEEPING_BAG = register("lime_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val PINK_SLEEPING_BAG = register("pink_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val GRAY_SLEEPING_BAG = register("gray_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val LIGHT_GRAY_SLEEPING_BAG = register("light_gray_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val CYAN_SLEEPING_BAG = register("cyan_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val BLUE_SLEEPING_BAG = register("sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val PURPLE_SLEEPING_BAG = register("purple_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val GREEN_SLEEPING_BAG = register("green_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val BROWN_SLEEPING_BAG = register("brown_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val RED_SLEEPING_BAG = register("red_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    val BLACK_SLEEPING_BAG = register("black_sleeping_bag", SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
    
    val MEASURING_TAPE = register("measuring_tape", MeasuringTape(Settings().maxCount(1)))

    fun registerRune(string: String, entry: Rune): Rune {
        return register(string, entry).also(runes::add)
    }

    fun <E : Fluid> registerBucketItem(string: String, entry: E): BucketItem {
        return register(string+"_bucket", BucketItem(entry, Settings().recipeRemainder(Items.BUCKET).maxCount(1)))
    }

    fun <E : Block> registerBlockItem(string: String, entry: E): BlockItem {
        return register(string, BlockItem(entry, Settings()))
    }

    override fun initializeClient() {
        ModelPredicateProviderRegistry.register(MEASURING_TAPE, ModIdentifier.of("extended"), MeasuringTape.PredicateProvider())
        BuiltinItemRendererRegistry.INSTANCE.register(BlockCompendium.ENTANGLED_CHEST, EntangledChestBlockItemDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(BlockCompendium.ENTANGLED_TANK, EntangledTankBlockItemDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(WHITE_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(ORANGE_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(MAGENTA_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(LIGHT_BLUE_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(YELLOW_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(LIME_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(PINK_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(GRAY_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(LIGHT_GRAY_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(CYAN_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(BLUE_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(PURPLE_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(GREEN_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(BROWN_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(RED_GLIDER, GliderDynamicRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(BLACK_GLIDER, GliderDynamicRenderer())
        ModelLoadingRegistry.INSTANCE.registerVariantProvider {
            ModelVariantProvider { modelIdentifier, _ ->
                if(modelIdentifier.namespace == KibeMod.MOD_ID && modelIdentifier.variant == "inventory") {
                    when (modelIdentifier.path) {
                        "entangled_bag" -> return@ModelVariantProvider EntangledBagBakedModel()
                        "entangled_bucket" -> return@ModelVariantProvider EntangledBucketBakedModel()
                        "tank" -> return@ModelVariantProvider TankBlockItemBakedModel()
                    }
                }
                return@ModelVariantProvider null
            }
        }
    }



}
