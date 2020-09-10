package io.github.lucaargolo.kibe.items.entangledchest

import io.github.lucaargolo.kibe.blocks.ENTANGLED_CHEST
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChest
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChestEntity
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChestEntityRenderer
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack

class EntangledChestBlockItemDynamicRenderer: BuiltinItemRendererRegistry.DynamicItemRenderer {

    override fun render(stack: ItemStack, mode: ModelTransformation.Mode, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, lightmap: Int, overlay: Int) {
        val dummyChest = EntangledChestEntity(ENTANGLED_CHEST as EntangledChest)
        stack.tag?.getCompound("BlockEntityTag")?.let { dummyChest.fromClientTag(it) }

        val dummyRenderer = EntangledChestEntityRenderer(BlockEntityRenderDispatcher.INSTANCE)
        dummyRenderer.render(dummyChest, MinecraftClient.getInstance().tickDelta, matrixStack, vertexConsumerProvider, lightmap, overlay)
    }
}