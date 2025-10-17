package io.github.lucaargolo.kibe.block

import io.github.lucaargolo.kibe.KibeMod
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
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.Properties
import net.minecraft.util.ColorCode
import net.minecraft.util.DyeColor


object BlockCompendium : RegistryCompendium<Block>(Registries.BLOCK) {

    val CURSED_DIRT = registerBlock("cursed_dirt", CursedDirt(Settings.copy(Blocks.GRASS_BLOCK).ticksRandomly().strength(0.6F).sounds(BlockSoundGroup.GRASS)), BlockTags.SHOVEL_MINEABLE)
    val REDSTONE_TIMER = registerBlock("redstone_timer", RedstoneTimer(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F).nonOpaque()), BlockTags.PICKAXE_MINEABLE)

    val STONE_SPIKES = registerBlock("stone_spikes", Spikes(Spikes.Type.STONE, Settings.copy(Blocks.STONE)), BlockTags.PICKAXE_MINEABLE)
    val IRON_SPIKES = registerBlock("iron_spikes", Spikes(Spikes.Type.IRON, Settings.copy(Blocks.IRON_BLOCK)), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val GOLD_SPIKES = registerBlock("gold_spikes", Spikes(Spikes.Type.GOLD, Settings.copy(Blocks.GOLD_BLOCK)), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE, ItemTags.PIGLIN_LOVED)
    val DIAMOND_SPIKES = registerBlock("diamond_spikes", Spikes(Spikes.Type.DIAMOND, Settings.copy(Blocks.DIAMOND_BLOCK)), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)

    val REGULAR_CONVEYOR_BELT = registerBlock("regular_conveyor_belt", ConveyorBelt(0.050, Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)), BlockTags.PICKAXE_MINEABLE)
    val FAST_CONVEYOR_BELT = registerBlock("fast_conveyor_belt", ConveyorBelt(0.1, Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)), BlockTags.PICKAXE_MINEABLE)
    val EXPRESS_CONVEYOR_BELT = registerBlock("express_conveyor_belt", ConveyorBelt(0.2, Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)), BlockTags.PICKAXE_MINEABLE)

    val ENTANGLED_TANK = registerBlock("entangled_tank", EntangledTank(Settings.copy(Blocks.OBSIDIAN).requiresTool().strength(22.0F, 600.0F).luminance { state -> state[Properties.LEVEL_15] }), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE, hasItem = false)
    val ENTANGLED_CHEST = registerBlock("entangled_chest", EntangledChest(Settings.copy(Blocks.OBSIDIAN).requiresTool().strength(22.0F, 600.0F)), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE, hasItem = false)
    val TRASH_CAN = registerBlock("trash_can", TrashCan(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val VACUUM_HOPPER = registerBlock("vacuum_hopper", VacuumHopper(Settings.copy(Blocks.IRON_BLOCK).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BIG_TORCH = registerBlock("big_torch", BigTorch(Settings.copy(Blocks.TORCH).strength(0.5f).luminance{ state -> if(state[Properties.ENABLED]) 15 else 0 }.sounds(BlockSoundGroup.WOOD)), BlockTags.AXE_MINEABLE)
    val COOLER = registerBlock("cooler", Cooler(Settings.create().strength(0.2F).sounds(BlockSoundGroup.SNOW)), BlockTags.SHOVEL_MINEABLE, hasItem = false)
    val DRAWBRIDGE = registerBlock("drawbridge", Drawbridge(Settings.copy(Blocks.IRON_BLOCK).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL).nonOpaque()), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)

    val OBSIDIAN_SAND = registerBlock("obsidian_sand", ColoredFallingBlock(ColorCode(0x171623), Settings.copy(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)), BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.SHOVEL_MINEABLE)
    val WITHER_PROOF_BLOCK = registerBlock("wither_proof_block", Block(Settings.copy(Blocks.OBSIDIAN)), BlockTags.WITHER_IMMUNE, BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)
    val WITHER_PROOF_SAND = registerBlock("wither_proof_sand", ColoredFallingBlock(ColorCode(0x111111), Settings.copy(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)), BlockTags.WITHER_IMMUNE, BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.SHOVEL_MINEABLE)
    val WITHER_PROOF_GLASS = registerBlock("wither_proof_glass", TransparentBlock(Settings.copy(Blocks.OBSIDIAN).nonOpaque()), BlockTags.WITHER_IMMUNE, BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)
    val WITHER_BUILDER = registerBlock("wither_builder", WitherBuilder(Settings.copy(Blocks.OBSIDIAN)), BlockTags.WITHER_IMMUNE, BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)

    val PLACER = registerBlock("placer", Placer(Settings.copy(Blocks.IRON_BLOCK)), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BREAKER = registerBlock("breaker", Breaker(Settings.copy(Blocks.IRON_BLOCK)), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)

    val HEATER = registerBlock("heater", Heater(Settings.copy(Blocks.COBBLESTONE).luminance { if(it[Properties.ENABLED]) 15 else 0 }), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val DEHUMIDIFIER = registerBlock("dehumidifier", Dehumidifier(Settings.copy(Blocks.COBBLESTONE)), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)

    val COBBLESTONE_GENERATOR_MK1 = registerBlock("cobblestone_generator_mk1", BlockGenerator(Settings.copy(Blocks.IRON_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.01f), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val COBBLESTONE_GENERATOR_MK2 = registerBlock("cobblestone_generator_mk2", BlockGenerator(Settings.copy(Blocks.GOLD_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.04f), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE, ItemTags.PIGLIN_LOVED)
    val COBBLESTONE_GENERATOR_MK3 = registerBlock("cobblestone_generator_mk3", BlockGenerator(Settings.copy(Blocks.DIAMOND_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.16f), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)
    val COBBLESTONE_GENERATOR_MK4 = registerBlock("cobblestone_generator_mk4", BlockGenerator(Settings.copy(Blocks.EMERALD_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 0.64f), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)
    val COBBLESTONE_GENERATOR_MK5 = registerBlock("cobblestone_generator_mk5", BlockGenerator(Settings.copy(Blocks.NETHERITE_BLOCK).luminance { 4 }, Blocks.COBBLESTONE, 2.56f), BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BASALT_GENERATOR_MK1 = registerBlock("basalt_generator_mk1", BlockGenerator(Settings.copy(Blocks.IRON_BLOCK).luminance { 4 }, Blocks.BASALT, 0.01f), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BASALT_GENERATOR_MK2 = registerBlock("basalt_generator_mk2", BlockGenerator(Settings.copy(Blocks.GOLD_BLOCK).luminance { 4 }, Blocks.BASALT, 0.04f), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE, ItemTags.PIGLIN_LOVED)
    val BASALT_GENERATOR_MK3 = registerBlock("basalt_generator_mk3", BlockGenerator(Settings.copy(Blocks.DIAMOND_BLOCK).luminance { 4 }, Blocks.BASALT, 0.16f), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BASALT_GENERATOR_MK4 = registerBlock("basalt_generator_mk4", BlockGenerator(Settings.copy(Blocks.EMERALD_BLOCK).luminance { 4 }, Blocks.BASALT, 0.64f), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BASALT_GENERATOR_MK5 = registerBlock("basalt_generator_mk5", BlockGenerator(Settings.copy(Blocks.NETHERITE_BLOCK).luminance { 4 }, Blocks.BASALT, 2.56f), BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)
    val BLOCK_GENERATORS = registerTag("block_generators",
        COBBLESTONE_GENERATOR_MK1, BASALT_GENERATOR_MK1,
        COBBLESTONE_GENERATOR_MK2, BASALT_GENERATOR_MK2,
        COBBLESTONE_GENERATOR_MK3, BASALT_GENERATOR_MK3,
        COBBLESTONE_GENERATOR_MK4, BASALT_GENERATOR_MK4,
        COBBLESTONE_GENERATOR_MK5, BASALT_GENERATOR_MK5
    )

    val LIGHT_SOURCE = registerBlock("light_source", LightSource(Settings.copy(Blocks.GLASS).luminance{ 15 }.ticksRandomly().noCollision().breakInstantly().dropsNothing()), hasItem = false)
    val CHUNK_LOADER = registerBlock("chunk_loader", ChunkLoader(Settings.copy(Blocks.ENCHANTING_TABLE).requiresTool().strength(22.0F, 600.0F)), BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)
    val TANK = registerBlock("tank", Tank(Settings.copy(Blocks.GLASS).strength(0.5F).nonOpaque().luminance { state -> state[Properties.LEVEL_15] }.sounds(BlockSoundGroup.GLASS)), hasItem = false)
    val XP_SHOWER = registerBlock("xp_shower", XpShower(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F)), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val XP_DRAIN = registerBlock("xp_drain", XpDrain(Settings.copy(Blocks.STONE).requiresTool().strength(1.5F, 6.0F)), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val IGNITER = registerBlock("igniter", Igniter(Settings.copy(Blocks.COBBLESTONE)), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)
    val FLUID_HOPPER = registerBlock("fluid_hopper", FluidHopper(Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.STONE_GRAY).requiresTool().strength(3.0F, 4.8F).sounds(BlockSoundGroup.METAL).nonOpaque()), BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)

    val WHITE_ELEVATOR = registerBlock("white_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.WHITE).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val ORANGE_ELEVATOR = registerBlock("orange_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.ORANGE).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val MAGENTA_ELEVATOR = registerBlock("magenta_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.MAGENTA).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val LIGHT_BLUE_ELEVATOR = registerBlock("light_blue_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.LIGHT_BLUE).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val YELLOW_ELEVATOR = registerBlock("yellow_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.YELLOW).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val LIME_ELEVATOR = registerBlock("lime_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.LIME).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val PINK_ELEVATOR = registerBlock("pink_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.PINK).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val GRAY_ELEVATOR = registerBlock("gray_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.GRAY).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val LIGHT_GRAY_ELEVATOR = registerBlock("light_gray_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val CYAN_ELEVATOR = registerBlock("cyan_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.CYAN).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val BLUE_ELEVATOR = registerBlock("blue_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.BLUE).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val PURPLE_ELEVATOR = registerBlock("purple_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.PURPLE).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val GREEN_ELEVATOR = registerBlock("green_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.GREEN).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val BROWN_ELEVATOR = registerBlock("brown_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.BROWN).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val RED_ELEVATOR = registerBlock("red_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.RED).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val BLACK_ELEVATOR = registerBlock("black_elevator", Elevator(Settings.copy(Blocks.STONE).mapColor(MapColor.BLACK).requiresTool().strength(1.5F, 6.0F)), BlockTags.PICKAXE_MINEABLE)
    val ELEVATORS = registerAssociatedTag("elevators",
        DyeColor.WHITE to WHITE_ELEVATOR, DyeColor.ORANGE to ORANGE_ELEVATOR, DyeColor.MAGENTA to MAGENTA_ELEVATOR, DyeColor.LIGHT_BLUE to LIGHT_BLUE_ELEVATOR,
        DyeColor.YELLOW to YELLOW_ELEVATOR, DyeColor.LIME to LIME_ELEVATOR, DyeColor.PINK to PINK_ELEVATOR, DyeColor.GRAY to GRAY_ELEVATOR,
        DyeColor.LIGHT_GRAY to LIGHT_GRAY_ELEVATOR, DyeColor.CYAN to CYAN_ELEVATOR, DyeColor.BLUE to BLUE_ELEVATOR, DyeColor.PURPLE to PURPLE_ELEVATOR,
        DyeColor.GREEN to GREEN_ELEVATOR, DyeColor.BROWN to BROWN_ELEVATOR, DyeColor.RED to RED_ELEVATOR, DyeColor.BLACK to BLACK_ELEVATOR
    )

    val MAGNET_INHIBITOR: TagEntry<Block> = registerTag("magnet_inhibitor", children = mutableListOf(ConventionalBlockTags.STORAGE_BLOCKS_COAL))

    fun <E : FlowableFluid> registerFluidBlock(string: String, entry: E): FluidBlock {
        return registerBlock(string, FluidBlock(entry, Settings.copy(Blocks.LAVA)), hasItem = false)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : Block> registerBlock(string: String, entry: E, vararg tags: TagKey<*>, hasItem: Boolean = true): E {
        if(hasItem) {
            ItemCompendium.registerBlockItem(string, entry, *tags.filter { it.registry() == RegistryKeys.ITEM }.map { it as TagKey<Item> }.toTypedArray())
        }
        return super.register(ModIdentifier.of(string), entry, *tags.filter { it.registry() == RegistryKeys.BLOCK }.map { it as TagKey<Block> }.toTypedArray())
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
                        "tank" -> TankCustomModel()
                        else -> model
                    }
                } else model
            }
        }
    }


}