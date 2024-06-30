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
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

class EntangledChestBlockItemDynamicRenderer: BuiltinItemRendererRegistry.DynamicItemRenderer {

    override fun render(stack: ItemStack, mode: ModelTransformationMode, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, lightmap: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()
        val world = client.world ?: return

        val dummyChest = EntangledChestEntity(BlockPos.ORIGIN, BlockCompendium.ENTANGLED_CHEST.defaultState)
        stack.components.get(DataComponentTypes.BLOCK_ENTITY_DATA)?.copyNbt()?.let { dummyChest.readClientNbt(it, world.registryManager) }
        val context = BlockEntityRendererFactory.Context(client.blockEntityRenderDispatcher, client.blockRenderManager, client.itemRenderer, client.entityRenderDispatcher, client.entityModelLoader, client.textRenderer)
        val dummyRenderer = EntangledChestEntityRenderer(context)
        dummyRenderer.render(dummyChest, client.renderTickCounter.getTickDelta(true), matrixStack, vertexConsumerProvider, lightmap, overlay)
    }
}