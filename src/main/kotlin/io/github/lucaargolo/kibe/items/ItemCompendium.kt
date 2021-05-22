@file:Suppress("UNCHECKED_CAST", "unused")

package io.github.lucaargolo.kibe.items

import io.github.ladysnake.pal.VanillaAbilities
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.items.entangledbag.EntangledBag
import io.github.lucaargolo.kibe.items.entangledbag.EntangledBagBakedModel
import io.github.lucaargolo.kibe.items.entangledbag.EntangledBagScreenHandler
import io.github.lucaargolo.kibe.items.entangledbag.EntangledBagScreen
import io.github.lucaargolo.kibe.items.miscellaneous.*
import io.github.lucaargolo.kibe.items.trashcan.PocketTrashCan
import io.github.lucaargolo.kibe.items.trashcan.PocketTrashCanScreenHandler
import io.github.lucaargolo.kibe.items.trashcan.PocketTrashCanScreen
import io.github.lucaargolo.kibe.utils.INFINITE_FIRE_RESISTENCE
import io.github.lucaargolo.kibe.utils.INFINITE_WATER_BREATHING
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.FoodComponent
import net.minecraft.item.Item.*
import net.minecraft.item.Item
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import net.minecraft.util.registry.Registry
import java.util.function.Supplier
import java.util.function.Function
import kotlin.reflect.KClass
import com.mojang.datafixers.util.Pair
import io.github.lucaargolo.kibe.TRINKET
import io.github.lucaargolo.kibe.compat.TrinketRing
import io.github.lucaargolo.kibe.items.entangledtank.EntangledTankBlockItem
import io.github.lucaargolo.kibe.items.cooler.CoolerBlockItem
import io.github.lucaargolo.kibe.items.cooler.CoolerBlockItemScreen
import io.github.lucaargolo.kibe.items.cooler.CoolerBlockItemScreenHandler
import io.github.lucaargolo.kibe.items.entangledbucket.EntangledBucket
import io.github.lucaargolo.kibe.items.entangledbucket.EntangledBucketBakedModel
import io.github.lucaargolo.kibe.items.entangledchest.EntangledChestBlockItem
import io.github.lucaargolo.kibe.items.tank.TankBlockItem
import io.github.lucaargolo.kibe.items.tank.TankBlockItemBakedModel
import io.github.lucaargolo.kibe.utils.CREATIVE_TAB
import net.minecraft.util.Hand

class ContainerInfo<T: ScreenHandler>(
    handlerClass: KClass<*>,
    screenClass: Supplier<KClass<*>>,
    val identifier: Identifier? = null
){

    val handlerClass = handlerClass as KClass<T>
    val screenClass = screenClass as Supplier<KClass<HandledScreen<T>>>

    var handlerType: ScreenHandlerType<T>? = null
    var handler: T? = null

    var title: Text = LiteralText("")

    fun init(itemIdentifier: Identifier) {
        val id = identifier ?: itemIdentifier
        title = TranslatableText("screen.$MOD_ID.${id.path}")
        handlerType = ScreenHandlerRegistry.registerExtended(id) { i, playerInventory, packetByteBuf ->
            val hand = packetByteBuf.readEnumConstant(Hand::class.java)
            val tag = packetByteBuf.readCompoundTag()!!
            handler = handlerClass.java.constructors[0].newInstance(i, playerInventory, hand, playerInventory.player.world, tag) as T
            handler
        }
    }

    fun initClient() {
        ScreenRegistry.register(handlerType) { handler, playerInventory, title -> screenClass.get().java.constructors[0].newInstance(handler, playerInventory, title) as HandledScreen<T> }
    }

}

class ItemInfo (
    val identifier: Identifier,
    val item: Item,
    private val bakedModel: Supplier<BakedModel>?,
    var containers: List<ContainerInfo<*>>
){

    fun init() {
        Registry.register(Registry.ITEM, identifier, item)
        containers.forEach { it.init(identifier) }
    }

    fun initClient() {
        containers.forEach { it.initClient() }
        if(bakedModel != null) {
            ModelLoadingRegistry.INSTANCE.registerVariantProvider {
                ModelVariantProvider { modelIdentifier, _ ->
                    if(modelIdentifier.namespace == identifier.namespace && modelIdentifier.path == identifier.path && modelIdentifier.variant == "inventory") {
                        return@ModelVariantProvider object : UnbakedModel {
                            override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
                            override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationScreenHandler: ModelBakeSettings, modelId: Identifier) = bakedModel.get()
                            override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>?, unresolvedTextureReferences: MutableSet<Pair<String, String>>?): MutableCollection<SpriteIdentifier> = mutableListOf()
                        }
                    }
                    return@ModelVariantProvider null
                }
            }
        }
    }

}

