package io.github.lucaargolo.kibe.client.blockentity

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.block.EntangledChest
import io.github.lucaargolo.kibe.blockentity.EntangledChestEntity
import io.github.lucaargolo.kibe.client.EntangledRenderer
import io.github.lucaargolo.kibe.utils.EntangledChestAnimationState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.DyeItem
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.state.property.Properties
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import org.joml.Matrix4f
import java.util.*

class EntangledChestEntityRenderer(private val arg: BlockEntityRendererFactory.Context): BlockEntityRenderer<EntangledChestEntity> {

    companion object {
        val helper = EntangledRenderer("entangled_chest")
        private val previousAnimations = mutableMapOf<Pair<String, String>, Float>()
    }

    private val bottomModel = arg.getLayerModelPart(helper.bottomModelLayer)
    private val topModel = arg.getLayerModelPart(helper.topModelLayer)
    private val coreModelGold = arg.getLayerModelPart(helper.coreModelLayerGold)
    private val coreModelDiamond = arg.getLayerModelPart(helper.coreModelLayerDiamond)


    private val random = Random(31100L)

    override fun render(entity: EntangledChestEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val world = entity.world
        val blockState =
            if (world != null) entity.cachedState else (
                BlockCompendium.ENTANGLED_CHEST.defaultState.with(
                Properties.HORIZONTAL_FACING,
                Direction.SOUTH
            ))

        matrices.push()
        val f = (blockState.get(Properties.HORIZONTAL_FACING) as Direction).asRotation()
        matrices.translate(0.5, 0.5, 0.5)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-f))
        matrices.translate(-0.5, -0.5, -0.5)

        val chestIdentifier = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of("kibe:block/entangled_chest"))
        val chestConsumer = chestIdentifier.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutout)

        val runesIdentifier = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of("kibe:block/entangled_chest_runes"))
        val runesConsumer = runesIdentifier.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutout)

        val lightAbove = entity.world?.let { WorldRenderer.getLightmapCoordinates(it, entity.pos) } ?: light
        bottomModel.render(matrices, chestConsumer, lightAbove, overlay)

        var m = matrices.peek().positionMatrix
        renderMiddleDownPart(0.15f, m, vertexConsumers.getBuffer(RenderLayer.getEndPortal()))

        matrices.translate(0.5, 0.0, 0.5)

        val p = Pair(entity.key, entity.colorCode)
        val animation = EntangledChestAnimationState.state(p)
        val previousAnimation = previousAnimations.getOrPut(p) { animation }
        val a = MathHelper.lerp(tickDelta, previousAnimation, animation)
        previousAnimations.put(p, a)

        if(EntangledChest.canOpen(entity.world, entity.pos)) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(a * 9))
            matrices.translate(0f, a / 30f, 0f)
        }

        matrices.translate(-0.5f, 0f, -0.5f)

        val popup = if(
            MinecraftClient.getInstance().crosshairTarget!!.type == HitResult.Type.BLOCK &&
            (MinecraftClient.getInstance().crosshairTarget!! as BlockHitResult).blockPos == entity.pos &&
            MinecraftClient.getInstance().player!!.getStackInHand(Hand.MAIN_HAND).item is DyeItem
        ) 0.0625 else 0.0

        entity.runeColors.forEachIndexed { idx, col ->
            val runeModelLayer = EntangledTankEntityRenderer.helper.getRuneLayer(idx, col)
            matrices.translate(0.0, popup, 0.0)
            runeModelLayer?.let {
                val rune = arg.getLayerModelPart(runeModelLayer)
                rune.render(matrices, runesConsumer, lightAbove, overlay)
            }
            matrices.translate(0.0, -popup, 0.0)
        }

        val coreModel = if(entity.key != EntangledChest.DEFAULT_KEY) coreModelDiamond else coreModelGold

        coreModel.render(matrices, chestConsumer, lightAbove, overlay)

        topModel.render(matrices, chestConsumer, lightAbove, overlay)

        m = matrices.peek().positionMatrix
        renderMiddlePart(0.15f, m, vertexConsumers.getBuffer(RenderLayer.getEndPortal()))

        matrices.pop()
    }

    private fun renderMiddleDownPart(g: Float, matrix4f: Matrix4f, vertexConsumer: VertexConsumer) {
        val red = (random.nextFloat() * 0.5f + 0.1f) * g
        val green = (random.nextFloat() * 0.5f + 0.4f) * g
        val blue = (random.nextFloat() * 0.5f + 0.5f) * g

        renderVertices(matrix4f, vertexConsumer, 0.125f, 0.875f, 0.626f, 0.626f, 0.875f, 0.875f, 0.125f, 0.125f, red, green, blue) //Direction.UP
    }

    private fun renderMiddlePart(g: Float, matrix4f: Matrix4f, vertexConsumer: VertexConsumer) {
        val red = (random.nextFloat() * 0.5f + 0.1f) * g
        val green = (random.nextFloat() * 0.5f + 0.4f) * g
        val blue = (random.nextFloat() * 0.5f + 0.5f) * g

        renderVertices(matrix4f, vertexConsumer, 0.125f, 0.875f, 0.625f, 0.875f, 0.875f, 0.875f, 0.875f, 0.875f, red, green, blue) //Direction.SOUTH
        renderVertices(matrix4f, vertexConsumer, 0.125f, 0.875f, 0.875f, 0.625f, 0.125f, 0.125f, 0.125f, 0.125f, red, green, blue) //Direction.NORTH
        renderVertices(matrix4f, vertexConsumer, 0.875f, 0.875f, 0.875f, 0.625f, 0.125f, 0.875f, 0.875f, 0.125f, red, green, blue) //Direction.EAST
        renderVertices(matrix4f, vertexConsumer, 0.125f, 0.125f, 0.625f, 0.875f, 0.125f, 0.875f, 0.875f, 0.125f, red, green, blue) //Direction.WEST)
        renderVertices(matrix4f, vertexConsumer, 0.125f, 0.875f, 0.625f, 0.625f, 0.125f, 0.125f, 0.875f, 0.875f, red, green, blue) //Direction.DOWN
        renderVertices(matrix4f, vertexConsumer, 0.125f, 0.875f, 0.875f, 0.875f, 0.875f, 0.875f, 0.125f, 0.125f, red, green, blue) //Direction.UP
    }

    private fun renderVertices(matrix4f: Matrix4f, vertexConsumer: VertexConsumer, f: Float, g: Float, h: Float, i: Float, j: Float, k: Float, l: Float, m: Float, red: Float, green: Float, blue: Float) {
        vertexConsumer.vertex(matrix4f, f, h, j).color(red, green, blue, 1.0f)
        vertexConsumer.vertex(matrix4f, g, h, k).color(red, green, blue, 1.0f)
        vertexConsumer.vertex(matrix4f, g, i, l).color(red, green, blue, 1.0f)
        vertexConsumer.vertex(matrix4f, f, i, m).color(red, green, blue, 1.0f)
    }

}