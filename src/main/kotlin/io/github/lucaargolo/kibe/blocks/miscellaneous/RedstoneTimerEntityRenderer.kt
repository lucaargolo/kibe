package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.MOD_ID
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f
import java.util.*

class RedstoneTimerEntityRenderer(private val arg: BlockEntityRendererFactory.Context): BlockEntityRenderer<RedstoneTimerEntity> {

    companion object {
        val selectorModelLayers = mutableListOf<EntityModelLayer>()

        init {
            (0..15).forEach { level ->
                selectorModelLayers.add(EntityModelLayer(Identifier(MOD_ID, "redstone_timer"), "selector${level}"))
            }
        }

        fun setupSelectorModel(level: Int): TexturedModelData {
            val offsetX = when(level) {
                in 0..4 -> level*2
                in 5..8 -> 8
                in 9..12 -> 24-(level*2)
                in 13..15 -> 0
                else -> 0
            }

            val offsetY = when(level) {
                in 0..4 -> 0
                in 5..8 -> (level-4)*2
                in 9..12 -> 4*2
                in 13..15 -> 32-(level*2)
                else -> 0
            }

            val lv = ModelData()
            val lv2 = lv.getRoot()
            lv2.addChild("selector", ModelPartBuilder.create().uv(3, 3).cuboid(0f, 3f+offsetX, 3f+offsetY, 1f, 2f, 2f), ModelTransform.NONE)
            return TexturedModelData.of(lv, 16, 16)
        }

    }


    override fun render(blockEntity: RedstoneTimerEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        val timerTexture = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier(MOD_ID, "block/redstone_timer_"+blockEntity.current/4))
        val timerConsumer = timerTexture.getVertexConsumer(vertexConsumers, { texture: Identifier? -> RenderLayer.getEntitySolid(texture) })

        Direction.values().forEach { direction ->
            //Render selector
            matrices.push()
            matrices.translate(0.5, 0.5, 0.5)
            matrices.multiply(when(direction) {
                Direction.NORTH -> Vec3f.POSITIVE_Y.getDegreesQuaternion(0f)
                Direction.SOUTH -> Vec3f.POSITIVE_Y.getDegreesQuaternion(180f)
                Direction.WEST -> Vec3f.POSITIVE_Y.getDegreesQuaternion(90f)
                Direction.EAST -> Vec3f.POSITIVE_Y.getDegreesQuaternion(270f)
                Direction.UP -> Vec3f.POSITIVE_Z.getDegreesQuaternion(90f)
                Direction.DOWN -> Vec3f.POSITIVE_Z.getDegreesQuaternion(270f)
            })

            matrices.translate(-0.5, -0.5, -0.5)
            val selectorModel = arg.getLayerModelPart(selectorModelLayers[blockEntity.level])
            renderSelector(selectorModel, matrices, vertexConsumers, light, overlay)
            matrices.pop()

            //Render faces
            matrices.push()
            val vec = Direction.SOUTH.unitVector
            matrices.translate(0.5, 0.5, 0.5)

            val rot1 = when(direction) {
                Direction.NORTH -> Vec3f.POSITIVE_Y.getDegreesQuaternion(0f)
                Direction.SOUTH -> Vec3f.POSITIVE_Y.getDegreesQuaternion(180f)
                Direction.WEST -> Vec3f.POSITIVE_Y.getDegreesQuaternion(90f)
                Direction.EAST -> Vec3f.POSITIVE_Y.getDegreesQuaternion(270f)
                Direction.UP -> Vec3f.POSITIVE_X.getDegreesQuaternion(90f)
                Direction.DOWN -> Vec3f.POSITIVE_X.getDegreesQuaternion(270f)
            }
            vec.rotate(rot1)
            matrices.multiply(rot1)

            val rot2 = when(direction) {
                Direction.UP -> Vec3f.POSITIVE_Z.getDegreesQuaternion(90f)
                Direction.DOWN -> Vec3f.POSITIVE_Z.getDegreesQuaternion(270f)
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

        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180f))
        matrices.translate(0.0, -1.0, -1.0)
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-90f))
        matrices.translate(0.0, -1.0, 0.0)
        selector.render(matrices, ironConsumer, light, overlay)
    }




}