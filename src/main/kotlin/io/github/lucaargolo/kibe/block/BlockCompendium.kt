package io.github.lucaargolo.kibe.block

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.client.model.DrawbridgeCustomModel
import io.github.lucaargolo.kibe.client.model.TankCustomModel
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.utils.ModIdentifier
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.fluid.FlowableFluid
import net.minecraft.registry.Registries
import net.minecraft.sound.BlockSoundGroup


object BlockCompendium : RegistryCompendium<Block>(Registries.BLOCK) {

    val BLOCK_GENERATORS: Array<BlockGenerator>
        get() = blockGenerators.toTypedArray()
    private val blockGenerators = mutableListOf<BlockGenerator>()

    val CURSED_DIRT = register("cursed_dirt", CursedDirt())
    val REDSTONE_TIMER = register("redstone_timer", RedstoneTimer())

    val STONE_SPIKES = register("stone_spikes", Spikes(Spikes.Type.STONE, FabricBlockSettings.copyOf(Blocks.STONE)))
    val IRON_SPIKES = register("iron_spikes", Spikes(Spikes.Type.IRON, FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
    val GOLD_SPIKES = register("gold_spikes", Spikes(Spikes.Type.GOLD, FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK)))
    val DIAMOND_SPIKES = register("diamond_spikes", Spikes(Spikes.Type.DIAMOND, FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK)))

    val REGULAR_CONVEYOR_BELT = register("regular_conveyor_belt", ConveyorBelt(0.050))
    val FAST_CONVEYOR_BELT = register("fast_conveyor_belt", ConveyorBelt(0.1))
    val EXPRESS_CONVEYOR_BELT = register("express_conveyor_belt", ConveyorBelt(0.2))

    val ENTANGLED_TANK = register("entangled_tank", EntangledTank(), false)
    val ENTANGLED_CHEST = register("entangled_chest", EntangledChest(), false)
    val TRASH_CAN = register("trash_can", TrashCan())
    val VACUUM_HOPPER = register("vacuum_hopper", VacuumHopper())
    val BIG_TORCH = register("big_torch", BigTorch())
    val COOLER = register("cooler", Cooler(), false)
    val DRAWBRIDGE = register("drawbridge", Drawbridge())