val itemRegistry = mutableMapOf<Item, ItemInfo>()

fun getItemId(item: Item) = itemRegistry[item]?.identifier
fun getContainerInfo(item: Item) = itemRegistry[item]?.containers?.get(0)
fun getContainerInfo(item: Item, identifier: Identifier): ContainerInfo<*>? {
    itemRegistry[item]?.containers?.forEach {
        if(it.identifier == identifier)
            return it
    }
    return null
}

val KIBE         = register(Identifier(MOD_ID, "kibe"), Item(settingsWithTab().rarity(Rarity.COMMON).food(FoodComponent.Builder().hunger(6).saturationModifier(0.8F).meat().build())))
val GOLDEN_KIBE  = register(Identifier(MOD_ID, "golden_kibe"), Item(settingsWithTab().rarity(Rarity.UNCOMMON).food(FoodComponent.Builder().hunger(8).saturationModifier(1.2F).meat().build())))
val CURSED_KIBE  = register(Identifier(MOD_ID, "cursed_kibe"), Item(settingsWithTab().rarity(Rarity.UNCOMMON).food(FoodComponent.Builder().hunger(10).saturationModifier(1.2F).meat().build())))
val DIAMOND_KIBE = register(Identifier(MOD_ID, "diamond_kibe"), Item(settingsWithTab().rarity(Rarity.RARE).food(FoodComponent.Builder().hunger(16).saturationModifier(1F).meat().build())))

val CURSED_DROPLETS = register(Identifier(MOD_ID, "cursed_droplets"), Item(settingsWithTab()))
val CURSED_SEEDS    = register(Identifier(MOD_ID, "cursed_seeds"), CursedSeeds(settingsWithTab()))

val MAGNET = register(Identifier(MOD_ID, "magnet"), Magnet(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))

