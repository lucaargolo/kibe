package io.github.lucaargolo.kibe.block

import io.github.lucaargolo.kibe.client.model.DrawbridgeCustomModel
import io.github.lucaargolo.kibe.client.model.TankCustomModel
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.utils.ModIdentifier
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.minecraft.block.*
import net.minecraft.block.AbstractBlock.Settings
import net.minecraft.client.render.RenderLayer
import net.minecraft.fluid.FlowableFluid
import net.minecraft.item.Item
import net.minecraft.fluid.Fluid
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.Properties
import net.minecraft.util.ColorCode
import net.minecraft.util.DyeColor
import java.util.function.Supplier
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.registries.DeferredHolder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier


object BlockCompendium : RegistryCompendium<Block>(Registries.BLOCK) {

    val FLUID_BLOCKS: Map<Fluid, FluidBlock>
        get() = fluidBlocks.mapKeys { e -> e.key.get() }.mapValues { e -> e.value.get() }
    private val fluidBlocks = mutableMapOf<DeferredHolder<Fluid, out Fluid>, DeferredHolder<Block, FluidBlock>>()

    val CURSED_DIRT by register("cursed_dirt", { CursedDirt(Settings.copy(Blocks.GRASS_BLOCK).ticksRandomly().strength(0.6F).sounds(BlockSoundGroup.GRASS)) }, BlockTags.SHOVEL_MINEABLE)
    val REDSTONE_TIMER by register("redstone_timer", { RedstoneTimer(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F).nonOpaque()) }, BlockTags.PICKAXE_MINEABLE)

    val STONE_SPIKES by register("stone_spikes", { Spikes(Spikes.Type.STONE, Settings.copy(Blocks.STONE)) }, BlockTags.PICKAXE_MINEABLE)
    val IRON_SPIKES by register("iron_spikes", { Spikes(Spikes.Type.IRON, Settings.copy(Blocks.IRON_BLOCK)) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val GOLD_SPIKES by register("gold_spikes", { Spikes(Spikes.Type.GOLD, Settings.copy(Blocks.GOLD_BLOCK)) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE, ItemTags.PIGLIN_LOVED)
    val DIAMOND_SPIKES by register("diamond_spikes", { Spikes(Spikes.Type.DIAMOND, Settings.copy(Blocks.DIAMOND_BLOCK)) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)

    val REGULAR_CONVEYOR_BELT by register("regular_conveyor_belt", { ConveyorBelt(0.050, Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)) }, BlockTags.PICKAXE_MINEABLE)
    val FAST_CONVEYOR_BELT by register("fast_conveyor_belt", { ConveyorBelt(0.1, Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)) }, BlockTags.PICKAXE_MINEABLE)
    val EXPRESS_CONVEYOR_BELT by register("express_conveyor_belt", { ConveyorBelt(0.2, Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)) }, BlockTags.PICKAXE_MINEABLE)

    val ENTANGLED_TANK by register("entangled_tank", { EntangledTank(Settings.copy(Blocks.OBSIDIAN).requiresTool().strength(22.0F, 600.0F).luminance { state -> state[Properties.LEVEL_15] }) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE, hasItem = false)
    val ENTANGLED_CHEST by register("entangled_chest", { EntangledChest(Settings.copy(Blocks.OBSIDIAN).requiresTool().strength(22.0F, 600.0F)) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE, hasItem = false)
    val TRASH_CAN by register("trash_can", { TrashCan(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val VACUUM_HOPPER by register("vacuum_hopper", { VacuumHopper(Settings.copy(Blocks.IRON_BLOCK).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BIG_TORCH by register("big_torch", { BigTorch(Settings.copy(Blocks.TORCH).strength(0.5f).luminance { state -> if (state[Properties.ENABLED]) 15 else 0 }.sounds(BlockSoundGroup.WOOD)) }, BlockTags.AXE_MINEABLE)
    val COOLER by register("cooler", { Cooler(Settings.create().strength(0.2F).sounds(BlockSoundGroup.SNOW)) }, BlockTags.SHOVEL_MINEABLE, hasItem = false)
    val DRAWBRIDGE by register("drawbridge", { Drawbridge(Settings.copy(Blocks.IRON_BLOCK).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL).nonOpaque()) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)

    val OBSIDIAN_SAND by register("obsidian_sand", { ColoredFallingBlock(ColorCode(0x171623), Settings.copy(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)) }, BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.SHOVEL_MINEABLE)
    val WITHER_PROOF_BLOCK by register("wither_proof_block", { Block(Settings.copy(Blocks.OBSIDIAN)) }, BlockTags.WITHER_IMMUNE, BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)
    val WITHER_PROOF_SAND by register("wither_proof_sand", { ColoredFallingBlock(ColorCode(0x111111), Settings.copy(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)) }, BlockTags.WITHER_IMMUNE, BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.SHOVEL_MINEABLE)
    val WITHER_PROOF_GLASS by register("wither_proof_glass", { TransparentBlock(Settings.copy(Blocks.OBSIDIAN).nonOpaque()) }, BlockTags.WITHER_IMMUNE, BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)
    val WITHER_BUILDER by register("wither_builder", { WitherBuilder(Settings.copy(Blocks.OBSIDIAN)) }, BlockTags.WITHER_IMMUNE, BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)

    val PLACER by register("placer", { Placer(Settings.copy(Blocks.IRON_BLOCK)) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BREAKER by register("breaker", { Breaker(Settings.copy(Blocks.IRON_BLOCK)) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)

    val HEATER by register("heater", { Heater(Settings.copy(Blocks.COBBLESTONE).luminance { if (it[Properties.ENABLED]) 15 else 0 }) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val DEHUMIDIFIER by register("dehumidifier", { Dehumidifier(Settings.copy(Blocks.COBBLESTONE)) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)

    val COBBLESTONE_GENERATOR_MK1 by register("cobblestone_generator_mk1", { BlockGenerator(Settings.copy(Blocks.IRON_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.01f) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val COBBLESTONE_GENERATOR_MK2 by register("cobblestone_generator_mk2", { BlockGenerator(Settings.copy(Blocks.GOLD_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.04f) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE, ItemTags.PIGLIN_LOVED)
    val COBBLESTONE_GENERATOR_MK3 by register("cobblestone_generator_mk3", { BlockGenerator(Settings.copy(Blocks.DIAMOND_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.16f) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)
    val COBBLESTONE_GENERATOR_MK4 by register("cobblestone_generator_mk4", { BlockGenerator(Settings.copy(Blocks.EMERALD_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.64f) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)
    val COBBLESTONE_GENERATOR_MK5 by register("cobblestone_generator_mk5", { BlockGenerator(Settings.copy(Blocks.NETHERITE_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 2.56f) }, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BASALT_GENERATOR_MK1 by register("basalt_generator_mk1", { BlockGenerator(Settings.copy(Blocks.IRON_BLOCK).luminance { 4 }, Blocks.BASALT, 0.01f) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BASALT_GENERATOR_MK2 by register("basalt_generator_mk2", { BlockGenerator(Settings.copy(Blocks.GOLD_BLOCK).luminance { 4 }, Blocks.BASALT, 0.04f) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE, ItemTags.PIGLIN_LOVED)
    val BASALT_GENERATOR_MK3 by register("basalt_generator_mk3", { BlockGenerator(Settings.copy(Blocks.DIAMOND_BLOCK).luminance { 4 }, Blocks.BASALT, 0.16f) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BASALT_GENERATOR_MK4 by register("basalt_generator_mk4", { BlockGenerator(Settings.copy(Blocks.EMERALD_BLOCK).luminance { 4 }, Blocks.BASALT, 0.64f) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BASALT_GENERATOR_MK5 by register("basalt_generator_mk5", { BlockGenerator(Settings.copy(Blocks.NETHERITE_BLOCK).luminance { 4 }, Blocks.BASALT, 2.56f) }, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BLOCK_GENERATORS = registerTag("block_generators",
        "cobblestone_generator_mk1", "basalt_generator_mk1",
        "cobblestone_generator_mk2", "basalt_generator_mk2",
        "cobblestone_generator_mk3", "basalt_generator_mk3",
        "cobblestone_generator_mk4", "basalt_generator_mk4",
        "cobblestone_generator_mk5", "basalt_generator_mk5"
    )

    val LIGHT_SOURCE by register("light_source", { LightSource(Settings.copy(Blocks.GLASS).luminance { 15 }.ticksRandomly().noCollision().breakInstantly().dropsNothing()) }, hasItem = false)
    val CHUNK_LOADER by register("chunk_loader", { ChunkLoader(Settings.copy(Blocks.ENCHANTING_TABLE).requiresTool().strength(22.0F, 600.0F)) }, BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)
    val TANK by register("tank", { Tank(Settings.copy(Blocks.GLASS).strength(0.5F).nonOpaque().luminance { state -> state[Properties.LEVEL_15] }.sounds(BlockSoundGroup.GLASS)) }, hasItem = false)
    val XP_SHOWER by register("xp_shower", { XpShower(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val XP_DRAIN by register("xp_drain", { XpDrain(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val IGNITER by register("igniter", { Igniter(Settings.copy(Blocks.COBBLESTONE)) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val FLUID_HOPPER by register("fluid_hopper", { FluidHopper(Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.STONE_GRAY).requiresTool().strength(3.0F, 4.8F).sounds(BlockSoundGroup.METAL).nonOpaque()) }, BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)

    val WHITE_ELEVATOR by register("white_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.WHITE).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val ORANGE_ELEVATOR by register("orange_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.ORANGE).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val MAGENTA_ELEVATOR by register("magenta_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.MAGENTA).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val LIGHT_BLUE_ELEVATOR by register("light_blue_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.LIGHT_BLUE).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val YELLOW_ELEVATOR by register("yellow_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.YELLOW).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val LIME_ELEVATOR by register("lime_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.LIME).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val PINK_ELEVATOR by register("pink_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.PINK).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val GRAY_ELEVATOR by register("gray_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.GRAY).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val LIGHT_GRAY_ELEVATOR by register("light_gray_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val CYAN_ELEVATOR by register("cyan_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.CYAN).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val BLUE_ELEVATOR by register("blue_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.BLUE).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val PURPLE_ELEVATOR by register("purple_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.PURPLE).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val GREEN_ELEVATOR by register("green_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.GREEN).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val BROWN_ELEVATOR by register("brown_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.BROWN).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val RED_ELEVATOR by register("red_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.RED).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val BLACK_ELEVATOR by register("black_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.BLACK).requiresTool().strength(1.5F, 6.0F)) }, BlockTags.PICKAXE_MINEABLE)
    val ELEVATORS = registerAssociatedTag("elevators",
        DyeColor.WHITE to "white_elevator", DyeColor.ORANGE to "orange_elevator", DyeColor.MAGENTA to "magenta_elevator", DyeColor.LIGHT_BLUE to "light_blue_elevator",
        DyeColor.YELLOW to "yellow_elevator", DyeColor.LIME to "lime_elevator", DyeColor.PINK to "pink_elevator", DyeColor.GRAY to "gray_elevator",
        DyeColor.LIGHT_GRAY to "light_gray_elevator", DyeColor.CYAN to "cyan_elevator", DyeColor.BLUE to "blue_elevator", DyeColor.PURPLE to "purple_elevator",
        DyeColor.GREEN to "green_elevator", DyeColor.BROWN to "brown_elevator", DyeColor.RED to "red_elevator", DyeColor.BLACK to "black_elevator"
    )

    val MAGNET_INHIBITOR: TagEntry<Block> = registerTag("magnet_inhibitor", children = mutableListOf(ConventionalBlockTags.STORAGE_BLOCKS_COAL))

    fun <E : FlowableFluid> registerFluidBlock(string: String, entry: Supplier<E>, vararg tags: TagKey<Block>): Lazy<FluidBlock> {
        val blockDelegate = register(string, { FluidBlock(entry.get(), Settings.copy(Blocks.LAVA)) }, false)
        fluidBlocks[entry] = blockDelegate
        return blockDelegate
    }

    override fun <E: Block> register(string: String, entry: Supplier<E>, vararg tags: TagKey<Block>): Lazy<E> {
        return register(string, entry, *tags, hasItem = true)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : Block> register(string: String, entry: Supplier<E>, vararg tags: TagKey<*>, hasItem: Boolean = true): Lazy<E> {
        return super.register(string, entry, *tags.filter { it.registry() == RegistryKeys.BLOCK }.map { it as TagKey<Block> }.toTypedArray()).also {
            if(hasItem) {
                ItemCompendium.registerBlockItem(string, it, *tags.filter { it.registry() == RegistryKeys.ITEM }.map { it as TagKey<Item> }.toTypedArray())
            }
        }
    }

    override fun initializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(DRAWBRIDGE, RenderLayer.getCutoutMipped())
        BlockRenderLayerMap.INSTANCE.putBlock(VACUUM_HOPPER, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(BIG_TORCH, RenderLayer.getCutoutMipped())
        BlockRenderLayerMap.INSTANCE.putBlock(COOLER, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(WITHER_PROOF_GLASS, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(ENTANGLED_TANK, RenderLayer.getCutoutMipped())

        ModelLoadingPlugin.register { plugin ->
            plugin.modifyModelOnLoad().register { model, context ->
                val modelIdentifier = context.topLevelId() ?: return@register model
                return@register when (modelIdentifier.id) {
                    ModIdentifier.of("drawbridge") -> DrawbridgeCustomModel()
                    ModIdentifier.of("tank") -> TankCustomModel()
                    else -> model
                }
            }
        }
        MOD_BUS.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        BlockRenderLayerMap.INSTANCE.putBlock(DRAWBRIDGE, RenderLayer.getCutoutMipped())
        BlockRenderLayerMap.INSTANCE.putBlock(VACUUM_HOPPER, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(BIG_TORCH, RenderLayer.getCutoutMipped())
        BlockRenderLayerMap.INSTANCE.putBlock(COOLER, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(WITHER_PROOF_GLASS, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(ENTANGLED_TANK, RenderLayer.getCutoutMipped())
    }


}