    val OBSIDIAN_SAND = register("obsidian_sand", FallingBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)))
    val WITHER_PROOF_BLOCK = register("wither_proof_block", Block(FabricBlockSettings.copyOf(Blocks.OBSIDIAN)))
    val WITHER_PROOF_SAND = register("wither_proof_sand", FallingBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)))
    val WITHER_PROOF_GLASS = register("wither_proof_glass", GlassBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).nonOpaque()))
    val WITHER_BUILDER = register("wither_builder", WitherBuilder())

    val PLACER = register("placer", Placer())
    val BREAKER = register("breaker", Breaker())

    val HEATER = register("heater", Heater())
    val DEHUMIDIFIER = register("dehumidifier", Dehumidifier())

    val COBBLESTONE_GENERATOR_MK1 = registerBlockGenerator("cobblestone_generator_mk1", BlockGenerator(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.01f))
    val COBBLESTONE_GENERATOR_MK2 = registerBlockGenerator("cobblestone_generator_mk2", BlockGenerator(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.04f))
    val COBBLESTONE_GENERATOR_MK3 = registerBlockGenerator("cobblestone_generator_mk3", BlockGenerator(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.16f))
    val COBBLESTONE_GENERATOR_MK4 = registerBlockGenerator("cobblestone_generator_mk4", BlockGenerator(FabricBlockSettings.copyOf(Blocks.EMERALD_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.64f))
    val COBBLESTONE_GENERATOR_MK5 = registerBlockGenerator("cobblestone_generator_mk5", BlockGenerator(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK).luminance(4), Blocks.COBBLESTONE, 2.56f))

    val BASALT_GENERATOR_MK1 = registerBlockGenerator("basalt_generator_mk1", BlockGenerator(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).luminance(4), Blocks.BASALT, 0.01f))
    val BASALT_GENERATOR_MK2 = registerBlockGenerator("basalt_generator_mk2", BlockGenerator(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).luminance(4), Blocks.BASALT, 0.04f))
    val BASALT_GENERATOR_MK3 = registerBlockGenerator("basalt_generator_mk3", BlockGenerator(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).luminance(4), Blocks.BASALT, 0.16f))
    val BASALT_GENERATOR_MK4 = registerBlockGenerator("basalt_generator_mk4", BlockGenerator(FabricBlockSettings.copyOf(Blocks.EMERALD_BLOCK).luminance(4), Blocks.BASALT, 0.64f))
    val BASALT_GENERATOR_MK5 = registerBlockGenerator("basalt_generator_mk5", BlockGenerator(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK).luminance(4), Blocks.BASALT, 2.56f))

    val LIGHT_SOURCE = register("light_source", LightSource(), false)
    val CHUNK_LOADER = register("chunk_loader", ChunkLoader())
    val TANK = register("tank", Tank(), false)
    val XP_SHOWER = register("xp_shower", XpShower())
    val XP_DRAIN = register("xp_drain", XpDrain())
    val IGNITER = register("igniter", Igniter())
    val FLUID_HOPPER = register("fluid_hopper", FluidHopper())

    val WHITE_ELEVATOR = register("white_elevator", Elevator())
    val ORANGE_ELEVATOR = register("orange_elevator", Elevator())
    val MAGENTA_ELEVATOR = register("magenta_elevator", Elevator())
    val LIGHT_BLUE_ELEVATOR = register("light_blue_elevator", Elevator())
    val YELLOW_ELEVATOR = register("yellow_elevator", Elevator())
    val LIME_ELEVATOR = register("lime_elevator", Elevator())
    val PINK_ELEVATOR = register("pink_elevator", Elevator())
    val GRAY_ELEVATOR = register("gray_elevator", Elevator())
    val LIGHT_GRAY_ELEVATOR = register("light_gray_elevator", Elevator())
    val CYAN_ELEVATOR = register("cyan_elevator", Elevator())
    val BLUE_ELEVATOR = register("blue_elevator", Elevator())
    val PURPLE_ELEVATOR = register("purple_elevator", Elevator())
    val GREEN_ELEVATOR = register("green_elevator", Elevator())
    val BROWN_ELEVATOR = register("brown_elevator", Elevator())
    val RED_ELEVATOR = register("red_elevator", Elevator())
    val BLACK_ELEVATOR = register("black_elevator", Elevator())

    override fun <E : Block> register(string: String, entry: E): E {
        return register(string, entry, true)
    }

    fun registerBlockGenerator(string: String, entry: BlockGenerator): BlockGenerator {
        return register(string, entry).also(blockGenerators::add)
    }

    fun <E : FlowableFluid> registerFluidBlock(string: String, entry: E): FluidBlock {
        return register(string, FluidBlock(entry, FabricBlockSettings.copy(Blocks.LAVA)), false)
    }

    fun <E : Block> register(string: String, entry: E, hasBlockItem: Boolean): E {
        if(hasBlockItem) {
            ItemCompendium.registerBlockItem(string, entry)
        }
        return super.register(ModIdentifier(string), entry)
    }

    override fun initializeClient() {
        super.initializeClient()
        BlockRenderLayerMap.INSTANCE.putBlock(DRAWBRIDGE, RenderLayer.getCutoutMipped())
        BlockRenderLayerMap.INSTANCE.putBlock(VACUUM_HOPPER, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(BIG_TORCH, RenderLayer.getCutoutMipped())
        BlockRenderLayerMap.INSTANCE.putBlock(COOLER, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(WITHER_PROOF_GLASS, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(ENTANGLED_TANK, RenderLayer.getCutoutMipped())
        ModelLoadingRegistry.INSTANCE.registerVariantProvider {
            ModelVariantProvider { modelIdentifier, _ ->
                if(modelIdentifier.namespace == KibeMod.MOD_ID) {
                    when (modelIdentifier.path) {
                        "drawbridge" -> return@ModelVariantProvider DrawbridgeCustomModel()
                        "tank" -> if(modelIdentifier.variant != "inventory") return@ModelVariantProvider TankCustomModel()
                    }
                }
                return@ModelVariantProvider null
            }
        }
    }

}