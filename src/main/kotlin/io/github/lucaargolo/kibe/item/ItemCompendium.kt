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
import io.github.lucaargolo.kibe.client.model.SlimyBootsModel
import io.github.lucaargolo.kibe.utils.ModIdentifier
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import io.github.lucaargolo.kibe.utils.helper.AbilityHelper
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.LeveledCauldronBlock
import net.minecraft.block.cauldron.CauldronBehavior
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.component.type.FoodComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.item.*
import net.minecraft.item.Item.Settings
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.ItemActionResult
import net.minecraft.util.Rarity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent
import net.neoforged.neoforge.registries.DeferredHolder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Predicate

object ItemCompendium: RegistryCompendium<Item>(Registries.ITEM) {

    val FLUID_BUCKETS: Map<Fluid, BucketItem>
        get() = fluidBuckets.mapKeys { e -> e.key.get() }.mapValues { e -> e.value.get() }
    private val fluidBuckets = mutableMapOf<DeferredHolder<Fluid, out Fluid>, DeferredHolder<Item, BucketItem>>()

    private val AS_ITEM = mutableMapOf<DeferredHolder<Block, out Block>, String>()

    val ELEVATORS by registerTag("elevators", *BlockCompendium.ELEVATORS.values.map{ AS_ITEM[it]!! }.toTypedArray())

    val KIBE by register("kibe") { Item(Settings().rarity(Rarity.COMMON).food(FoodComponent.Builder().nutrition(6).saturationModifier(0.8F).build())) }
    val GOLDEN_KIBE by register("golden_kibe", { Item(Settings().rarity(Rarity.UNCOMMON).food(FoodComponent.Builder().nutrition(8).saturationModifier(1.2F).build())) }, ItemTags.PIGLIN_LOVED)
    val CURSED_KIBE by register("cursed_kibe") { Item(Settings().rarity(Rarity.UNCOMMON).food(FoodComponent.Builder().nutrition(10).saturationModifier(1.2F).build())) }
    val DIAMOND_KIBE by register("diamond_kibe") { Item(Settings().rarity(Rarity.RARE).food(FoodComponent.Builder().nutrition(16).saturationModifier(1F).build())) }

    val CURSED_DROPLETS by register("cursed_droplets") { Item(Settings()) }
    val CURSED_SEEDS by register("cursed_seeds") { CursedSeeds(Settings()) }

    val MAGNET by register("magnet") { Magnet.create(Settings().maxCount(1).rarity(Rarity.UNCOMMON)) }

    val DIAMOND_RING by register("diamond_ring") { Item(Settings().maxCount(1).rarity(Rarity.UNCOMMON)) }
    val ANGEL_RING by register("angel_ring") { AbilityRing.create(Settings().maxCount(1).rarity(Rarity.EPIC), VanillaAbilities.ALLOW_FLYING) }
    val MAGMA_RING by register("magma_ring") { AbilityRing.create(Settings().maxCount(1).rarity(Rarity.RARE), AbilityHelper.INFINITE_FIRE_RESISTENCE) }
    val WATER_RING by register("water_ring") { AbilityRing.create(Settings().maxCount(1).rarity(Rarity.RARE), AbilityHelper.INFINITE_WATER_BREATHING) }
    val LIGHT_RING by register("light_ring") { LightRing(Settings().maxCount(1).rarity(Rarity.UNCOMMON)) }

    val GOLDEN_LASSO by register("golden_lasso", { Lasso.GoldenLasso(Settings().maxCount(1).rarity(Rarity.UNCOMMON)) }, ItemTags.PIGLIN_LOVED)
    val CURSED_LASSO by register("cursed_lasso") { Lasso.CursedLasso(Settings().maxCount(1).rarity(Rarity.UNCOMMON)) }
    val DIAMOND_LASSO by register("diamond_lasso") { Lasso.DiamondLasso(Settings().maxCount(1).rarity(Rarity.RARE)) }

    val SLIME_BOOTS by register("slime_boots", { SlimeBoots(Settings().maxDamage(128).maxCount(1).rarity(Rarity.UNCOMMON)) }, ItemTags.FOOT_ARMOR)
    val SLIME_SLING by register("slime_sling") { SlimeSling(Settings().maxDamage(128).maxCount(1).rarity(Rarity.UNCOMMON)) }

    val TORCH_SLING by register("torch_sling") { TorchSling(Settings().maxCount(1).rarity(Rarity.UNCOMMON)) }
    val ESCAPE_ROPE by register("escape_rope", { EscapeRope(Settings().maxCount(1).rarity(Rarity.UNCOMMON)) }, ItemTags.DURABILITY_ENCHANTABLE)
    
