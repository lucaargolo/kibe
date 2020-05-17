package io.github.lucaargolo.kibe.items.entangled

import io.github.lucaargolo.kibe.MOD_ID
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import java.awt.Color
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.util.*
import java.util.function.Supplier

class EntangledBagBakedModel: BakedModel, FabricBakedModel {

    override fun isVanillaAdapter(): Boolean = false

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {

        var color = Color(255, 255, 255, 255).rgb
        val emitter = context.emitter

        context.pushTransform { quad ->
            quad.spriteColor(0, color, color, color, color)
            true
        }

        val background = ModelIdentifier(Identifier(MOD_ID, "entangled_bag_background"), "inventory")
        val backgroundModel = MinecraftClient.getInstance().bakedModelManager.getModel(background)
        backgroundModel.getQuads(null, null, randSupplier.get()).forEach { q ->
            emitter.fromVanilla(q.vertexData, 0, true)
            emitter.emit()
        }

        val core =
            if(stack.hasTag() && stack.tag!!.contains("key") && stack.tag!!.getString("key") != "global")
                ModelIdentifier(Identifier(MOD_ID, "entangled_bag_diamond_core"), "inventory")
            else
                ModelIdentifier(Identifier(MOD_ID, "entangled_bag_gold_core"), "inventory")
        val coreModel = MinecraftClient.getInstance().bakedModelManager.getModel(core)
        coreModel.getQuads(null, null, randSupplier.get()).forEach { q ->
            emitter.fromVanilla(q.vertexData, 0, true)
            emitter.emit()
        }

        context.popTransform()

        if(stack.hasTag() && stack.tag!!.contains("rune1")) {
            var sumr = 0
            var sumg = 0
            var sumb = 0
            (1..8).forEach {
                val dye = DyeColor.byName(stack.tag!!.getString("rune$it"), DyeColor.WHITE)
                val dyeColor = Color(dye.materialColor.color)
                sumr += dyeColor.red
                sumg += dyeColor.green
                sumb += dyeColor.blue
            }
            color = Color(sumr/8, sumg/8, sumb/8, 255).rgb
        }
        context.pushTransform { quad ->
            quad.spriteColor(0, color, color, color, color)
            true
        }
        val ring = ModelIdentifier(Identifier(MOD_ID, "entangled_bag_ring"), "inventory")
        val ringModel = MinecraftClient.getInstance().bakedModelManager.getModel(ring)
        ringModel.getQuads(null, null, randSupplier.get()).forEach { q ->
            emitter.fromVanilla(q.vertexData, 0, true)
            emitter.emit()
        }
        context.popTransform()
    }


    override fun emitBlockQuads(p0: BlockRenderView?, p1: BlockState?, p2: BlockPos?, p3: Supplier<Random>?, p4: RenderContext?) {}

    @Throws(IOException::class)
    private fun getReaderForResource(location: Identifier): Reader {
        val file = Identifier(location.namespace, location.path + ".json")
        val resource = MinecraftClient.getInstance().resourceManager.getResource(file)
        return BufferedReader(InputStreamReader(resource.inputStream, Charsets.UTF_8))
    }

    override fun getItemPropertyOverrides(): ModelItemPropertyOverrideList = ModelItemPropertyOverrideList.EMPTY

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun getSprite() = null

    override fun hasDepth(): Boolean = false

    override fun getTransformation(): ModelTransformation? = loadTransformFromJson(Identifier("minecraft:models/item/generated"))

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