val DIAMOND_RING = register(Identifier(MOD_ID, "diamond_ring"),  Item(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val ANGEL_RING   = register(Identifier(MOD_ID, "angel_ring"),  if(TRINKET) TrinketRing(settingsWithTab().maxCount(1).rarity(Rarity.EPIC), VanillaAbilities.ALLOW_FLYING) else AbilityRing(settingsWithTab().maxCount(1).rarity(Rarity.EPIC), VanillaAbilities.ALLOW_FLYING))
val MAGMA_RING   = register(Identifier(MOD_ID, "magma_ring"),  if(TRINKET) TrinketRing(settingsWithTab().maxCount(1).rarity(Rarity.RARE), INFINITE_FIRE_RESISTENCE) else AbilityRing(settingsWithTab().maxCount(1).rarity(Rarity.RARE), INFINITE_FIRE_RESISTENCE))
val WATER_RING   = register(Identifier(MOD_ID, "water_ring"),  if(TRINKET) TrinketRing(settingsWithTab().maxCount(1).rarity(Rarity.RARE), INFINITE_WATER_BREATHING) else AbilityRing(settingsWithTab().maxCount(1).rarity(Rarity.RARE), INFINITE_WATER_BREATHING))
val LIGHT_RING   = register(Identifier(MOD_ID, "light_ring"),  LightRing(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))

val GOLDEN_LASSO  = register(Identifier(MOD_ID, "golden_lasso"),  Lasso.GoldenLasso(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val CURSED_LASSO  = register(Identifier(MOD_ID, "cursed_lasso"),  Lasso.CursedLasso(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val DIAMOND_LASSO = register(Identifier(MOD_ID, "diamond_lasso"),  Lasso.DiamondLasso(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))

val WHITE_RUNE      = register(Identifier(MOD_ID, "white_rune"),  Rune(DyeColor.WHITE, settingsWithTab()))
val ORANGE_RUNE     = register(Identifier(MOD_ID, "orange_rune"),  Rune(DyeColor.ORANGE, settingsWithTab()))
val MAGENTA_RUNE    = register(Identifier(MOD_ID, "magenta_rune"),  Rune(DyeColor.MAGENTA, settingsWithTab()))
val LIGHT_BLUE_RUNE = register(Identifier(MOD_ID, "light_blue_rune"),  Rune(DyeColor.LIGHT_BLUE, settingsWithTab()))
val YELLOW_RUNE     = register(Identifier(MOD_ID, "yellow_rune"),  Rune(DyeColor.YELLOW, settingsWithTab()))
val LIME_RUNE       = register(Identifier(MOD_ID, "lime_rune"),  Rune(DyeColor.LIME, settingsWithTab()))
val PINK_RUNE       = register(Identifier(MOD_ID, "pink_rune"),  Rune(DyeColor.PINK, settingsWithTab()))
val GRAY_RUNE       = register(Identifier(MOD_ID, "gray_rune"),  Rune(DyeColor.GRAY, settingsWithTab()))
val LIGHT_GRAY_RUNE = register(Identifier(MOD_ID, "light_gray_rune"),  Rune(DyeColor.LIGHT_GRAY, settingsWithTab()))
val CYAN_RUNE       = register(Identifier(MOD_ID, "cyan_rune"),  Rune(DyeColor.CYAN, settingsWithTab()))
val BLUE_RUNE       = register(Identifier(MOD_ID, "blue_rune"),  Rune(DyeColor.BLUE, settingsWithTab()))
val PURPLE_RUNE     = register(Identifier(MOD_ID, "purple_rune"),  Rune(DyeColor.PURPLE, settingsWithTab()))
val GREEN_RUNE      = register(Identifier(MOD_ID, "green_rune"),  Rune(DyeColor.GREEN, settingsWithTab()))
val BROWN_RUNE      = register(Identifier(MOD_ID, "brown_rune"),  Rune(DyeColor.BROWN, settingsWithTab()))
val RED_RUNE        = register(Identifier(MOD_ID, "red_rune"),  Rune(DyeColor.RED, settingsWithTab()))
val BLACK_RUNE      = register(Identifier(MOD_ID, "black_rune"),  Rune(DyeColor.BLACK, settingsWithTab()))

val SLIME_BOOTS = register(Identifier(MOD_ID, "slime_boots"),  SlimeBoots(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val SLIME_SLING = register(Identifier(MOD_ID, "slime_sling"),  SlimeSling(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))

val TORCH_SLING = register(Identifier(MOD_ID, "torch_sling"),  TorchSling(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val ESCAPE_ROPE = register(Identifier(MOD_ID, "escape_rope"),  EscapeRope(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))

val WOODEN_BUCKET = register(Identifier(MOD_ID, "wooden_bucket"), WoodenBucket.Empty(settingsWithTab().maxCount(16)))
val WATER_WOODEN_BUCKET = register(Identifier(MOD_ID, "water_wooden_bucket"), WoodenBucket.Water(settingsWithTab().maxCount(1)))

val GLIDER_LEFT_WING = register(Identifier(MOD_ID, "glider_left_wing"), Item(settingsWithTab()))
val GLIDER_RIGHT_WING = register(Identifier(MOD_ID, "glider_right_wing"), Item(settingsWithTab()))

val WHITE_GLIDER = register(Identifier(MOD_ID, "white_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val ORANGE_GLIDER = register(Identifier(MOD_ID, "orange_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val MAGENTA_GLIDER = register(Identifier(MOD_ID, "magenta_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val LIGHT_BLUE_GLIDER = register(Identifier(MOD_ID, "light_blue_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val YELLOW_GLIDER = register(Identifier(MOD_ID, "yellow_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val LIME_GLIDER = register(Identifier(MOD_ID, "lime_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val PINK_GLIDER = register(Identifier(MOD_ID, "pink_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val GRAY_GLIDER = register(Identifier(MOD_ID, "gray_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val LIGHT_GRAY_GLIDER = register(Identifier(MOD_ID, "light_gray_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val CYAN_GLIDER = register(Identifier(MOD_ID, "cyan_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val BLUE_GLIDER = register(Identifier(MOD_ID, "blue_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val PURPLE_GLIDER = register(Identifier(MOD_ID, "purple_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val GREEN_GLIDER = register(Identifier(MOD_ID, "green_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val BROWN_GLIDER = register(Identifier(MOD_ID, "brown_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val RED_GLIDER = register(Identifier(MOD_ID, "red_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))
val BLACK_GLIDER = register(Identifier(MOD_ID, "black_glider"), Glider(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)))

val VOID_BUCKET = register(Identifier(MOD_ID, "void_bucket"), VoidBucket(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))

val POCKET_CRAFTING_TABLE = register(Identifier(MOD_ID, "pocket_crafting_table"),  PocketCraftingTable(settingsWithTab().maxCount(1)))
val POCKET_TRASH_CAN = register(Identifier(MOD_ID, "pocket_trash_can"),  PocketTrashCan(settingsWithTab().maxCount(1)), containers = listOf(ContainerInfo<PocketTrashCanScreenHandler>(PocketTrashCanScreenHandler::class, { PocketTrashCanScreen::class })))

val ENTANGLED_CHEST = register(Identifier(MOD_ID, "entangled_chest"), EntangledChestBlockItem(settingsWithTab()))
val ENTANGLED_TANK = register(Identifier(MOD_ID, "entangled_tank"), EntangledTankBlockItem(settingsWithTab()))
val ENTANGLED_BAG = register(Identifier(MOD_ID, "entangled_bag"),  EntangledBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)), { EntangledBagBakedModel() }, listOf(ContainerInfo<EntangledBagScreenHandler>(EntangledBagScreenHandler::class, { EntangledBagScreen::class })))
val ENTANGLED_BUCKET = register(Identifier(MOD_ID, "entangled_bucket"),  EntangledBucket(settingsWithTab().maxCount(1).rarity(Rarity.RARE)), { EntangledBucketBakedModel() })
val COOLER = register(Identifier(MOD_ID, "cooler"), CoolerBlockItem(settingsWithTab().maxCount(1).rarity(Rarity.UNCOMMON)), containers = listOf(ContainerInfo<CoolerBlockItemScreenHandler>(CoolerBlockItemScreenHandler::class, { CoolerBlockItemScreen::class }, identifier = Identifier(MOD_ID, "cooler_item"))))
val TANK = register(Identifier(MOD_ID, "tank"), TankBlockItem(settingsWithTab()), { TankBlockItemBakedModel() } )

val WHITE_SLEEPING_BAG = register(Identifier(MOD_ID, "white_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val ORANGE_SLEEPING_BAG = register(Identifier(MOD_ID, "orange_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val MAGENTA_SLEEPING_BAG = register(Identifier(MOD_ID, "magenta_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val LIGHT_BLUE_SLEEPING_BAG = register(Identifier(MOD_ID, "light_blue_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val YELLOW_SLEEPING_BAG = register(Identifier(MOD_ID, "yellow_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val LIME_SLEEPING_BAG = register(Identifier(MOD_ID, "lime_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val PINK_SLEEPING_BAG = register(Identifier(MOD_ID, "pink_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val GRAY_SLEEPING_BAG = register(Identifier(MOD_ID, "gray_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val LIGHT_GRAY_SLEEPING_BAG = register(Identifier(MOD_ID, "light_gray_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val CYAN_SLEEPING_BAG = register(Identifier(MOD_ID, "cyan_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val BLUE_SLEEPING_BAG = register(Identifier(MOD_ID, "sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val PURPLE_SLEEPING_BAG = register(Identifier(MOD_ID, "purple_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val GREEN_SLEEPING_BAG = register(Identifier(MOD_ID, "green_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val BROWN_SLEEPING_BAG = register(Identifier(MOD_ID, "brown_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val RED_SLEEPING_BAG = register(Identifier(MOD_ID, "red_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))
val BLACK_SLEEPING_BAG = register(Identifier(MOD_ID, "black_sleeping_bag"), SleepingBag(settingsWithTab().maxCount(1).rarity(Rarity.RARE)))

private fun settingsWithTab() = Settings().group(CREATIVE_TAB)

fun register(identifier: Identifier, item: Item, bakedModel: Supplier<BakedModel>? = null, containers: List<ContainerInfo<*>> = listOf()): Item {
    val info = ItemInfo(identifier, item, bakedModel, containers)
    itemRegistry[item] = info
    return item
}

fun initItems() {
    itemRegistry.forEach{ it.value.init() }
}

fun initItemsClient() {
    itemRegistry.forEach{ it.value.initClient() }
}
