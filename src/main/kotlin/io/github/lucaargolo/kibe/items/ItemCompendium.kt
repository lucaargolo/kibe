package io.github.lucaargolo.kibe.items

import io.github.ladysnake.pal.VanillaAbilities
import io.github.lucaargolo.kibe.CLIENT
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.items.entangled.EntangledBag
import io.github.lucaargolo.kibe.items.entangled.EntangledBagBakedModel
import io.github.lucaargolo.kibe.items.entangled.EntangledBagContainer
import io.github.lucaargolo.kibe.items.entangled.EntangledBagScreen
import io.github.lucaargolo.kibe.items.miscellaneous.*
import io.github.lucaargolo.kibe.items.trashcan.PocketTrashCan
import io.github.lucaargolo.kibe.items.trashcan.PocketTrashCanContainer
import io.github.lucaargolo.kibe.items.trashcan.PocketTrashCanScreen
import io.github.lucaargolo.kibe.utils.INFINITE_FIRE_RESISTENCE
import io.github.lucaargolo.kibe.utils.INFINITE_WATER_BREATHING
import net.minecraft.item.FoodComponent
import net.minecraft.item.FoodComponents
import net.minecraft.item.Item.*
import net.minecraft.item.Item
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity

val itemRegistry = mutableMapOf<Identifier, ModItem>()

val KIBE         = register(Identifier(MOD_ID, "kibe"), ModItem(Item(Settings().rarity(Rarity.COMMON).food(FoodComponent.Builder().hunger(6).saturationModifier(0.8F).meat().build()))))
val GOLDEN_KIBE  = register(Identifier(MOD_ID, "golden_kibe"), ModItem(Item(Settings().rarity(Rarity.UNCOMMON).food(FoodComponent.Builder().hunger(8).saturationModifier(1.2F).meat().build()))))
val CURSED_KIBE  = register(Identifier(MOD_ID, "cursed_kibe"), ModItem(Item(Settings().rarity(Rarity.UNCOMMON).food(FoodComponent.Builder().hunger(10).saturationModifier(1.2F).meat().build()))))
val DIAMOND_KIBE = register(Identifier(MOD_ID, "diamond_kibe"), ModItem(Item(Settings().rarity(Rarity.RARE).food(FoodComponent.Builder().hunger(16).saturationModifier(1F).meat().build()))))

val CURSED_DROPLETS = register(Identifier(MOD_ID, "cursed_droplets"), ModItem())
val CURSED_SEEDS    = register(Identifier(MOD_ID, "cursed_seeds"),  ModItem(CursedSeeds(Settings())))

val MAGNET = register(Identifier(MOD_ID, "magnet"), ModItem(BooleanItem(Settings().maxCount(1).rarity(Rarity.UNCOMMON))))

val DIAMOND_RING = register(Identifier(MOD_ID, "diamond_ring"),  ModItem(Item(Settings().maxCount(1).rarity(Rarity.UNCOMMON))))
val ANGEL_RING   = register(Identifier(MOD_ID, "angel_ring"),  ModItem(AbilityRing(Settings().maxCount(1).rarity(Rarity.EPIC), VanillaAbilities.ALLOW_FLYING)))
val MAGMA_RING   = register(Identifier(MOD_ID, "magma_ring"),  ModItem(AbilityRing(Settings().maxCount(1).rarity(Rarity.RARE), INFINITE_FIRE_RESISTENCE)))
val WATER_RING   = register(Identifier(MOD_ID, "water_ring"),  ModItem(AbilityRing(Settings().maxCount(1).rarity(Rarity.RARE), INFINITE_WATER_BREATHING)))
val LIGHT_RING   = register(Identifier(MOD_ID, "light_ring"),  ModItem(LightRing(Settings().maxCount(1).rarity(Rarity.UNCOMMON))))

val GOLDEN_LASSO  = register(Identifier(MOD_ID, "golden_lasso"),  ModItem(Lasso.GoldenLasso(Settings().maxCount(1).rarity(Rarity.UNCOMMON))))
val CURSED_LASSO  = register(Identifier(MOD_ID, "cursed_lasso"),  ModItem(Lasso.CursedLasso(Settings().maxCount(1).rarity(Rarity.UNCOMMON))))
val DIAMOND_LASSO = register(Identifier(MOD_ID, "diamond_lasso"),  ModItem(Lasso.DiamondLasso(Settings().maxCount(1).rarity(Rarity.RARE))))

