package io.github.lucaargolo.kibe.fluids

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.fluids.miscellaneous.LiquidXpFluid
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
import net.minecraft.container.PlayerContainer
import net.minecraft.fluid.BaseFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.BucketItem
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockRenderView
import java.util.function.Function


val fluidRegistry = mutableMapOf<Identifier, Pair<Fluid, Fluid>>()

val LIQUID_XP = register(Identifier(MOD_ID, "liquid_xp"), LiquidXpFluid.Still(), LiquidXpFluid.Flowing())

private fun register(identifier: Identifier, fluidStill: BaseFluid, fluidFlowing: BaseFluid): Identifier {
    fluidRegistry[identifier] = Pair(fluidStill, fluidFlowing)
    return identifier
}


fun getFluidStill(fluid: Identifier): Fluid? {
    return Registry.FLUID.get(fluid)
}

fun getFluidFlowing(fluid: Identifier): Fluid? {
    return Registry.FLUID.get(Identifier(fluid.namespace, "flowing_${fluid.path}"))
}

fun getFluidBucket(fluid: Identifier): Item? {
    return Registry.ITEM.get(Identifier(fluid.namespace, "${fluid.path}_bucket"))
}

fun getFluidBlock(fluid: Identifier): Block? {
    return Registry.BLOCK.get(fluid)
}

fun initFluids() {
    fluidRegistry.forEach{
        val baseFluid = Registry.register(Registry.FLUID, it.key, it.value.first)
        Registry.register(Registry.FLUID, Identifier(it.key.namespace, "flowing_${it.key.path}"), it.value.second)
        Registry.register(Registry.ITEM, Identifier(it.key.namespace, "${it.key.path}_bucket"), BucketItem(it.value.first, Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)))
        Registry.register(Registry.BLOCK, it.key, object: FluidBlock(baseFluid as BaseFluid, FabricBlockSettings.copy(Blocks.LAVA)) {})
    }
}

fun initFluidsClient() {
    fluidRegistry.forEach{
        setupFluidRendering(it.value.first, it.value.second, it.key, 0xFFFFFF)
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), it.value.first, it.value.second)
    }

}

@Suppress("SameParameterValue")
private fun setupFluidRendering(still: Fluid?, flowing: Fluid?, textureFluidId: Identifier, color: Int) {
    val stillSpriteId = Identifier(textureFluidId.namespace, "block/" + textureFluidId.path + "_still")
    val flowingSpriteId = Identifier(textureFluidId.namespace, "block/" + textureFluidId.path + "_flow")

    // If they're not already present, add the sprites to the block atlas
    ClientSpriteRegistryCallback.event(PlayerContainer.BLOCK_ATLAS_TEXTURE)
        .register(ClientSpriteRegistryCallback { _: SpriteAtlasTexture?, registry: ClientSpriteRegistryCallback.Registry ->
            registry.register(stillSpriteId)
            registry.register(flowingSpriteId)
        })

    val fluidId = Registry.FLUID.getId(still)
    val listenerId = Identifier(fluidId.namespace, fluidId.path + "_reload_listener")
    val fluidSprites = arrayOf<Sprite?>(null, null)
    ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(object : SimpleSynchronousResourceReloadListener {
        override fun getFabricId(): Identifier {
            return listenerId
        }

        /**
         * Get the sprites from the block atlas when resources are reloaded
         */
        override fun apply(resourceManager: ResourceManager?) {
            val atlas: Function<Identifier, Sprite> =
                MinecraftClient.getInstance().getSpriteAtlas(PlayerContainer.BLOCK_ATLAS_TEXTURE)
            fluidSprites[0] = atlas.apply(stillSpriteId)
            fluidSprites[1] = atlas.apply(flowingSpriteId)
        }
    })

    // The FluidRenderer gets the sprites and color from a FluidRenderHandler during rendering
    val renderHandler: FluidRenderHandler = object : FluidRenderHandler {
        override fun getFluidSprites(view: BlockRenderView, pos: BlockPos, state: FluidState): Array<Sprite?> {
            return fluidSprites
        }

        override fun getFluidColor(view: BlockRenderView, pos: BlockPos, state: FluidState): Int {
            return color
        }
    }

    FluidRenderHandlerRegistry.INSTANCE.register(still, renderHandler)
    FluidRenderHandlerRegistry.INSTANCE.register(flowing, renderHandler)
}