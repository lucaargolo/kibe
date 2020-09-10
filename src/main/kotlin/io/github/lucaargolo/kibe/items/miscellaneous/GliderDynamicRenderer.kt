package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.items.getItemId
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.util.*

class GliderDynamicRenderer: BuiltinItemRendererRegistry.DynamicItemRenderer {

    @Throws(IOException::class)
    private fun getReaderForResource(location: Identifier): Reader {
        val file = Identifier(location.namespace, location.path + ".json")
        val resource = MinecraftClient.getInstance().resourceManager.getResource(file)
        return BufferedReader(InputStreamReader(resource.inputStream, Charsets.UTF_8))
    }

    private fun loadTransformFromJson(location: Identifier): ModelTransformation? {
        return try {
            JsonUnbakedModel.deserialize(getReaderForResource(location)).transformations
        } catch (exception: IOException) {
            exception.printStackTrace()
            null
        }
    }

    private val deployedTransform: ModelTransformation? by lazy {
        loadTransformFromJson(Identifier("kibe:models/item/glider"))
    }

    private val itemTransform: ModelTransformation? by lazy {
        loadTransformFromJson(Identifier("minecraft:models/item/generated"))
    }

    override fun render(stack: ItemStack, mode: ModelTransformation.Mode, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, lightmap: Int, overlay: Int) {

        val isEnabled = Glider.isEnabled(stack)
        val isGui = (mode == ModelTransformation.Mode.GUI || mode == ModelTransformation.Mode.FIXED)
        val force3d = (mode == ModelTransformation.Mode.GROUND || mode == ModelTransformation.Mode.HEAD || mode == ModelTransformation.Mode.NONE)

        matrixStack.pop()
        matrixStack.push()

        val leftHanded = (mode == ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND || mode == ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND)
        if(force3d || (isEnabled && !isGui)) {
            deployedTransform?.getTransformation(mode)?.apply(leftHanded, matrixStack)
        }else{
            itemTransform?.getTransformation(mode)?.apply(leftHanded, matrixStack)
        }

        matrixStack.translate(-0.5, -0.5, -0.5)

        val cutoutBuffer = vertexConsumerProvider.getBuffer(RenderLayer.getCutout())
        val itemId = getItemId(stack.item) ?: Identifier(MOD_ID, "white_glider")

        if(force3d || (isEnabled && !isGui)) {
            val handleIdentifier = ModelIdentifier(Identifier(MOD_ID, "glider_handle"), "inventory")
            val handleModel = MinecraftClient.getInstance().bakedModelManager.getModel(handleIdentifier)

            handleModel.getQuads(null, null, Random()).forEach { q ->
                cutoutBuffer.quad(matrixStack.peek(), q, 1f, 1f, 1f, lightmap, overlay)
            }

            val gliderIdentifier = ModelIdentifier(Identifier(MOD_ID, itemId.path + "_active"), "inventory")
            val gliderModel = MinecraftClient.getInstance().bakedModelManager.getModel(gliderIdentifier)

            gliderModel.getQuads(null, null, Random()).forEach { q ->
                cutoutBuffer.quad(matrixStack.peek(), q, 1f, 1f, 1f, lightmap, overlay)
            }
        }else {
            val statusId = if(isEnabled) Identifier(MOD_ID, "glider_active") else Identifier(MOD_ID, itemId.path + "_inactive")
            val invIdentifier = ModelIdentifier(statusId, "inventory")
            val invModel = MinecraftClient.getInstance().bakedModelManager.getModel(invIdentifier)

            invModel.getQuads(null, null, Random()).forEach { q ->
                cutoutBuffer.quad(matrixStack.peek(), q, 1f, 1f, 1f, lightmap, overlay)
            }
        }

    }

}