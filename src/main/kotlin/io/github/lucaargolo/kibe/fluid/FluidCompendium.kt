package io.github.lucaargolo.kibe.fluid

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.fluid.miscellaneous.LiquidXpFluid
import io.github.lucaargolo.kibe.fluid.miscellaneous.ModdedFluid
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.utils.ModIdentifier
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.Sprite
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.registry.Registries
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView

object FluidCompendium: RegistryCompendium<Fluid>(Registries.FLUID) {

    val LIQUID_XP by register("liquid_xp", LiquidXpFluid.Still())
    val LIQUID_XP_FLOWING by register("flowing_liquid_xp", LiquidXpFluid.Flowing())
    val EXPERIENCE = registerTag(Identifier.of("c", "experience"), LIQUID_XP)

    override fun initialize() {
        super.initialize()
        setupFluid(LIQUID_XP, LIQUID_XP_FLOWING, "liquid_xp")
    }

    override fun initializeClient() {
        super.initializeClient()
        setupFluidClient(LIQUID_XP, LIQUID_XP_FLOWING, "liquid_xp", 0xFFFFFF)
    }

    private fun setupFluid(still: ModdedFluid, flowing: ModdedFluid, name: String) {
        val id = ModIdentifier.of(name)
        val fluidBlock by BlockCompendium.registerFluidBlock(id, still)
        val fluidBucket by ItemCompendium.registerBucketItem(id, still)
        still.fluidBlock = fluidBlock
        still.fluidBucket = fluidBucket
        flowing.fluidBlock = fluidBlock
        flowing.fluidBucket = fluidBucket
    }

    private fun setupFluidClient(still: ModdedFluid, flowing: ModdedFluid, name: String, color: Int) {
        val stillSpriteId = ModIdentifier.of("block/" + name + "_still")
        val flowingSpriteId = ModIdentifier.of("block/" + name + "_flow")
        val listenerId = ModIdentifier.of(name + "_reload_listener")
        val fluidSprites = arrayOf<Sprite?>(null, null)

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(object :
            SimpleSynchronousResourceReloadListener {

            override fun getFabricId() = listenerId

            override fun reload(resourceManager: ResourceManager?) {
                val client = MinecraftClient.getInstance()
                val atlas = client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                fluidSprites[0] = atlas.apply(stillSpriteId)
                fluidSprites[1] = atlas.apply(flowingSpriteId)
            }
        })

        val renderHandler: FluidRenderHandler = object : FluidRenderHandler {
            override fun getFluidSprites(view: BlockRenderView?, pos: BlockPos?, state: FluidState?) = fluidSprites
            override fun getFluidColor(view: BlockRenderView?, pos: BlockPos?, state: FluidState?) = color
        }

        FluidRenderHandlerRegistry.INSTANCE.register(still, renderHandler)
        FluidRenderHandlerRegistry.INSTANCE.register(flowing, renderHandler)

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), still, flowing)
    }

}