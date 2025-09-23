package io.github.lucaargolo.kibe.client.item

import io.github.lucaargolo.kibe.item.Glider
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader

class GliderDynamicRenderer: BuiltinItemRendererRegistry.DynamicItemRenderer {

    @Throws(IOException::class, NoSuchElementException::class)
    private fun getReaderForResource(location: Identifier): Reader {
        val file = Identifier.of(location.namespace, location.path + ".json")
        val resource = MinecraftClient.getInstance().resourceManager.getResource(file).get()
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
        loadTransformFromJson(Identifier.of("kibe:models/item/glider"))
    }

    private val itemTransform: ModelTransformation? by lazy {
        loadTransformFromJson(Identifier.of("minecraft:models/item/generated"))
    }

    override fun render(stack: ItemStack, mode: ModelTransformationMode, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, lightmap: Int, overlay: Int) {

        val isEnabled = Glider.isEnabled(stack)
        val isGui = (mode == ModelTransformationMode.GUI || mode == ModelTransformationMode.FIXED)
        val force3d = (mode == ModelTransformationMode.GROUND || mode == ModelTransformationMode.HEAD || mode == ModelTransformationMode.NONE)

        matrixStack.pop()
        matrixStack.push()

        val leftHanded = (mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND)
        if(force3d || (isEnabled && !isGui)) {
            deployedTransform?.getTransformation(mode)?.apply(leftHanded, matrixStack)
        }else{
            itemTransform?.getTransformation(mode)?.apply(leftHanded, matrixStack)
        }

        matrixStack.translate(-0.5, -0.5, -0.5)

        val cutoutBuffer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutout(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE))
        val itemId = Registries.ITEM.getId(stack.item)

        if(force3d || (isEnabled && !isGui)) {
            val handleIdentifier = ModelIdentifier.ofInventoryVariant(ModIdentifier.of("item/glider_handle"))
            val handleModel = MinecraftClient.getInstance().bakedModelManager.getModel(handleIdentifier)

            handleModel.getQuads(null, null, Random.create()).forEach { q ->
                cutoutBuffer.quad(matrixStack.peek(), q, 1f, 1f, 1f, 1f, lightmap, overlay)
            }

            val gliderIdentifier = ModelIdentifier.ofInventoryVariant(ModIdentifier.of("item/"+itemId.path + "_active"))
            val gliderModel = MinecraftClient.getInstance().bakedModelManager.getModel(gliderIdentifier)

            gliderModel.getQuads(null, null, Random.create()).forEach { q ->
                cutoutBuffer.quad(matrixStack.peek(), q, 1f, 1f, 1f, 1f, lightmap, overlay)
            }
        }else {
            val statusId = if(isEnabled) ModIdentifier.of("item/glider_active") else ModIdentifier.of("item/"+itemId.path + "_inactive")
            val statusIdentifier = ModelIdentifier.ofInventoryVariant(statusId)
            val invModel = MinecraftClient.getInstance().bakedModelManager.getModel(statusIdentifier)

            invModel.getQuads(null, null, Random.create()).forEach { q ->
                cutoutBuffer.quad(matrixStack.peek(), q, 1f, 1f, 1f, 1f, lightmap, overlay)
            }
        }

    }

}