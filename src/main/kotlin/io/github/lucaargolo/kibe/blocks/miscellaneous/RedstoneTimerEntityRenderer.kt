package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.MOD_ID
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.util.Identifier
import java.util.function.Function
import kotlin.math.ceil


class RedstoneTimerEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<RedstoneTimerEntity>(dispatcher) {

    var background: ModelPart = ModelPart(16, 16, 1, -13)
    var leftSolid: ModelPart = ModelPart(16, 16, 13, -1)
    var topSolid: ModelPart = ModelPart(16, 16, 3, 3)
    var top: ModelPart = ModelPart(16, 16, 0, -1)
    var bottom: ModelPart = ModelPart(16, 16, 0, -16)
    var bottomSolid: ModelPart = ModelPart(16, 16, 3, -9)
    var center: ModelPart = ModelPart(16, 16, 5, -1)
    var rightSolid: ModelPart = ModelPart(16, 16, 3, -1)
    var right: ModelPart = ModelPart(16, 16, 0, 0)
    var left: ModelPart = ModelPart(16, 16, 15, 0)

    init {
        background.addCuboid(1f, 1f, 1f, 0f, 14f, 14f)
        leftSolid.addCuboid(0f, 1f, 1f, 1f, 14f, 2f)
        topSolid.addCuboid(0f, 13f, 3f, 1f, 2f, 10f)
        top.addCuboid(0f, 15f, 0f, 0f, 1f, 16f)
        bottom.addCuboid(0f, 0f, 0f, 0f, 1f, 16f)
        bottomSolid.addCuboid(0f, 1f, 3f, 1f, 2f, 10f)
        center.addCuboid(0f, 5f, 5f, 1f, 6f, 6f)
        rightSolid.addCuboid(0f, 1f, 13f, 1f, 14f, 2f)
        right.addCuboid(0f, 1f, 15f, 0f, 14f,1f)
        left.addCuboid(0f, 1f, 0f, 0f, 14f, 1f)
    }


    override fun render(blockEntity: RedstoneTimerEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val spriteIdentifier = SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, Identifier("kibe:block/redstone_timer_"+ ceil(tickDelta*15).toInt()))
        val vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumers,
            Function { texture: Identifier? -> RenderLayer.getEntitySolid(texture) }
        )
        val lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.world, blockEntity.pos.up())
        
        matrices.push()
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180f));
        matrices.translate(0.0, -1.0, -1.0)
        renderThings(matrices, vertexConsumer, lightAbove, overlay)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f));
        matrices.translate(-1.0, 0.0, -1.0)
        renderThings(matrices, vertexConsumer, lightAbove, overlay)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f));
        matrices.translate(-1.0, 0.0, 0.0)
        renderThings(matrices, vertexConsumer, lightAbove, overlay)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f));
        matrices.translate(-1.0, 0.0, -1.0)
        renderThings(matrices, vertexConsumer, lightAbove, overlay)
        matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90f));
        matrices.translate(0.0, -1.0, 0.0)
        renderThings(matrices, vertexConsumer, lightAbove, overlay)
        matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180f));
        matrices.translate(-1.0, -1.0, 0.0)
        renderThings(matrices, vertexConsumer, lightAbove, overlay)

        matrices.pop()
    }

    private fun renderThings(matrices: MatrixStack, vertexConsumer: VertexConsumer, light: Int, overlay: Int) {
        background.render(matrices, vertexConsumer, light, overlay)
        leftSolid.render(matrices, vertexConsumer, light, overlay)
        topSolid.render(matrices, vertexConsumer, light, overlay)
        top.render(matrices, vertexConsumer, light, overlay)
        bottom.render(matrices, vertexConsumer, light, overlay)
        bottomSolid.render(matrices, vertexConsumer, light, overlay)
        center.render(matrices, vertexConsumer, light, overlay)
        rightSolid.render(matrices, vertexConsumer, light, overlay)
        right.render(matrices, vertexConsumer, light, overlay)
        left.render(matrices, vertexConsumer, light, overlay)
    }




}