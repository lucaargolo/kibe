@file:Suppress("UNCHECKED_CAST", "unused")

package io.github.lucaargolo.kibe.items

import io.github.ladysnake.pal.VanillaAbilities
import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.client.items.EntangledBagBakedModel
import io.github.lucaargolo.kibe.client.items.EntangledBucketBakedModel
import io.github.lucaargolo.kibe.client.items.TankBlockItemBakedModel
import io.github.lucaargolo.kibe.client.screens.CoolerBlockItemScreen
import io.github.lucaargolo.kibe.client.screens.EntangledBagScreen
import io.github.lucaargolo.kibe.client.screens.PocketTrashCanScreen
import io.github.lucaargolo.kibe.screenhandlers.CoolerBlockItemScreenHandler
import io.github.lucaargolo.kibe.screenhandlers.EntangledBagScreenHandler
import io.github.lucaargolo.kibe.screenhandlers.PocketTrashCanScreenHandler
import io.github.lucaargolo.kibe.utils.INFINITE_FIRE_RESISTENCE
import io.github.lucaargolo.kibe.utils.INFINITE_WATER_BREATHING
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.item.ClampedModelPredicateProvider
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.Baker
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.FoodComponent
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import java.util.function.Function
import java.util.function.Supplier
import kotlin.reflect.KClass

class ContainerInfo<T: ScreenHandler>(
    handlerClass: KClass<*>,
    screenClass: Supplier<KClass<*>>,
    val identifier: Identifier? = null
){

    val handlerClass = handlerClass as KClass<T>
    val screenClass = screenClass as Supplier<KClass<HandledScreen<T>>>

    var handlerType: ScreenHandlerType<T>? = null
    var handler: T? = null

    var title: Text = Text.literal("")

    fun init(itemIdentifier: Identifier) {
        val id = identifier ?: itemIdentifier
        title = Text.translatable("screen.${KibeMod.MOD_ID}.${id.path}")
        handlerType = ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
            val hand = packetByteBuf.readEnumConstant(Hand::class.java)
            val tag = packetByteBuf.readNbt()!!
            handler = handlerClass.java.constructors[0].newInstance(i, playerInventory, hand, playerInventory.player.world, tag) as T
            handler
        }
        Registry.register(Registries.SCREEN_HANDLER, id, handlerType)
    }

    fun initClient() {
        HandledScreens.register(handlerType) { handler, playerInventory, title -> screenClass.get().java.constructors[0].newInstance(handler, playerInventory, title) as HandledScreen<T> }
    }

}

