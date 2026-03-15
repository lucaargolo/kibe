package io.github.lucaargolo.kibe.client.item

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.blockentity.EntangledChestEntity
import io.github.lucaargolo.kibe.client.blockentity.EntangledChestEntityRenderer
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

class EntangledChestBlockItemDynamicRenderer: BuiltinItemRendererRegistry.DynamicItemRenderer {

    private val dummyRenderer = EntangledChestEntityRenderer(BlockEntityRendererFactory.Context(MinecraftClient.getInstance().blockEntityRenderDispatcher, MinecraftClient.getInstance().blockRenderManager, MinecraftClient.getInstance().itemRenderer, MinecraftClient.getInstance().entityRenderDispatcher, MinecraftClient.getInstance().entityModelLoader, MinecraftClient.getInstance().textRenderer))

    override fun render(stack: ItemStack, mode: ModelTransformationMode, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, lightmap: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()
        val dummyChest = EntangledChestEntity(BlockPos.ORIGIN, BlockCompendium.ENTANGLED_CHEST.defaultState)
        dummyChest.readComponents(stack)
        dummyRenderer.render(dummyChest, client.renderTickCounter.getTickDelta(true), matrixStack, vertexConsumerProvider, lightmap, overlay)
    }

}