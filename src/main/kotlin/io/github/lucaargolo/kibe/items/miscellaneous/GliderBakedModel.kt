package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.items.getItemId
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.impl.client.indigo.renderer.render.ItemRenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import java.awt.Color
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.lang.Exception
import java.util.*
import java.util.function.Supplier

class GliderBakedModel: BakedModel, FabricBakedModel {

    override fun isVanillaAdapter(): Boolean = false

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {

        val isEnabled = Glider.isEnabled(stack)
        var isGui = false
        var force3d = true

        if(context is ItemRenderContext) {
            try {
                val transformField = context.javaClass.getDeclaredField("transformMode")
                transformField.isAccessible = true
                val transformMode = transformField.get(context) as ModelTransformation.Mode

                val matrixStackField = context.javaClass.getDeclaredField("matrixStack")
                matrixStackField.isAccessible = true
                val matrixStack = matrixStackField.get(context) as MatrixStack

                matrixStack.pop()
                matrixStack.push()

                isGui = (transformMode == ModelTransformation.Mode.GUI || transformMode == ModelTransformation.Mode.FIXED)
                force3d = (transformMode == ModelTransformation.Mode.GROUND || transformMode == ModelTransformation.Mode.HEAD || transformMode == ModelTransformation.Mode.NONE)

                val leftHanded = (transformMode == ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND || transformMode == ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND)
                if(force3d || (isEnabled && !isGui)) {
                    deployedTransform?.getTransformation(transformMode)?.apply(leftHanded, matrixStack)
                }else{
                    itemTransform?.getTransformation(transformMode)?.apply(leftHanded, matrixStack)
                }

                matrixStack.translate(-0.5, -0.5, -0.5)

                val matrixField = context.javaClass.getDeclaredField("matrix")
                matrixField.isAccessible = true
                matrixField.set(context, matrixStack.peek().model)

                val normalMatrixField = Class.forName("net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractRenderContext").getDeclaredField("normalMatrix")
                normalMatrixField.isAccessible = true
                normalMatrixField.set(context, matrixStack.peek().normal)

            }catch (e: Exception) { }
        }

        val color = Color(255, 255, 255, 255).rgb
        val emitter = context.emitter

        context.pushTransform { quad ->
            quad.spriteColor(0, color, color, color, color)
            true
        }

        val itemId = getItemId(stack.item) ?: Identifier(MOD_ID, "white_glider")

        if(force3d || (isEnabled && !isGui)) {

            val handleIdentifier = ModelIdentifier(Identifier(MOD_ID, "glider_handle"), "inventory")
            val handleModel = MinecraftClient.getInstance().bakedModelManager.getModel(handleIdentifier)

            handleModel.getQuads(null, null, randSupplier.get()).forEach { q ->
                emitter.fromVanilla(q.vertexData, 0, true)
                emitter.emit()
            }

            val gliderIdentifier = ModelIdentifier(Identifier(MOD_ID, itemId.path+"_active"), "inventory")
            val gliderModel = MinecraftClient.getInstance().bakedModelManager.getModel(gliderIdentifier)

            gliderModel.getQuads(null, null, randSupplier.get()).forEach { q ->
                emitter.fromVanilla(q.vertexData, 0, true)
                emitter.emit()
            }

        }else{
            val statusId = if(isEnabled) Identifier(MOD_ID, "glider_active") else Identifier(MOD_ID, itemId.path+"_inactive")
            val invIdentifier = ModelIdentifier(statusId, "inventory")
            val invModel = MinecraftClient.getInstance().bakedModelManager.getModel(invIdentifier)

            invModel.getQuads(null, null, randSupplier.get()).forEach { q ->
                emitter.fromVanilla(q.vertexData, 0, true)
                emitter.emit()
            }
        }

        context.popTransform()
    }


    override fun emitBlockQuads(p0: BlockRenderView?, p1: BlockState?, p2: BlockPos?, p3: Supplier<Random>?, p4: RenderContext?) {}

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun getSprite() = null

    override fun hasDepth(): Boolean = false

    private val deployedTransform: ModelTransformation? by lazy {
        loadTransformFromJson(Identifier("kibe:models/item/glider"))
    }

    private val itemTransform: ModelTransformation? by lazy {
        loadTransformFromJson(Identifier("minecraft:models/item/generated"))
    }

    override fun getTransformation(): ModelTransformation? = itemTransform

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

    @Throws(IOException::class)
    private fun getReaderForResource(location: Identifier): Reader {
        val file = Identifier(location.namespace, location.path + ".json")
        val resource = MinecraftClient.getInstance().resourceManager.getResource(file)
        return BufferedReader(InputStreamReader(resource.inputStream, Charsets.UTF_8))
    }

}