    val WOODEN_BUCKET by register("wooden_bucket") { WoodenBucket.Empty(Settings().maxCount(16)) }
    val WOODEN_WATER_BUCKET by register("wooden_water_bucket", { WoodenBucket.Water(Settings().maxCount(1).recipeRemainder(WOODEN_BUCKET)) }, ConventionalItemTags.WATER_BUCKETS)
    
    val GLIDER_LEFT_WING by register("glider_left_wing") { Item(Settings()) }
    val GLIDER_RIGHT_WING by register("glider_right_wing") { Item(Settings()) }

    val WHITE_GLIDER by register("white_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val ORANGE_GLIDER by register("orange_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val MAGENTA_GLIDER by register("magenta_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val LIGHT_BLUE_GLIDER by register("light_blue_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val YELLOW_GLIDER by register("yellow_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val LIME_GLIDER by register("lime_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val PINK_GLIDER by register("pink_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val GRAY_GLIDER by register("gray_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val LIGHT_GRAY_GLIDER by register("light_gray_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val CYAN_GLIDER by register("cyan_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val BLUE_GLIDER by register("blue_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val PURPLE_GLIDER by register("purple_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val GREEN_GLIDER by register("green_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val BROWN_GLIDER by register("brown_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val RED_GLIDER by register("red_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val BLACK_GLIDER by register("black_glider") { Glider(Settings().maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(KibeMod.CONFIG.miscellaneousModule.gliderDurability)) }
    val GLIDERS by registerAssociatedTag("gliders",
        DyeColor.WHITE to "white_glider", DyeColor.ORANGE to "orange_glider", DyeColor.MAGENTA to "magenta_glider", DyeColor.LIGHT_BLUE to "light_blue_glider",
        DyeColor.YELLOW to "yellow_glider", DyeColor.LIME to "lime_glider", DyeColor.PINK to "pink_glider", DyeColor.GRAY to "gray_glider",
        DyeColor.LIGHT_GRAY to "light_gray_glider", DyeColor.CYAN to "cyan_glider", DyeColor.BLUE to "blue_glider", DyeColor.PURPLE to "purple_glider",
        DyeColor.GREEN to "green_glider", DyeColor.BROWN to "brown_glider", DyeColor.RED to "red_glider", DyeColor.BLACK to "black_glider"
    )

    val VOID_BUCKET by register("void_bucket") { VoidBucket(Settings().maxCount(1).rarity(Rarity.RARE)) }

    val POCKET_CRAFTING_TABLE by register("pocket_crafting_table") { PocketCraftingTable(Settings().maxCount(1)) }
    val POCKET_TRASH_CAN by register("pocket_trash_can") { PocketTrashCan(Settings().maxCount(1)) }

    val ENTANGLED_CHEST by register("entangled_chest") { EntangledChestBlockItem(Settings()) }
    val ENTANGLED_TANK by register("entangled_tank") { EntangledTankBlockItem(Settings()) }
    val ENTANGLED_BAG by register("entangled_bag") { EntangledBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val ENTANGLED_BUCKET by register("entangled_bucket") { EntangledBucket(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val COOLER by register("cooler") { CoolerBlockItem.create(Settings().maxCount(1).rarity(Rarity.UNCOMMON)) }
    val TANK by register("tank") { TankBlockItem(Settings()) }

    val WHITE_SLEEPING_BAG by register("white_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val ORANGE_SLEEPING_BAG by register("orange_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val MAGENTA_SLEEPING_BAG by register("magenta_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val LIGHT_BLUE_SLEEPING_BAG by register("light_blue_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val YELLOW_SLEEPING_BAG by register("yellow_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val LIME_SLEEPING_BAG by register("lime_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val PINK_SLEEPING_BAG by register("pink_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val GRAY_SLEEPING_BAG by register("gray_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val LIGHT_GRAY_SLEEPING_BAG by register("light_gray_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val CYAN_SLEEPING_BAG by register("cyan_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val BLUE_SLEEPING_BAG by register("blue_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val PURPLE_SLEEPING_BAG by register("purple_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val GREEN_SLEEPING_BAG by register("green_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val BROWN_SLEEPING_BAG by register("brown_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val RED_SLEEPING_BAG by register("red_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val BLACK_SLEEPING_BAG by register("black_sleeping_bag") { SleepingBag(Settings().maxCount(1).rarity(Rarity.RARE)) }
    val SLEEPING_BAGS by registerAssociatedTag("sleeping_bags",
        DyeColor.WHITE to "white_sleeping_bag", DyeColor.ORANGE to "orange_sleeping_bag", DyeColor.MAGENTA to "magenta_sleeping_bag", DyeColor.LIGHT_BLUE to "light_blue_sleeping_bag",
        DyeColor.YELLOW to "yellow_sleeping_bag", DyeColor.LIME to "lime_sleeping_bag", DyeColor.PINK to "pink_sleeping_bag", DyeColor.GRAY to "gray_sleeping_bag",
        DyeColor.LIGHT_GRAY to "light_gray_sleeping_bag", DyeColor.CYAN to "cyan_sleeping_bag", DyeColor.BLUE to "blue_sleeping_bag", DyeColor.PURPLE to "purple_sleeping_bag",
        DyeColor.GREEN to "green_sleeping_bag", DyeColor.BROWN to "brown_sleeping_bag", DyeColor.RED to "red_sleeping_bag", DyeColor.BLACK to "black_sleeping_bag"
    )

    val MEASURING_TAPE by register("measuring_tape") { MeasuringTape(Settings().maxCount(1)) }

    fun <E : Fluid> registerBucketItem(string: String, entry: DeferredHolder<Fluid, E>, vararg tags: TagKey<Item>): DeferredHolder<Item, BucketItem> {
        val bucketDelegate = register(string+"_bucket", { BucketItem(entry.get(), Settings().recipeRemainder(Items.BUCKET).maxCount(1)) }, *tags)
        fluidBuckets[entry] = bucketDelegate
        return bucketDelegate
    }

    fun <E : Block> registerBlockItem(string: String, entry: DeferredHolder<Block, E>, vararg tags: TagKey<Item>): DeferredHolder<Item, BlockItem> {
        return register(string, { BlockItem(entry.get(), Settings()) }, *tags).also {
            AS_ITEM.put(entry, string)
        }
    }

    override fun initialize() {
        super.initialize()
        MOD_BUS.addListener(::cauldronBehaviours)
    }

    private fun cauldronBehaviours(event: RegisterCauldronFluidContentEvent) {
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().put(WOODEN_BUCKET, CauldronBehavior { state, world, pos, player, hand, stack ->
            CauldronBehavior.emptyCauldron(state, world, pos, player, hand, stack, ItemStack(WOODEN_WATER_BUCKET), Predicate {
                 it!!.get<Int?>(LeveledCauldronBlock.LEVEL) == 3
            }, SoundEvents.ITEM_BUCKET_FILL)
        })
        CauldronBehavior.POWDER_SNOW_CAULDRON_BEHAVIOR.map().put(WOODEN_WATER_BUCKET, CauldronBehavior { state, world, pos, player, hand, stack ->
            fillCauldronWithWoodenBucket(world, pos, player, hand, stack, Blocks.WATER_CAULDRON.defaultState.with<Int?, Int?>(LeveledCauldronBlock.LEVEL, 3), SoundEvents.ITEM_BUCKET_EMPTY)
        })
    }

    private fun fillCauldronWithWoodenBucket(world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, stack: ItemStack, state: BlockState, soundEvent: SoundEvent): ItemActionResult? {
        if (!world.isClient) {
            val item = stack.item
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, ItemStack(WOODEN_BUCKET)))
            player.incrementStat(Stats.FILL_CAULDRON)
            player.incrementStat(Stats.USED.getOrCreateStat(item))
            world.setBlockState(pos, state)
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f)
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos)
        }

        return ItemActionResult.success(world.isClient)
    }

    override fun initializeClient() {
        ModelLoadingPlugin.register { plugin ->
            plugin.modifyModelOnLoad().register { model, context ->
                val modelIdentifier = context.topLevelId() ?: return@register model
                return@register when (modelIdentifier) {
                    ModelIdentifier.ofInventoryVariant(ModIdentifier.of("entangled_bag")) -> EntangledBagBakedModel()
                    ModelIdentifier.ofInventoryVariant(ModIdentifier.of("entangled_bucket")) -> EntangledBucketBakedModel()
                    else -> model
                }
            }
            plugin.modifyModelAfterBake().register { model, context ->
                val modelIdentifier = context.topLevelId() ?: return@register model
                val item = Registries.ITEM.get(modelIdentifier.id)
                if(item is ArmorItem && item.type == ArmorItem.Type.BOOTS) {
                    return@register model?.let(::SlimyBootsModel)
                }
                return@register model
            }
        }
        MOD_BUS.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
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
    }

}
