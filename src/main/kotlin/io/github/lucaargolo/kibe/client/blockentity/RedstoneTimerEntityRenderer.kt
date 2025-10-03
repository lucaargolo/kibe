package io.github.lucaargolo.kibe.client.blockentity

import io.github.lucaargolo.kibe.blockentity.RedstoneTimerEntity
import io.github.lucaargolo.kibe.client.KibeModClient
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.minecraft.client.model.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.random.Random

class RedstoneTimerEntityRenderer(private val arg: BlockEntityRendererFactory.Context): BlockEntityRenderer<RedstoneTimerEntity> {

    companion object {
        val selectorModelLayers = mutableListOf<EntityModelLayer>()

        init {
            (0..15).forEach { level ->
                selectorModelLayers.add(EntityModelLayer(ModIdentifier.of("redstone_timer"), "selector${level}"))
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

        val timerTexture = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier.of("block/redstone_timer_"+blockEntity.current/4))
        val timerConsumer = timerTexture.getVertexConsumer(vertexConsumers, { texture: Identifier? -> RenderLayer.getEntitySolid(texture) })

        Direction.entries.forEach { direction ->
            //Render selector
            matrices.push()
            matrices.translate(0.5, 0.5, 0.5)
            matrices.multiply(when(direction) {
                Direction.NORTH -> RotationAxis.POSITIVE_Y.rotationDegrees(0f)
                Direction.SOUTH -> RotationAxis.POSITIVE_Y.rotationDegrees(180f)
                Direction.WEST -> RotationAxis.POSITIVE_Y.rotationDegrees(90f)
                Direction.EAST -> RotationAxis.POSITIVE_Y.rotationDegrees(270f)
                Direction.UP -> RotationAxis.POSITIVE_Z.rotationDegrees(90f)
                Direction.DOWN -> RotationAxis.POSITIVE_Z.rotationDegrees(270f)
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
                Direction.NORTH -> RotationAxis.POSITIVE_Y.rotationDegrees(0f)
                Direction.SOUTH -> RotationAxis.POSITIVE_Y.rotationDegrees(180f)
                Direction.WEST -> RotationAxis.POSITIVE_Y.rotationDegrees(90f)
                Direction.EAST -> RotationAxis.POSITIVE_Y.rotationDegrees(270f)
                Direction.UP -> RotationAxis.POSITIVE_X.rotationDegrees(90f)
                Direction.DOWN -> RotationAxis.POSITIVE_X.rotationDegrees(270f)
            }
            vec.rotate(rot1)
            matrices.multiply(rot1)

            val rot2 = when(direction) {
                Direction.UP -> RotationAxis.POSITIVE_Z.rotationDegrees(90f)
                Direction.DOWN -> RotationAxis.POSITIVE_Z.rotationDegrees(270f)
                else -> null
            }

            rot2?.let {
                vec.rotate(it)
                matrices.multiply(it)
            }

            matrices.translate(-0.5, -0.5, -0.5)
            val entry = matrices.peek()
            val sprite = timerTexture.sprite
            val p = (sprite.maxU - sprite.minU)/16f

            timerConsumer.vertex(entry, 0.0625f, 0.0625f, 0.9375f).color(1f, 1f, 1f, 1f).texture(sprite.minU+p, sprite.maxV-p).overlay(overlay).light(light).normal(entry, vec.x, vec.y, vec.z)
            timerConsumer.vertex(entry, 0.9375f, 0.0625f, 0.9375f).color(1f, 1f, 1f, 1f).texture(sprite.maxU-p, sprite.maxV-p).overlay(overlay).light(light).normal(entry, vec.x, vec.y, vec.z)
            timerConsumer.vertex(entry, 0.9375f, 0.9375f, 0.9375f).color(1f, 1f, 1f, 1f).texture(sprite.maxU-p, sprite.minV+p).overlay(overlay).light(light).normal(entry, vec.x, vec.y, vec.z)
            timerConsumer.vertex(entry, 0.0625f, 0.9375f, 0.9375f).color(1f, 1f, 1f, 1f).texture(sprite.minU+p, sprite.minV+p).overlay(overlay).light(light).normal(entry, vec.x, vec.y, vec.z)

            matrices.pop()
        }

        val tankGlassIdentifier = ModIdentifier.of("block/redstone_timer_structure")
        val tankGlassModel = KibeModClient.bakedModel(tankGlassIdentifier)

        val cutoutBuffer = vertexConsumers.getBuffer(RenderLayer.getCutout())
        tankGlassModel?.getQuads(null, null, Random.create())?.forEach { q ->
            cutoutBuffer.quad(matrices.peek(), q, 1f, 1f, 1f, 1f, light, overlay)
        }

    }


    private fun renderSelector(selector: ModelPart, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val ironTexture = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of("block/iron_block"))
        val ironConsumer = ironTexture.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid)

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
        matrices.translate(0.0, -1.0, -1.0)
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90f))
        matrices.translate(0.0, -1.0, 0.0)
        selector.render(matrices, ironConsumer, light, overlay)
    }




}