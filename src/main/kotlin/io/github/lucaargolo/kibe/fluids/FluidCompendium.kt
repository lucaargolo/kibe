package io.github.lucaargolo.kibe.fluids

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.fluids.miscellaneous.LiquidXpFluid
import io.github.lucaargolo.kibe.fluids.miscellaneous.ModdedFluid
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.BucketItem
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockRenderView
import java.util.function.Function

val fluidRegistry = mutableMapOf<Identifier, ModdedFluid>()

val LIQUID_XP = register(Identifier(MOD_ID, "liquid_xp"), LiquidXpFluid.Still())
val LIQUID_XP_FLOWING = register(Identifier(MOD_ID, "flowing_liquid_xp"), LiquidXpFluid.Flowing())

private fun register(identifier: Identifier, fluid: ModdedFluid): ModdedFluid {
    fluidRegistry[identifier] = fluid
    return fluid
}

fun getFluidBucket(fluid: Fluid): Item {
    val fluidIdentifier = Registry.FLUID.getId(fluid)
    return Registry.ITEM.get(Identifier(fluidIdentifier.namespace, "${fluidIdentifier.path.replace("flowing_", "")}_bucket"))
}

fun getFluidBlock(fluid: Fluid): Block {
    val fluidIdentifier = Registry.FLUID.getId(fluid)
    return Registry.BLOCK.get(Identifier(fluidIdentifier.namespace, fluidIdentifier.path.replace("flowing_", "")))
}

fun initFluids() {
    fluidRegistry.forEach{
        val identifierStill = it.key
        val fluidStill = it.value
        if(!identifierStill.path.startsWith("flowing_")) {
            val registeredFluid = Registry.register(Registry.FLUID, identifierStill, fluidStill)
            val identifierFlowing = Identifier(MOD_ID, "flowing_" + identifierStill.path)
            val fluidFlowing = fluidRegistry[identifierFlowing]
            Registry.register(Registry.FLUID, identifierFlowing, fluidFlowing)
            Registry.register(Registry.ITEM, Identifier(it.key.namespace, "${identifierStill.path}_bucket"), BucketItem(fluidStill, Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)))
            Registry.register(Registry.BLOCK, it.key, object : FluidBlock(registeredFluid as FlowableFluid, FabricBlockSettings.copy(Blocks.LAVA)) {})
            FluidKeys.put(registeredFluid, registeredFluid.key)
        }
    }
}

fun initFluidsClient() {

    fluidRegistry.forEach{
        val identifierStill = it.key
        val fluidStill = it.value
        if(!identifierStill.path.startsWith("flowing_")) {
            val identifierFlowing = Identifier(MOD_ID, "flowing_" + identifierStill.path)
            val fluidFlowing = fluidRegistry[identifierFlowing]
            setupFluidRendering(fluidStill, fluidFlowing, it.key, 0xFFFFFF)
            BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), fluidStill, fluidFlowing)
        }
    }

}

@Suppress("SameParameterValue")
private fun setupFluidRendering(still: Fluid?, flowing: Fluid?, textureFluidId: Identifier, color: Int) {
    val stillSpriteId = Identifier(textureFluidId.namespace, "block/" + textureFluidId.path + "_still")
    val flowingSpriteId = Identifier(textureFluidId.namespace, "block/" + textureFluidId.path + "_flow")

    ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
        .register(ClientSpriteRegistryCallback { _: SpriteAtlasTexture?, registry: ClientSpriteRegistryCallback.Registry ->
            registry.register(stillSpriteId)
            registry.register(flowingSpriteId)
        })

    val fluidId = Registry.FLUID.getId(still)
    val listenerId = Identifier(fluidId.namespace, fluidId.path + "_reload_listener")
    val fluidSprites = arrayOf<Sprite?>(null, null)

    ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(object :
        SimpleSynchronousResourceReloadListener {
        override fun getFabricId() = listenerId

        override fun apply(resourceManager: ResourceManager?) {
            val atlas: Function<Identifier, Sprite> =
                MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
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
}