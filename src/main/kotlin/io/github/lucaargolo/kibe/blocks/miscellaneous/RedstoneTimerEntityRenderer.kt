package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.MOD_ID
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import java.util.*

class RedstoneTimerEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<RedstoneTimerEntity>(dispatcher) {
    
    override fun render(blockEntity: RedstoneTimerEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

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



        val timerTexture = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier(MOD_ID, "block/redstone_timer_"+blockEntity.current/4))
        val timerConsumer = timerTexture.getVertexConsumer(vertexConsumers, { texture: Identifier? -> RenderLayer.getEntitySolid(texture) })

        val selector = ModelPart(16, 16, 3, 3)
        selector.addCuboid(0f, 3f+offsetX, 3f+offsetY, 1f, 2f, 2f)

        Direction.values().forEach {
            //Render selector
            matrices.push()
            matrices.translate(0.5, 0.5, 0.5)
            matrices.multiply(when(it) {
                Direction.NORTH -> Vector3f.POSITIVE_Y.getDegreesQuaternion(0f)
                Direction.SOUTH -> Vector3f.POSITIVE_Y.getDegreesQuaternion(180f)
                Direction.WEST -> Vector3f.POSITIVE_Y.getDegreesQuaternion(90f)
                Direction.EAST -> Vector3f.POSITIVE_Y.getDegreesQuaternion(270f)
                Direction.UP -> Vector3f.POSITIVE_Z.getDegreesQuaternion(90f)
                Direction.DOWN -> Vector3f.POSITIVE_Z.getDegreesQuaternion(270f)
            })
            matrices.translate(-0.5, -0.5, -0.5)
            renderSelector(selector, matrices, vertexConsumers, light, overlay)
            matrices.pop()

            //Render faces
            matrices.push()
            val vec = Direction.SOUTH.unitVector
            matrices.translate(0.5, 0.5, 0.5)

            val rot1 = when(it) {
                Direction.NORTH -> Vector3f.POSITIVE_Y.getDegreesQuaternion(0f)
                Direction.SOUTH -> Vector3f.POSITIVE_Y.getDegreesQuaternion(180f)
                Direction.WEST -> Vector3f.POSITIVE_Y.getDegreesQuaternion(90f)
                Direction.EAST -> Vector3f.POSITIVE_Y.getDegreesQuaternion(270f)
                Direction.UP -> Vector3f.POSITIVE_X.getDegreesQuaternion(90f)
                Direction.DOWN -> Vector3f.POSITIVE_X.getDegreesQuaternion(270f)
            }
            vec.rotate(rot1)
            matrices.multiply(rot1)

            val rot2 = when(it) {
                Direction.UP -> Vector3f.POSITIVE_Z.getDegreesQuaternion(90f)
                Direction.DOWN -> Vector3f.POSITIVE_Z.getDegreesQuaternion(270f)
                else -> null
            }

            rot2?.let {
                vec.rotate(it)
                matrices.multiply(it)
            }

            matrices.translate(-0.5, -0.5, -0.5)
            val entry = matrices.peek()
            val model = entry.model
            val normal = entry.normal
            val sprite = timerTexture.sprite
            val p = (sprite.maxU - sprite.minU)/16f

            timerConsumer.vertex(model, 0.0625f, 0.0625f, 0.9375f).color(1f, 1f, 1f, 1f).texture(sprite.minU+p, sprite.maxV-p).overlay(overlay).light(light).normal(normal, vec.x, vec.y, vec.z).next()
            timerConsumer.vertex(model, 0.9375f, 0.0625f, 0.9375f).color(1f, 1f, 1f, 1f).texture(sprite.maxU-p, sprite.maxV-p).overlay(overlay).light(light).normal(normal, vec.x, vec.y, vec.z).next()
            timerConsumer.vertex(model, 0.9375f, 0.9375f, 0.9375f).color(1f, 1f, 1f, 1f).texture(sprite.maxU-p, sprite.minV+p).overlay(overlay).light(light).normal(normal, vec.x, vec.y, vec.z).next()
            timerConsumer.vertex(model, 0.0625f, 0.9375f, 0.9375f).color(1f, 1f, 1f, 1f).texture(sprite.minU+p, sprite.minV+p).overlay(overlay).light(light).normal(normal, vec.x, vec.y, vec.z).next()

            matrices.pop()
        }

        val tankGlassIdentifier = ModelIdentifier(Identifier(MOD_ID, "redstone_timer_structure"), "")
        val tankGlassModel = MinecraftClient.getInstance().bakedModelManager.getModel(tankGlassIdentifier)

        val cutoutBuffer = vertexConsumers.getBuffer(RenderLayer.getCutout())
        tankGlassModel.getQuads(null, null, Random()).forEach { q ->
            cutoutBuffer.quad(matrices.peek(), q, 1f, 1f, 1f, light, overlay)
        }

    }


    private fun renderSelector(selector: ModelPart, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val ironTexture = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("block/iron_block"))
        val ironConsumer = ironTexture.getVertexConsumer(vertexConsumers, { texture: Identifier? -> RenderLayer.getEntitySolid(texture) })

        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180f))
        matrices.translate(0.0, -1.0, -1.0)
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90f))
        matrices.translate(0.0, -1.0, 0.0)
        selector.render(matrices, ironConsumer, light, overlay)
    }




}