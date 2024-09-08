package io.github.lucaargolo.kibe.block

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.client.model.DrawbridgeCustomModel
import io.github.lucaargolo.kibe.client.model.TankCustomModel
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.item.BucketItem
import net.minecraft.sound.BlockSoundGroup
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.ObjectHolderDelegate


object BlockCompendium : RegistryCompendium<Block>(ForgeRegistries.BLOCKS) {

    val BLOCK_GENERATORS: Array<BlockGenerator>
        get() = blockGenerators.map(ObjectHolderDelegate<BlockGenerator>::get).toTypedArray()
    private val blockGenerators = mutableListOf<ObjectHolderDelegate<BlockGenerator>>()

    val FLUID_BLOCKS: Map<Fluid, FluidBlock>
        get() = fluidBlocks.mapKeys { e -> e.key.get() }.mapValues { e -> e.value.get() }
    private val fluidBlocks = mutableMapOf<ObjectHolderDelegate<out Fluid>, ObjectHolderDelegate<FluidBlock>>()

    val CURSED_DIRT by register("cursed_dirt", { CursedDirt() })
    val REDSTONE_TIMER by register("redstone_timer", { RedstoneTimer() })

    val STONE_SPIKES by register("stone_spikes", { Spikes(Spikes.Type.STONE, FabricBlockSettings.copyOf(Blocks.STONE)) })
    val IRON_SPIKES by register("iron_spikes", { Spikes(Spikes.Type.IRON, FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)) })
    val GOLD_SPIKES by register("gold_spikes", { Spikes(Spikes.Type.GOLD, FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK)) })
    val DIAMOND_SPIKES by register("diamond_spikes", { Spikes(Spikes.Type.DIAMOND, FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK)) })

    val REGULAR_CONVEYOR_BELT by register("regular_conveyor_belt", { ConveyorBelt(0.050) })
    val FAST_CONVEYOR_BELT by register("fast_conveyor_belt", { ConveyorBelt(0.1) })
    val EXPRESS_CONVEYOR_BELT by register("express_conveyor_belt", { ConveyorBelt(0.2) })

    val ENTANGLED_TANK by register("entangled_tank", { EntangledTank() }, false)
    val ENTANGLED_CHEST by register("entangled_chest", { EntangledChest() }, false)
    val TRASH_CAN by register("trash_can", { TrashCan() })
    val VACUUM_HOPPER by register("vacuum_hopper", { VacuumHopper() })
    val BIG_TORCH by register("big_torch", { BigTorch() })
    val COOLER by register("cooler", { Cooler() }, false)
    val DRAWBRIDGE by register("drawbridge", { Drawbridge() })

    val OBSIDIAN_SAND by register("obsidian_sand", { FallingBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)) })
    val WITHER_PROOF_BLOCK by register("wither_proof_block", { Block(FabricBlockSettings.copyOf(Blocks.OBSIDIAN)) })
    val WITHER_PROOF_SAND by register("wither_proof_sand", { FallingBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).sounds(BlockSoundGroup.SAND)) })
    val WITHER_PROOF_GLASS by register("wither_proof_glass", { GlassBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).nonOpaque()) })
    val WITHER_BUILDER by register("wither_builder", { WitherBuilder() })

    val PLACER by register("placer", { Placer() })
    val BREAKER by register("breaker", { Breaker() })

    val HEATER by register("heater", { Heater() })
    val DEHUMIDIFIER by register("dehumidifier", { Dehumidifier() })

    val COBBLESTONE_GENERATOR_MK1 by registerBlockGenerator("cobblestone_generator_mk1", { BlockGenerator(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.01f) })
    val COBBLESTONE_GENERATOR_MK2 by registerBlockGenerator("cobblestone_generator_mk2", { BlockGenerator(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.04f) })
    val COBBLESTONE_GENERATOR_MK3 by registerBlockGenerator("cobblestone_generator_mk3", { BlockGenerator(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.16f) })
    val COBBLESTONE_GENERATOR_MK4 by registerBlockGenerator("cobblestone_generator_mk4", { BlockGenerator(FabricBlockSettings.copyOf(Blocks.EMERALD_BLOCK).luminance(4), Blocks.COBBLESTONE, 0.64f) })
    val COBBLESTONE_GENERATOR_MK5 by registerBlockGenerator("cobblestone_generator_mk5", { BlockGenerator(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK).luminance(4), Blocks.COBBLESTONE, 2.56f) })

    val BASALT_GENERATOR_MK1 by registerBlockGenerator("basalt_generator_mk1", { BlockGenerator(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).luminance(4), Blocks.BASALT, 0.01f) })
    val BASALT_GENERATOR_MK2 by registerBlockGenerator("basalt_generator_mk2", { BlockGenerator(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).luminance(4), Blocks.BASALT, 0.04f) })
    val BASALT_GENERATOR_MK3 by registerBlockGenerator("basalt_generator_mk3", { BlockGenerator(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).luminance(4), Blocks.BASALT, 0.16f) })
    val BASALT_GENERATOR_MK4 by registerBlockGenerator("basalt_generator_mk4", { BlockGenerator(FabricBlockSettings.copyOf(Blocks.EMERALD_BLOCK).luminance(4), Blocks.BASALT, 0.64f) })
    val BASALT_GENERATOR_MK5 by registerBlockGenerator("basalt_generator_mk5", { BlockGenerator(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK).luminance(4), Blocks.BASALT, 2.56f) })

    val LIGHT_SOURCE by register("light_source", { LightSource() }, false)
    val CHUNK_LOADER by register("chunk_loader", { ChunkLoader() })
    val TANK by register("tank", { Tank() }, false)
    val XP_SHOWER by register("xp_shower", { XpShower() })
    val XP_DRAIN by register("xp_drain", { XpDrain() })
    val IGNITER by register("igniter", { Igniter() })
    val FLUID_HOPPER by register("fluid_hopper", { FluidHopper() })

    val WHITE_ELEVATOR by register("white_elevator", { Elevator() })
    val ORANGE_ELEVATOR by register("orange_elevator", { Elevator() })
    val MAGENTA_ELEVATOR by register("magenta_elevator", { Elevator() })
    val LIGHT_BLUE_ELEVATOR by register("light_blue_elevator", { Elevator() })
    val YELLOW_ELEVATOR by register("yellow_elevator", { Elevator() })
    val LIME_ELEVATOR by register("lime_elevator", { Elevator() })
    val PINK_ELEVATOR by register("pink_elevator", { Elevator() })
    val GRAY_ELEVATOR by register("gray_elevator", { Elevator() })
    val LIGHT_GRAY_ELEVATOR by register("light_gray_elevator", { Elevator() })
    val CYAN_ELEVATOR by register("cyan_elevator", { Elevator() })
    val BLUE_ELEVATOR by register("blue_elevator", { Elevator() })
    val PURPLE_ELEVATOR by register("purple_elevator", { Elevator() })
    val GREEN_ELEVATOR by register("green_elevator", { Elevator() })
    val BROWN_ELEVATOR by register("brown_elevator", { Elevator() })
    val RED_ELEVATOR by register("red_elevator", { Elevator() })
    val BLACK_ELEVATOR by register("black_elevator", { Elevator() })

    override fun <E : Block> register(string: String, entry: () -> E): ObjectHolderDelegate<E> {
        return register(string, entry, true)
    }

    fun registerBlockGenerator(string: String, entry: () -> BlockGenerator): ObjectHolderDelegate<BlockGenerator> {
        return register(string, entry).also(blockGenerators::add)
    }

    fun <E : FlowableFluid> registerFluidBlock(string: String, entry: ObjectHolderDelegate<E>): ObjectHolderDelegate<FluidBlock> {
        val blockDelegate = register(string, { FluidBlock(entry, FabricBlockSettings.copy(Blocks.LAVA)) }, false)
        fluidBlocks[entry] = blockDelegate
        return blockDelegate
    }

    fun <E : Block> register(string: String, entry: () -> E, hasBlockItem: Boolean): ObjectHolderDelegate<E> {
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