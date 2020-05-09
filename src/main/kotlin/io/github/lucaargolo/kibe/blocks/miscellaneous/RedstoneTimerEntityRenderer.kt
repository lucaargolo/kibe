package io.github.lucaargolo.kibe.blocks.miscellaneous

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


class RedstoneTimerEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<RedstoneTimerEntity>(dispatcher) {

    var background: ModelPart = ModelPart(16, 16, 1, -13)
    var leftSolid: ModelPart = ModelPart(16, 16, 13, -1)
    var topSolid: ModelPart = ModelPart(16, 16, 3, 3)
    var top: ModelPart = ModelPart(16, 16, 0, -1)
    var bottom: ModelPart = ModelPart(16, 16, 0, -16)
    var bottomSolid: ModelPart = ModelPart(16, 16, 3, -9)
    var center: ModelPart = ModelPart(16, 16, 5, -1)
    var rightSolid: ModelPart = ModelPart(16, 16, -2, -1)
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

        val lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.world, blockEntity.pos.up())

        matrices.push()
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180f));
        matrices.translate(0.0, -1.0, -1.0)
        renderThings(blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f));
        matrices.translate(-1.0, 0.0, -1.0)
        renderThings(blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f));
        matrices.translate(-1.0, 0.0, 0.0)
        renderThings(blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f));
        matrices.translate(-1.0, 0.0, -1.0)
        renderThings(blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90f));
        matrices.translate(0.0, -1.0, 0.0)
        renderThings(blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180f));
        matrices.translate(-1.0, -1.0, 0.0)
        renderThings(blockEntity, matrices, vertexConsumers, lightAbove, overlay)

        matrices.pop()
    }

    private fun renderThings(blockEntity: RedstoneTimerEntity, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        val timerTexture = SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, Identifier("kibe:block/redstone_timer_"+blockEntity.current/4))
        val ironTexture = SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, Identifier("block/iron_block"))

        val timerConsumer = timerTexture.getVertexConsumer(vertexConsumers,
            Function { texture: Identifier? -> RenderLayer.getEntitySolid(texture) }
        )
        val ironConsumer = ironTexture.getVertexConsumer(vertexConsumers,
            Function { texture: Identifier? -> RenderLayer.getEntitySolid(texture) }
        )

        val offsetx = when(blockEntity.level) {
            in 0..4 -> blockEntity.level*2
            in 5..8 -> 8
            in 9..12 -> 24-(blockEntity.level*2)
            in 13..15 -> 0
            else -> 0
        }

        val offsety = when(blockEntity.level) {
            in 0..4 -> 0
            in 5..8 -> (blockEntity.level-4)*2
            in 9..12 -> 4*2
            in 13..15 -> 32-(blockEntity.level*2)
            else -> 0
        }

        val selector = ModelPart(16, 16, 3, 3)
        selector.addCuboid(3f+offsetx, 3f+offsety, 0f, 2f, 2f, 1f)
        selector.render(matrices, ironConsumer, light, overlay)

        background.render(matrices, timerConsumer, light, overlay)
        leftSolid.render(matrices, timerConsumer, light, overlay)
        topSolid.render(matrices, timerConsumer, light, overlay)
        top.render(matrices, timerConsumer, light, overlay)
        bottom.render(matrices, timerConsumer, light, overlay)
        bottomSolid.render(matrices, timerConsumer, light, overlay)
        center.render(matrices, timerConsumer, light, overlay)
        rightSolid.render(matrices, timerConsumer, light, overlay)
        right.render(matrices, timerConsumer, light, overlay)
        left.render(matrices, timerConsumer, light, overlay)
    }




}