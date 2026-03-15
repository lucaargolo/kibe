package io.github.lucaargolo.kibe.client.item

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.blockentity.EntangledTankEntity
import io.github.lucaargolo.kibe.client.blockentity.EntangledTankEntityRenderer
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random

class EntangledTankBlockItemDynamicRenderer: BuiltinItemRendererRegistry.DynamicItemRenderer {

    private val dummyRenderer = EntangledTankEntityRenderer(BlockEntityRendererFactory.Context(MinecraftClient.getInstance().blockEntityRenderDispatcher, MinecraftClient.getInstance().blockRenderManager, MinecraftClient.getInstance().itemRenderer, MinecraftClient.getInstance().entityRenderDispatcher, MinecraftClient.getInstance().entityModelLoader, MinecraftClient.getInstance().textRenderer))

    override fun render(stack: ItemStack, mode: ModelTransformationMode, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, lightmap: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()
        val dummyTank = EntangledTankEntity(client.player?.blockPos ?: BlockPos.ORIGIN, BlockCompendium.ENTANGLED_TANK.defaultState)
        dummyTank.readComponents(stack)
        dummyTank.lastRenderedFluid = dummyTank.getTank().amount / 81000f

        dummyRenderer.render(dummyTank, client.renderTickCounter.getTickDelta(true), matrixStack, vertexConsumerProvider, lightmap, overlay)

        val tankGlassIdentifier = ModelIdentifier(ModIdentifier.of("entangled_tank"), "level=0")
        val tankGlassModel = client.bakedModelManager.getModel(tankGlassIdentifier)

        val cutoutBuffer = vertexConsumerProvider.getBuffer(RenderLayer.getCutout())
        tankGlassModel.getQuads(null, null, Random.create()).forEach { q ->
            cutoutBuffer.quad(matrixStack.peek(), q, 1f, 1f, 1f, 1f, lightmap, overlay)
        }

    }

}