val WHITE_RUNE      = register(Identifier(MOD_ID, "white_rune"),  ModItem(Rune(DyeColor.WHITE, Settings())))
val ORANGE_RUNE     = register(Identifier(MOD_ID, "orange_rune"),  ModItem(Rune(DyeColor.ORANGE, Settings())))
val MAGENTA_RUNE    = register(Identifier(MOD_ID, "magenta_rune"),  ModItem(Rune(DyeColor.MAGENTA, Settings())))
val LIGHT_BLUE_RUNE = register(Identifier(MOD_ID, "light_blue_rune"),  ModItem(Rune(DyeColor.LIGHT_BLUE, Settings())))
val YELLOW_RUNE     = register(Identifier(MOD_ID, "yellow_rune"),  ModItem(Rune(DyeColor.YELLOW, Settings())))
val LIME_RUNE       = register(Identifier(MOD_ID, "lime_rune"),  ModItem(Rune(DyeColor.LIME, Settings())))
val PINK_RUNE       = register(Identifier(MOD_ID, "pink_rune"),  ModItem(Rune(DyeColor.PINK, Settings())))
val GRAY_RUNE       = register(Identifier(MOD_ID, "gray_rune"),  ModItem(Rune(DyeColor.GRAY, Settings())))
val LIGHT_GRAY_RUNE = register(Identifier(MOD_ID, "light_gray_rune"),  ModItem(Rune(DyeColor.LIGHT_GRAY, Settings())))
val CYAN_RUNE       = register(Identifier(MOD_ID, "cyan_rune"),  ModItem(Rune(DyeColor.CYAN, Settings())))
val BLUE_RUNE       = register(Identifier(MOD_ID, "blue_rune"),  ModItem(Rune(DyeColor.BLUE, Settings())))
val PURPLE_RUNE     = register(Identifier(MOD_ID, "purple_rune"),  ModItem(Rune(DyeColor.PURPLE, Settings())))
val GREEN_RUNE      = register(Identifier(MOD_ID, "green_rune"),  ModItem(Rune(DyeColor.GREEN, Settings())))
val BROWN_RUNE      = register(Identifier(MOD_ID, "brown_rune"),  ModItem(Rune(DyeColor.BROWN, Settings())))
val RED_RUNE        = register(Identifier(MOD_ID, "red_rune"),  ModItem(Rune(DyeColor.RED, Settings())))
val BLACK_RUNE      = register(Identifier(MOD_ID, "black_rune"),  ModItem(Rune(DyeColor.BLACK, Settings())))

val SLIME_BOOTS = register(Identifier(MOD_ID, "slime_boots"),  ModItem(SlimeBoots(Settings().maxCount(1).rarity(Rarity.UNCOMMON))))
val SLIME_SLING = register(Identifier(MOD_ID, "slime_sling"),  ModItem(SlimeSling(Settings().maxCount(1).rarity(Rarity.UNCOMMON))))

val WOODEN_BUCKET = register(Identifier(MOD_ID, "wooden_bucket"), ModItem(WoodenBucket.Empty(Settings().maxCount(16))))
val WATER_WOODEN_BUCKET = register(Identifier(MOD_ID, "water_wooden_bucket"), ModItem(WoodenBucket.Water(Settings().maxCount(1))))

val GLIDER = register(Identifier(MOD_ID, "glider"), ModItem(Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON)), GliderBakedModel()))

val VOID_BUCKET = register(Identifier(MOD_ID, "void_bucket"), ModItem(VoidBucket(Settings().maxCount(1).rarity(Rarity.RARE))))

val POCKET_CRAFTING_TABLE = register(Identifier(MOD_ID, "pocket_crafting_table"),  ModItem(PocketCraftingTable(Settings().maxCount(1))))
val POCKET_TRASH_CAN =
    if(CLIENT)
        register(Identifier(MOD_ID, "pocket_trash_can"),  ModItem(PocketTrashCan(Settings().maxCount(1)), PocketTrashCanContainer::class, PocketTrashCanScreen::class))
    else
        register(Identifier(MOD_ID, "pocket_trash_can"),  ModItem(PocketTrashCan(Settings().maxCount(1)), PocketTrashCanContainer::class))

val ENTANGLED_BAG =
    if(CLIENT)
        register(Identifier(MOD_ID, "entangled_bag"),  ModItem(EntangledBag(Settings().maxCount(1).rarity(Rarity.RARE)), EntangledBagBakedModel(), EntangledBagContainer::class, EntangledBagScreen::class))
    else
        register(Identifier(MOD_ID, "entangled_bag"),  ModItem(EntangledBag(Settings().maxCount(1).rarity(Rarity.RARE)), EntangledBagContainer::class))


val SLEEPING_BAG = register(Identifier(MOD_ID, "sleeping_bag"), ModItem(SleepingBag(Settings().rarity(Rarity.RARE))))

private fun register(identifier: Identifier, item: ModItem): Item {
    itemRegistry[identifier] = item
    return item.item
}

fun getItemId(item: Item): Identifier? {
    itemRegistry.forEach {
        if(it.value.item == item) return it.key
    }
    return null
}

fun initItems() {
    itemRegistry.forEach{ it.value.init(it.key) }
}

fun initItemsClient() {
    itemRegistry.forEach{ it.value.initClient(it.key) }
}