class ItemInfo (
    val identifier: Identifier,
    val item: Item,
    private val bakedModel: (() -> BakedModel)?,
    var containers: List<ContainerInfo<*>>,
    private val modelPredicateProviders: (() -> List<Pair<Identifier, ClampedModelPredicateProvider>>?)?
){

    fun init() {
        Registry.register(Registries.ITEM, identifier, item)
        containers.forEach { it.init(identifier) }
    }

    fun initClient() {
        containers.forEach { it.initClient() }
        modelPredicateProviders?.invoke()?.let { providers ->
            for (provider in providers) {
                ModelPredicateProviderRegistry.register(item, provider.first, provider.second)
            }
        }
        if(bakedModel != null) {
            ModelLoadingRegistry.INSTANCE.registerVariantProvider {
                ModelVariantProvider { modelIdentifier, _ ->
                    if(modelIdentifier.namespace == identifier.namespace && modelIdentifier.path == identifier.path && modelIdentifier.variant == "inventory") {
                        return@ModelVariantProvider object : UnbakedModel {
                            override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
                            override fun setParents(modelLoader: Function<Identifier, UnbakedModel>?) {}
                            override fun bake(baker: Baker?, textureGetter: Function<SpriteIdentifier, Sprite>?, rotationContainer: ModelBakeSettings?, modelId: Identifier?) = bakedModel.invoke()
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

val KIBE         = register(ModIdentifier("kibe"), Item(Settings().rarity(Rarity.COMMON).food(FoodComponent.Builder().hunger(6).saturationModifier(0.8F).meat().build())))
val GOLDEN_KIBE  = register(ModIdentifier("golden_kibe"), Item(Settings().rarity(Rarity.UNCOMMON).food(FoodComponent.Builder().hunger(8).saturationModifier(1.2F).meat().build())))
val CURSED_KIBE  = register(ModIdentifier("cursed_kibe"), Item(Settings().rarity(Rarity.UNCOMMON).food(FoodComponent.Builder().hunger(10).saturationModifier(1.2F).meat().build())))
val DIAMOND_KIBE = register(ModIdentifier("diamond_kibe"), Item(Settings().rarity(Rarity.RARE).food(FoodComponent.Builder().hunger(16).saturationModifier(1F).meat().build())))

val CURSED_DROPLETS = register(ModIdentifier("cursed_droplets"), Item(Settings()))
val CURSED_SEEDS    = register(ModIdentifier("cursed_seeds"), CursedSeeds(Settings()))

val MAGNET = register(ModIdentifier("magnet"), Magnet.create(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))

val DIAMOND_RING = register(ModIdentifier("diamond_ring"),  Item(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
val ANGEL_RING = register(ModIdentifier("angel_ring"), AbilityRing.create(Settings().maxCount(1).rarity(Rarity.EPIC), VanillaAbilities.ALLOW_FLYING))
val MAGMA_RING = register(ModIdentifier("magma_ring"), AbilityRing.create(Settings().maxCount(1).rarity(Rarity.RARE), INFINITE_FIRE_RESISTENCE))
val WATER_RING = register(ModIdentifier("water_ring"), AbilityRing.create(Settings().maxCount(1).rarity(Rarity.RARE), INFINITE_WATER_BREATHING))
val LIGHT_RING   = register(ModIdentifier("light_ring"),  LightRing(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))

val GOLDEN_LASSO  = register(ModIdentifier("golden_lasso"),  Lasso.GoldenLasso(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
val CURSED_LASSO  = register(ModIdentifier("cursed_lasso"),  Lasso.CursedLasso(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
val DIAMOND_LASSO = register(ModIdentifier("diamond_lasso"),  Lasso.DiamondLasso(Settings().maxCount(1).rarity(Rarity.RARE)))

val WHITE_RUNE      = register(ModIdentifier("white_rune"),  Rune(DyeColor.WHITE, Settings()))
val ORANGE_RUNE     = register(ModIdentifier("orange_rune"),  Rune(DyeColor.ORANGE, Settings()))
val MAGENTA_RUNE    = register(ModIdentifier("magenta_rune"),  Rune(DyeColor.MAGENTA, Settings()))
val LIGHT_BLUE_RUNE = register(ModIdentifier("light_blue_rune"),  Rune(DyeColor.LIGHT_BLUE, Settings()))
val YELLOW_RUNE     = register(ModIdentifier("yellow_rune"),  Rune(DyeColor.YELLOW, Settings()))
val LIME_RUNE       = register(ModIdentifier("lime_rune"),  Rune(DyeColor.LIME, Settings()))
val PINK_RUNE       = register(ModIdentifier("pink_rune"),  Rune(DyeColor.PINK, Settings()))
val GRAY_RUNE       = register(ModIdentifier("gray_rune"),  Rune(DyeColor.GRAY, Settings()))
val LIGHT_GRAY_RUNE = register(ModIdentifier("light_gray_rune"),  Rune(DyeColor.LIGHT_GRAY, Settings()))
val CYAN_RUNE       = register(ModIdentifier("cyan_rune"),  Rune(DyeColor.CYAN, Settings()))
val BLUE_RUNE       = register(ModIdentifier("blue_rune"),  Rune(DyeColor.BLUE, Settings()))
val PURPLE_RUNE     = register(ModIdentifier("purple_rune"),  Rune(DyeColor.PURPLE, Settings()))
val GREEN_RUNE      = register(ModIdentifier("green_rune"),  Rune(DyeColor.GREEN, Settings()))
val BROWN_RUNE      = register(ModIdentifier("brown_rune"),  Rune(DyeColor.BROWN, Settings()))
val RED_RUNE        = register(ModIdentifier("red_rune"),  Rune(DyeColor.RED, Settings()))
val BLACK_RUNE      = register(ModIdentifier("black_rune"),  Rune(DyeColor.BLACK, Settings()))

val SLIME_BOOTS = register(ModIdentifier("slime_boots"),  SlimeBoots(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
val SLIME_SLING = register(ModIdentifier("slime_sling"),  SlimeSling(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))

val TORCH_SLING = register(ModIdentifier("torch_sling"),  TorchSling(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
val ESCAPE_ROPE = register(ModIdentifier("escape_rope"),  EscapeRope(Settings().maxCount(1).rarity(Rarity.UNCOMMON)))

val WOODEN_BUCKET = register(ModIdentifier("wooden_bucket"), WoodenBucket.Empty(Settings().maxCount(16)))
val WATER_WOODEN_BUCKET = register(ModIdentifier("water_wooden_bucket"), WoodenBucket.Water(Settings().maxCount(1)))

val GLIDER_LEFT_WING = register(ModIdentifier("glider_left_wing"), Item(Settings()))
val GLIDER_RIGHT_WING = register(ModIdentifier("glider_right_wing"), Item(Settings()))

val WHITE_GLIDER = register(ModIdentifier("white_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val ORANGE_GLIDER = register(ModIdentifier("orange_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val MAGENTA_GLIDER = register(ModIdentifier("magenta_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val LIGHT_BLUE_GLIDER = register(ModIdentifier("light_blue_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val YELLOW_GLIDER = register(ModIdentifier("yellow_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val LIME_GLIDER = register(ModIdentifier("lime_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val PINK_GLIDER = register(ModIdentifier("pink_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val GRAY_GLIDER = register(ModIdentifier("gray_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val LIGHT_GRAY_GLIDER = register(ModIdentifier("light_gray_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val CYAN_GLIDER = register(ModIdentifier("cyan_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val BLUE_GLIDER = register(ModIdentifier("blue_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val PURPLE_GLIDER = register(ModIdentifier("purple_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val GREEN_GLIDER = register(ModIdentifier("green_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val BROWN_GLIDER = register(ModIdentifier("brown_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val RED_GLIDER = register(ModIdentifier("red_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))
val BLACK_GLIDER = register(ModIdentifier("black_glider"), Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.MOD_CONFIG.miscellaneousModule.gliderDurability)))

val VOID_BUCKET = register(ModIdentifier("void_bucket"), VoidBucket(Settings().maxCount(1).rarity(Rarity.RARE)))

val POCKET_CRAFTING_TABLE = register(ModIdentifier("pocket_crafting_table"),  PocketCraftingTable(Settings().maxCount(1)))
val POCKET_TRASH_CAN = register(ModIdentifier("pocket_trash_can"),  PocketTrashCan(Settings().maxCount(1)), containers = listOf(ContainerInfo<PocketTrashCanScreenHandler>(
    PocketTrashCanScreenHandler::class, { PocketTrashCanScreen::class })))

val ENTANGLED_CHEST = register(ModIdentifier("entangled_chest"), EntangledChestBlockItem(Settings()))
val ENTANGLED_TANK = register(ModIdentifier("entangled_tank"), EntangledTankBlockItem(Settings()))
val ENTANGLED_BAG = register(ModIdentifier("entangled_bag"),  EntangledBag(Settings().maxCount(1).rarity(Rarity.RARE)), { EntangledBagBakedModel() }, listOf(ContainerInfo<EntangledBagScreenHandler>(
    EntangledBagScreenHandler::class, { EntangledBagScreen::class })))
val ENTANGLED_BUCKET = register(ModIdentifier("entangled_bucket"),  EntangledBucket(Settings().maxCount(1).rarity(Rarity.RARE)), { EntangledBucketBakedModel() })
val COOLER = register(ModIdentifier("cooler"), CoolerBlockItem(Settings().maxCount(1).rarity(Rarity.UNCOMMON)), containers = listOf(ContainerInfo<CoolerBlockItemScreenHandler>(
    CoolerBlockItemScreenHandler::class, { CoolerBlockItemScreen::class }, identifier = ModIdentifier("cooler_item"))))
val TANK = register(ModIdentifier("tank"), TankBlockItem(Settings()), { TankBlockItemBakedModel() } )

val WHITE_SLEEPING_BAG = register(ModIdentifier("white_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val ORANGE_SLEEPING_BAG = register(ModIdentifier("orange_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val MAGENTA_SLEEPING_BAG = register(ModIdentifier("magenta_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val LIGHT_BLUE_SLEEPING_BAG = register(ModIdentifier("light_blue_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val YELLOW_SLEEPING_BAG = register(ModIdentifier("yellow_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val LIME_SLEEPING_BAG = register(ModIdentifier("lime_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val PINK_SLEEPING_BAG = register(ModIdentifier("pink_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val GRAY_SLEEPING_BAG = register(ModIdentifier("gray_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val LIGHT_GRAY_SLEEPING_BAG = register(ModIdentifier("light_gray_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val CYAN_SLEEPING_BAG = register(ModIdentifier("cyan_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val BLUE_SLEEPING_BAG = register(ModIdentifier("sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val PURPLE_SLEEPING_BAG = register(ModIdentifier("purple_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val GREEN_SLEEPING_BAG = register(ModIdentifier("green_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val BROWN_SLEEPING_BAG = register(ModIdentifier("brown_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val RED_SLEEPING_BAG = register(ModIdentifier("red_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))
val BLACK_SLEEPING_BAG = register(ModIdentifier("black_sleeping_bag"), SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)))

val MEASURING_TAPE = register(ModIdentifier("measuring_tape"), MeasuringTape(Settings().maxCount(1)), modelPredicateProviders = { listOf(Pair(ModIdentifier("extended"), MeasuringTape.PredicateProvider())) })

fun register(identifier: Identifier, item: Item, bakedModel: (() -> BakedModel)? = null, containers: List<ContainerInfo<*>> = listOf(), modelPredicateProviders: (() -> List<Pair<Identifier, ClampedModelPredicateProvider>>?)? = null): Item {
    val info = ItemInfo(identifier, item, bakedModel, containers, modelPredicateProviders)
    itemRegistry[item] = info
    return item
}

fun initItems() {
    itemRegistry.forEach{ it.value.init() }
}

fun initItemsClient() {
    itemRegistry.forEach{ it.value.initClient() }
}
