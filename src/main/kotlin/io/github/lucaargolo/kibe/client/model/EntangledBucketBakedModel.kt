package io.github.lucaargolo.kibe.client.model

import com.google.common.base.Suppliers
import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.block.EntangledTank
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import io.github.lucaargolo.kibe.data.state.EntangledTankState
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingConstants
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.MissingSprite
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.awt.Color
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

class EntangledBucketBakedModel: UnbakedModel, BakedModel, FabricBakedModel {

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
    override fun setParents(modelLoader: Function<Identifier, UnbakedModel>?) {}
    override fun bake(baker: Baker?, textureGetter: Function<SpriteIdentifier, Sprite>?, rotationContainer: ModelBakeSettings?) = this

    override fun isVanillaAdapter(): Boolean = false

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {

        val background = ModelLoadingConstants.toResourceModelId(ModIdentifier.of("item/entangled_bucket_background"))
        val backgroundModel = MinecraftClient.getInstance().bakedModelManager.getModel(background)
        (backgroundModel as FabricBakedModel).emitItemQuads(stack, randSupplier, context)

        val colorCode = (stack.get(ComponentTypeCompendium.RUNE_SET) ?: KibeMod.DEFAULT_RUNE_SET).map(DyeColor::getId).joinToString(separator = "", transform = Integer::toHexString)
        val key = stack.get(ComponentTypeCompendium.ENTANGLED_KEY) ?: EntangledTank.DEFAULT_KEY

        EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.add(Pair(key, colorCode))
        val fluidInv = EntangledTankState.CLIENT_STATES[key]?.fluidInvMap?.get(colorCode) ?: object: SingleVariantStorage<FluidVariant>() {
            override fun getCapacity(variant: FluidVariant?) = 0L
            override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
        }
        val fluid = fluidInv.resource.fluid ?: Fluids.EMPTY

        if(fluid != Fluids.EMPTY) {
            val fluidRenderHandler: FluidRenderHandler? = FluidRenderHandlerRegistry.INSTANCE.get(fluid)
            val fluidIdentifier = ModelLoadingConstants.toResourceModelId(ModIdentifier.of("item/entangled_bucket_fluid"))
            val fluidModel = MinecraftClient.getInstance().bakedModelManager.getModel(fluidIdentifier)

            val fluidColor: Int = fluidRenderHandler?.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player!!.blockPos, fluid.defaultState) ?: 0xffffff
            val fluidSprite: Sprite = fluidRenderHandler?.getFluidSprites(MinecraftClient.getInstance().world, BlockPos.ORIGIN, fluid.defaultState)?.get(0) ?: MISSING_SPRITE.get();
            val colorInt = Color((fluidColor shr 16 and 255), (fluidColor shr 8 and 255), (fluidColor and 255)).rgb

            context.pushTransform { quad ->
                @Suppress("UnstableApiUsage")
                quad.nominalFace(GeometryHelper.lightFace(quad))
                quad.color(colorInt, colorInt, colorInt, colorInt)
                quad.spriteBake(fluidSprite, MutableQuadView.BAKE_LOCK_UV)
                true
            }

            val emitter = context.emitter
            fluidModel.getQuads(null, null, randSupplier.get()).forEach(Consumer { q: BakedQuad ->
                emitter.fromVanilla(q.vertexData, 0)
                emitter.emit()
            })
            context.popTransform()
        }

        val foreground = ModelLoadingConstants.toResourceModelId(ModIdentifier.of("item/entangled_bucket_foreground"))
        val foregroundModel = MinecraftClient.getInstance().bakedModelManager.getModel(foreground)
        (foregroundModel as FabricBakedModel).emitItemQuads(stack, randSupplier, context)

        val core = if(stack.contains(ComponentTypeCompendium.ENTANGLED_KEY) && stack.get(ComponentTypeCompendium.ENTANGLED_KEY) != EntangledTank.DEFAULT_KEY)
            ModIdentifier.of("item/entangled_bucket_diamond_core")
        else ModIdentifier.of("item/entangled_bucket_gold_core")
        val coreIdentifier = ModelLoadingConstants.toResourceModelId(core)
        val coreModel = MinecraftClient.getInstance().bakedModelManager.getModel(coreIdentifier)
        (coreModel as FabricBakedModel).emitItemQuads(stack, randSupplier, context)

        var color = Color.WHITE.rgb
        if(stack.contains(ComponentTypeCompendium.RUNE_SET)) {
            var sumr = 0
            var sumg = 0
            var sumb = 0
            stack.get(ComponentTypeCompendium.RUNE_SET)?.forEach { dye ->
                val dyeColor = Color(dye.mapColor.color)
                sumr += dyeColor.red
                sumg += dyeColor.green
                sumb += dyeColor.blue
            }
            color = Color(sumr / 8, sumg / 8, sumb / 8, 255).rgb
        }
        context.pushTransform { quad ->
            quad.color(color, color, color, color)
            true
        }
        val emitter = context.emitter
        val ring = ModelLoadingConstants.toResourceModelId(ModIdentifier.of("item/entangled_ring"))
        val ringModel = MinecraftClient.getInstance().bakedModelManager.getModel(ring)
        ringModel.getQuads(null, null, randSupplier.get()).forEach { q ->
            emitter.fromVanilla(q.vertexData, 0)
            emitter.emit()
        }
        context.popTransform()
    }


    override fun emitBlockQuads(p0: BlockRenderView?, p1: BlockState?, p2: BlockPos?, p3: Supplier<Random>?, p4: RenderContext?) {}

    @Throws(IOException::class, NoSuchElementException::class)
    private fun getReaderForResource(location: Identifier): Reader {
        val file = Identifier.of(location.namespace, location.path + ".json")
        val resource = MinecraftClient.getInstance().resourceManager.getResource(file).get()
        return BufferedReader(InputStreamReader(resource.inputStream, Charsets.UTF_8))
    }

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun getParticleSprite() = null

    override fun hasDepth(): Boolean = false

    override fun getTransformation(): ModelTransformation? = loadTransformFromJson(Identifier.of("minecraft:models/item/generated"))

    override fun useAmbientOcclusion(): Boolean = true

    override fun isSideLit(): Boolean = false

    override fun isBuiltin(): Boolean = false

    private fun loadTransformFromJson(location: Identifier): ModelTransformation? {
        return try {
            JsonUnbakedModel.deserialize(getReaderForResource(location)).transformations
        } catch (exception: IOException) {
            exception.printStackTrace()
            null
        }
    }

    companion object {
        private val MISSING_SPRITE: Supplier<Sprite> = Suppliers.memoize {
            val atlas = MinecraftClient.getInstance().bakedModelManager.getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
            atlas.getSprite(MissingSprite.getMissingSpriteId())
        }
    }

}