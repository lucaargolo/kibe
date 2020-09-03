package io.github.lucaargolo.kibe.items.entangledchest

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.ENTANGLED_CHEST
import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChest
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChestEntity
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChestEntityRenderer
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTank
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntity
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.impl.client.indigo.renderer.render.ItemRenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
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
import java.util.*
import java.util.function.Supplier

class EntangledChestBlockItemBakedModel: BakedModel, FabricBakedModel {

    override fun isVanillaAdapter(): Boolean = false

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {

        try {
            val itemRenderContext: ItemRenderContext = context as? ItemRenderContext ?: return

            val matrixStackField = itemRenderContext.javaClass.getDeclaredField("matrixStack")
            matrixStackField.isAccessible = true
            val matrixStack = matrixStackField.get(itemRenderContext) as MatrixStack

            val vertexConsumerProviderField = itemRenderContext.javaClass.getDeclaredField("vertexConsumerProvider")
            vertexConsumerProviderField.isAccessible = true
            val vertexConsumerProvider = vertexConsumerProviderField.get(itemRenderContext) as VertexConsumerProvider

            val lightmapField = itemRenderContext.javaClass.getDeclaredField("lightmap")
            lightmapField.isAccessible = true
            val lightmap = lightmapField.get(itemRenderContext) as Int

            val overlayField = itemRenderContext.javaClass.getDeclaredField("overlay")
            overlayField.isAccessible = true
            val overlay = overlayField.get(itemRenderContext) as Int

            val dummyChest = EntangledChestEntity(ENTANGLED_CHEST as EntangledChest)
            stack.tag?.getCompound("BlockEntityTag")?.let { dummyChest.fromClientTag(it) }

            val dummyRenderer = EntangledChestEntityRenderer(BlockEntityRenderDispatcher.INSTANCE)
            dummyRenderer.render(dummyChest, MinecraftClient.getInstance().tickDelta, matrixStack, vertexConsumerProvider, lightmap, overlay)

        }catch (e: Exception) { }

    }

    override fun emitBlockQuads(p0: BlockRenderView?, p1: BlockState?, p2: BlockPos?, p3: Supplier<Random>?, p4: RenderContext?) {}

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun getSprite() = null

    override fun hasDepth(): Boolean = false

    private val transform: ModelTransformation? by lazy {
        loadTransformFromJson(Identifier("minecraft:models/block/block"))
    }

    override fun getTransformation(): ModelTransformation? = transform

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