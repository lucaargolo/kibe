package io.github.lucaargolo.kibe.block

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.client.model.DrawbridgeCustomModel
import io.github.lucaargolo.kibe.client.model.TankCustomModel
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.block.*
import net.minecraft.block.AbstractBlock.Settings
import net.minecraft.client.render.RenderLayer
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.registry.Registries
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.Properties
import net.minecraft.util.ColorCode
import net.neoforged.neoforge.registries.DeferredHolder
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier


object BlockCompendium : RegistryCompendium<Block>(Registries.BLOCK) {

    val BLOCK_GENERATORS: Array<BlockGenerator>
        get() = blockGenerators.map(DeferredHolder<Block, BlockGenerator>::get).toTypedArray()
    private val blockGenerators = mutableListOf<DeferredHolder<Block, BlockGenerator>>()

    val FLUID_BLOCKS: Map<Fluid, FluidBlock>
        get() = fluidBlocks.mapKeys { e -> e.key.get() }.mapValues { e -> e.value.get() }
    private val fluidBlocks = mutableMapOf<DeferredHolder<Fluid, out Fluid>, DeferredHolder<Block, FluidBlock>>()

    val CURSED_DIRT by register("cursed_dirt", { CursedDirt(Settings.copy(Blocks.GRASS_BLOCK).ticksRandomly().strength(0.6F).sounds(BlockSoundGroup.GRASS)) })
    val REDSTONE_TIMER by register("redstone_timer", { RedstoneTimer(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F).nonOpaque()) })

    val STONE_SPIKES by register("stone_spikes", { Spikes(Spikes.Type.STONE, Settings.copy(Blocks.STONE)) })
    val IRON_SPIKES by register("iron_spikes", { Spikes(Spikes.Type.IRON, Settings.copy(Blocks.IRON_BLOCK)) })
    val GOLD_SPIKES by register("gold_spikes", { Spikes(Spikes.Type.GOLD, Settings.copy(Blocks.GOLD_BLOCK)) })
    val DIAMOND_SPIKES by register("diamond_spikes", { Spikes(Spikes.Type.DIAMOND, Settings.copy(Blocks.DIAMOND_BLOCK)) })

    val REGULAR_CONVEYOR_BELT by register("regular_conveyor_belt", { ConveyorBelt(0.050, Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)) })
    val FAST_CONVEYOR_BELT by register("fast_conveyor_belt", { ConveyorBelt(0.1, Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)) })
    val EXPRESS_CONVEYOR_BELT by register("express_conveyor_belt", { ConveyorBelt(0.2, Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)) })

    val ENTANGLED_TANK by register("entangled_tank", { EntangledTank(Settings.copy(Blocks.OBSIDIAN).requiresTool().strength(22.0F, 600.0F).luminance { state -> state[Properties.LEVEL_15] }) }, false)
    val ENTANGLED_CHEST by register("entangled_chest", { EntangledChest(Settings.copy(Blocks.OBSIDIAN).requiresTool().strength(22.0F, 600.0F)) }, false)
    val TRASH_CAN by register("trash_can", { TrashCan(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F)) })
    val VACUUM_HOPPER by register("vacuum_hopper", { VacuumHopper(Settings.copy(Blocks.IRON_BLOCK).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL).nonOpaque()) })
    val BIG_TORCH by register("big_torch", { BigTorch(Settings.copy(Blocks.TORCH).strength(0.5f).luminance{ state -> if(state[Properties.ENABLED]) 15 else 0 }.sounds(BlockSoundGroup.WOOD)) })
    val COOLER by register("cooler", { Cooler(Settings.create().strength(0.2F).sounds(BlockSoundGroup.SNOW)) }, false)
    val DRAWBRIDGE by register("drawbridge", { Drawbridge(Settings.copy(Blocks.IRON_BLOCK).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL).nonOpaque()) })

    val OBSIDIAN_SAND by register("obsidian_sand", { ColoredFallingBlock(ColorCode(0x171623), Settings.copy(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)) })
    val WITHER_PROOF_BLOCK by register("wither_proof_block", { Block(Settings.copy(Blocks.OBSIDIAN)) })
    val WITHER_PROOF_SAND by register("wither_proof_sand", { ColoredFallingBlock(ColorCode(0x111111), Settings.copy(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)) })
    val WITHER_PROOF_GLASS by register("wither_proof_glass", { TransparentBlock(Settings.copy(Blocks.OBSIDIAN).nonOpaque()) })
    val WITHER_BUILDER by register("wither_builder", { WitherBuilder(Settings.copy(Blocks.OBSIDIAN)) })

    val PLACER by register("placer", { Placer(Settings.copy(Blocks.IRON_BLOCK)) })
    val BREAKER by register("breaker", { Breaker(Settings.copy(Blocks.IRON_BLOCK)) })

    val HEATER by register("heater", { Heater(Settings.copy(Blocks.COBBLESTONE).luminance { if(it[Properties.ENABLED]) 15 else 0 }) })
    val DEHUMIDIFIER by register("dehumidifier", { Dehumidifier(Settings.copy(Blocks.COBBLESTONE)) })

    val COBBLESTONE_GENERATOR_MK1 by registerBlockGenerator("cobblestone_generator_mk1", { BlockGenerator(Settings.copy(Blocks.IRON_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.01f) })
    val COBBLESTONE_GENERATOR_MK2 by registerBlockGenerator("cobblestone_generator_mk2", { BlockGenerator(Settings.copy(Blocks.GOLD_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.04f) })
    val COBBLESTONE_GENERATOR_MK3 by registerBlockGenerator("cobblestone_generator_mk3", { BlockGenerator(Settings.copy(Blocks.DIAMOND_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.16f) })
    val COBBLESTONE_GENERATOR_MK4 by registerBlockGenerator("cobblestone_generator_mk4", { BlockGenerator(Settings.copy(Blocks.EMERALD_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.64f) })
    val COBBLESTONE_GENERATOR_MK5 by registerBlockGenerator("cobblestone_generator_mk5", { BlockGenerator(Settings.copy(Blocks.NETHERITE_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 2.56f) })

    val BASALT_GENERATOR_MK1 by registerBlockGenerator("basalt_generator_mk1", { BlockGenerator(Settings.copy(Blocks.IRON_BLOCK).luminance { 4 }, Blocks.BASALT, 0.01f) })
    val BASALT_GENERATOR_MK2 by registerBlockGenerator("basalt_generator_mk2", { BlockGenerator(Settings.copy(Blocks.GOLD_BLOCK).luminance { 4 }, Blocks.BASALT, 0.04f) })
    val BASALT_GENERATOR_MK3 by registerBlockGenerator("basalt_generator_mk3", { BlockGenerator(Settings.copy(Blocks.DIAMOND_BLOCK).luminance { 4 }, Blocks.BASALT, 0.16f) })
    val BASALT_GENERATOR_MK4 by registerBlockGenerator("basalt_generator_mk4", { BlockGenerator(Settings.copy(Blocks.EMERALD_BLOCK).luminance { 4 }, Blocks.BASALT, 0.64f) })
    val BASALT_GENERATOR_MK5 by registerBlockGenerator("basalt_generator_mk5", { BlockGenerator(Settings.copy(Blocks.NETHERITE_BLOCK).luminance { 4 }, Blocks.BASALT, 2.56f) })

    val LIGHT_SOURCE by register("light_source", { LightSource(Settings.copy(Blocks.GLASS).luminance{ 15 }.ticksRandomly().noCollision().breakInstantly().dropsNothing()) }, false)
    val CHUNK_LOADER by register("chunk_loader", { ChunkLoader(Settings.copy(Blocks.ENCHANTING_TABLE).requiresTool().strength(22.0F, 600.0F)) })
    val TANK by register("tank", { Tank(Settings.copy(Blocks.GLASS).strength(0.5F).nonOpaque().luminance { state -> state[Properties.LEVEL_15] }.sounds(BlockSoundGroup.GLASS)) }, false)
    val XP_SHOWER by register("xp_shower", { XpShower(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F)) })
    val XP_DRAIN by register("xp_drain", { XpDrain(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F)) })
    val IGNITER by register("igniter", { Igniter(Settings.copy(Blocks.COBBLESTONE)) })
    val FLUID_HOPPER by register("fluid_hopper", { FluidHopper(Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.STONE_GRAY).requiresTool().strength(3.0F, 4.8F).sounds(BlockSoundGroup.METAL).nonOpaque()) })

    val WHITE_ELEVATOR by register("white_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val ORANGE_ELEVATOR by register("orange_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val MAGENTA_ELEVATOR by register("magenta_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val LIGHT_BLUE_ELEVATOR by register("light_blue_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val YELLOW_ELEVATOR by register("yellow_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val LIME_ELEVATOR by register("lime_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val PINK_ELEVATOR by register("pink_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val GRAY_ELEVATOR by register("gray_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val LIGHT_GRAY_ELEVATOR by register("light_gray_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val CYAN_ELEVATOR by register("cyan_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val BLUE_ELEVATOR by register("blue_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val PURPLE_ELEVATOR by register("purple_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val GREEN_ELEVATOR by register("green_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val BROWN_ELEVATOR by register("brown_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val RED_ELEVATOR by register("red_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })
    val BLACK_ELEVATOR by register("black_elevator", { Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.STONE_GRAY).requiresTool().strength(1.5F, 6.0F)) })

    override fun <E : Block> register(string: String, entry: Supplier<E>): DeferredHolder<Block, E> {
        return register(string, entry, true)
    }

    fun registerBlockGenerator(string: String, entry: () -> BlockGenerator): DeferredHolder<Block, BlockGenerator> {
        return register(string, entry).also(blockGenerators::add)
    }

    fun <E : FlowableFluid> registerFluidBlock(string: String, entry: DeferredHolder<Fluid, E>): DeferredHolder<Block, FluidBlock> {
        val blockDelegate = register(string, { FluidBlock(entry.get(), Settings.copy(Blocks.LAVA)) }, false)
        fluidBlocks[entry] = blockDelegate
        return blockDelegate
    }

    fun <E : Block> register(string: String, entry: Supplier<E>, hasBlockItem: Boolean): DeferredHolder<Block, E> {
        val delegate = super.register(string, entry)
        if(hasBlockItem) {
            ItemCompendium.registerBlockItem(string, delegate)
        }
        return delegate
    }

    override fun initializeClient() {
        super.initializeClient()
        BlockRenderLayerMap.INSTANCE.putBlock(DRAWBRIDGE, RenderLayer.getCutoutMipped())
        BlockRenderLayerMap.INSTANCE.putBlock(VACUUM_HOPPER, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(BIG_TORCH, RenderLayer.getCutoutMipped())
        BlockRenderLayerMap.INSTANCE.putBlock(COOLER, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(WITHER_PROOF_GLASS, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(ENTANGLED_TANK, RenderLayer.getCutoutMipped())
        ModelLoadingPlugin.register { plugin ->
            plugin.modifyModelOnLoad().register { model, context ->
                val modelIdentifier = context.topLevelId()
                if(modelIdentifier != null && modelIdentifier.id.namespace == KibeMod.MOD_ID) {
                    when (modelIdentifier.id.path) {
                        "drawbridge" -> DrawbridgeCustomModel()
                        "tank" -> if(modelIdentifier.variant != "inventory") TankCustomModel() else model
                        else -> model
                    }
                } else model
            }
        }
    }

}