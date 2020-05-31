package io.github.lucaargolo.kibe.blocks.miscellaneous

import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import java.util.function.Function

class RedstoneTimerEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<RedstoneTimerEntity>(dispatcher) {

    private var background = ModelPart(16, 16, 1, -13)

    init {
        background.addCuboid(1f, 1f, 1f, 0f, 14f, 14f)
    }
    
    override fun render(blockEntity: RedstoneTimerEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        val lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.world, blockEntity.pos.up())

        val offsetX = when(blockEntity.level) {
            in 0..4 -> blockEntity.level*2
            in 5..8 -> 8
            in 9..12 -> 24-(blockEntity.level*2)
            in 13..15 -> 0
            else -> 0
        }

        val offsetY = when(blockEntity.level) {
            in 0..4 -> 0
            in 5..8 -> (blockEntity.level-4)*2
            in 9..12 -> 4*2
            in 13..15 -> 32-(blockEntity.level*2)
            else -> 0
        }

        val selector = ModelPart(16, 16, 3, 3)
        selector.addCuboid(0f, 3f+offsetX, 3f+offsetY, 1f, 2f, 2f)

        matrices.push()
        renderThings(selector, blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.pop()

        matrices.push()
        matrices.translate(1.0, 0.0, 1.0)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f))
        renderThings(selector, blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.pop()

        matrices.push()
        matrices.translate(0.0, 0.0, 1.0)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f))
        renderThings(selector, blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.pop()

        matrices.push()
        matrices.translate(1.0, 0.0, 0.0)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270f))
        renderThings(selector, blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.pop()

        matrices.push()
        matrices.translate(1.0, 0.0, 0.0)
        matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90f))
        renderThings(selector, blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.pop()

        matrices.push()
        matrices.translate(0.0, 1.0, 0.0)
        matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(270f))
        renderThings(selector, blockEntity, matrices, vertexConsumers, lightAbove, overlay)
        matrices.pop()

    }

    private fun renderThings(selector: ModelPart, blockEntity: RedstoneTimerEntity, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val timerTexture = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/redstone_timer_"+blockEntity.current/4))
        val ironTexture = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("block/iron_block"))

        val timerConsumer = timerTexture.getVertexConsumer(vertexConsumers,
            Function { texture: Identifier? -> RenderLayer.getEntitySolid(texture) }
        )
        val ironConsumer = ironTexture.getVertexConsumer(vertexConsumers,
            Function { texture: Identifier? -> RenderLayer.getEntitySolid(texture) }
        )

        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180f))
        matrices.translate(0.0, -1.0, -1.0)
        background.render(matrices, timerConsumer, light, overlay)
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90f))
        matrices.translate(0.0, -1.0, 0.0)
        selector.render(matrices, ironConsumer, light, overlay)
    }




}