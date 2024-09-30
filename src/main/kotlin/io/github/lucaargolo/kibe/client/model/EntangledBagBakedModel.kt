package io.github.lucaargolo.kibe.client.model

import io.github.lucaargolo.kibe.block.EntangledChest
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
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
import java.util.function.Function
import java.util.function.Supplier

class EntangledBagBakedModel: UnbakedModel, BakedModel, FabricBakedModel {

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
    override fun setParents(modelLoader: Function<Identifier, UnbakedModel>?) {}
    override fun bake(baker: Baker?, textureGetter: Function<SpriteIdentifier, Sprite>?, rotationContainer: ModelBakeSettings?) = this

    override fun isVanillaAdapter(): Boolean = false

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {

        val defaultMaterial = RendererAccess.INSTANCE.renderer?.materialFinder()?.find() ?: return
        var color = Color(255, 255, 255, 255).rgb
        val emitter = context.emitter

        context.pushTransform { quad ->
            quad.color(color, color, color, color)
            true
        }

        val background = ModIdentifier.of("item/entangled_bag_background")
        val backgroundModel = MinecraftClient.getInstance().bakedModelManager.getModel(background)
        backgroundModel.getQuads(null, null, randSupplier.get()).forEach { q ->
            emitter.fromVanilla(q, defaultMaterial, null)
            emitter.emit()
        }

        val core =
            if(stack.contains(ComponentTypeCompendium.ENTANGLED_KEY) && stack.get(ComponentTypeCompendium.ENTANGLED_KEY) != EntangledChest.DEFAULT_KEY)
                ModIdentifier.of("item/entangled_bag_diamond_core")
            else
                ModIdentifier.of("item/entangled_bag_gold_core")
        val coreModel = MinecraftClient.getInstance().bakedModelManager.getModel(core)

        coreModel.getQuads(null, null, randSupplier.get()).forEach { q ->
            emitter.fromVanilla(q, defaultMaterial, null)
            emitter.emit()
        }


        context.popTransform()

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
            color = Color(sumr/8, sumg/8, sumb/8, 255).rgb
        }
        context.pushTransform { quad ->
            quad.color(color, color, color, color)
            true
        }
        val ring = ModIdentifier.of("item/entangled_ring")
        val ringModel = MinecraftClient.getInstance().bakedModelManager.getModel(ring)
        ringModel.getQuads(null, null, randSupplier.get()).forEach { q ->
            emitter.fromVanilla(q, defaultMaterial, null)
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

}