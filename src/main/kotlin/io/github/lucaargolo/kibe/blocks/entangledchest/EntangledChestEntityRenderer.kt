package io.github.lucaargolo.kibe.blocks.entangledchest

import io.github.lucaargolo.kibe.blocks.ENTANGLED_CHEST
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntityRenderer
import io.github.lucaargolo.kibe.items.entangledbag.EntangledBagScreen
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import io.github.lucaargolo.kibe.utils.EntangledRendererHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.state.property.Properties
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import org.joml.Matrix4f
import java.util.*

class EntangledChestEntityRenderer(private val arg: BlockEntityRendererFactory.Context): BlockEntityRenderer<EntangledChestEntity> {

    companion object {
        val helper = EntangledRendererHelper("entangled_chest")
    }

    private val bottomModel = arg.getLayerModelPart(helper.bottomModelLayer)
    private val topModel = arg.getLayerModelPart(helper.topModelLayer)
    private val coreModelGold = arg.getLayerModelPart(helper.coreModelLayerGold)
    private val coreModelDiamond = arg.getLayerModelPart(helper.coreModelLayerDiamond)

    private val random = Random(31100L)

    enum class AnimationState {
        GOING_UP,
        GOING_DOWN,
        UP,
        DOWN
    }

    private val contextMap = mutableMapOf<BlockPos, Context>()

    private class Context {
        var isScreenOpen = false
        var currentState = AnimationState.DOWN
        var counter = 0f
    }

    override fun render(entity: EntangledChestEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        var context = Context()

        if(contextMap.contains(entity.pos)) {
            context = contextMap[entity.pos]!!
        }else{
            contextMap[entity.pos] = context
        }

        var isScreenOpen = context.isScreenOpen
        var currentState = context.currentState
        var counter = context.counter

        val screen = MinecraftClient.getInstance().currentScreen
        val isChestScreenOpen = if(screen is EntangledChestScreen) {
            screen.screenHandler.entity.runeColors == entity.runeColors
        }else false
        val isBagScreenOpen = if(screen is EntangledBagScreen) {
            screen.hasSameColors(entity.runeColors)
        }else false

        if((isChestScreenOpen || isBagScreenOpen) && EntangledChest.canOpen(entity.world, entity.pos)) {
            if(!isScreenOpen) {
                isScreenOpen = true
                when(currentState){
                    AnimationState.DOWN -> {
                        currentState = AnimationState.GOING_UP
                        counter = 0f
                    }
                    AnimationState.GOING_DOWN -> {
                        currentState = AnimationState.GOING_UP
                        counter = 30f-counter
                    }
                    else -> print("AAAAAAAAAAAAAAAAaaa")
                }
            }
        }else{
            if(isScreenOpen) {
                isScreenOpen = false
                when(currentState){
                    AnimationState.UP -> {
                        currentState = AnimationState.GOING_DOWN
                        counter = 0f
                    }
                    AnimationState.GOING_UP -> {
                        currentState = AnimationState.GOING_DOWN
                        counter = 30f-counter
                    }
                    else -> print("BBBBBBBBBBBbbb")
                }
            }
        }

        val world = entity.world
        val blockState =
            if (world != null) entity.cachedState else (
                ENTANGLED_CHEST.defaultState.with(
                Properties.HORIZONTAL_FACING,
                Direction.SOUTH
            ))

        matrices.push()
        val f = (blockState.get(Properties.HORIZONTAL_FACING) as Direction).asRotation()
        matrices.translate(0.5, 0.5, 0.5)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-f))
        matrices.translate(-0.5, -0.5, -0.5)

        val chestIdentifier = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/entangled_chest"))
        val chestConsumer = chestIdentifier.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutout)

        val runesIdentifier = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/entangled_chest_runes"))
        val runesConsumer = runesIdentifier.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutout)

        val lightAbove = entity.world?.let { WorldRenderer.getLightmapCoordinates(it, entity.pos) } ?: light
        bottomModel.render(matrices, chestConsumer, lightAbove, overlay)

        var m = matrices.peek().positionMatrix
        renderMiddleDownPart(0.15f, m, vertexConsumers.getBuffer(RenderLayer.getEndPortal()))

        matrices.translate(0.5, 0.0, 0.5)
        when(currentState) {
            AnimationState.GOING_UP -> {
                counter += tickDelta
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(counter*6))
                matrices.translate(0.0, counter/90.0, 0.0)
                if(counter >= 30f) currentState = AnimationState.UP
            }
            AnimationState.GOING_DOWN -> {
                counter += tickDelta
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360-counter*6))
                matrices.translate(0.0, 0.333-counter/90.0, 0.0)
                if(counter >= 30f) currentState = AnimationState.DOWN
            }
            AnimationState.UP -> {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360f))
                matrices.translate(0.0, 30.0/90.0, 0.0)
                counter = 0f
            }
            AnimationState.DOWN -> {
                counter = 0f
            }
        }
        matrices.translate(-0.5, 0.0, -0.5)

        val popup = if(
            MinecraftClient.getInstance().crosshairTarget != null &&
            MinecraftClient.getInstance().crosshairTarget!!.type == HitResult.Type.BLOCK &&
            (MinecraftClient.getInstance().crosshairTarget!! as BlockHitResult).blockPos == entity.pos &&
            MinecraftClient.getInstance().player!!.getStackInHand(Hand.MAIN_HAND).item is Rune
        ) 0.0625 else 0.0

        (1..8).forEach { runeId ->
            val runeModelLayer = entity.runeColors[runeId]?.let { EntangledTankEntityRenderer.helper.getRuneLayer(runeId, it) }
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

        contextMap[entity.pos]!!.isScreenOpen = isScreenOpen
        contextMap[entity.pos]!!.currentState = currentState
        contextMap[entity.pos]!!.counter = counter

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
        vertexConsumer.vertex(matrix4f, f, h, j).color(red, green, blue, 1.0f).next()
        vertexConsumer.vertex(matrix4f, g, h, k).color(red, green, blue, 1.0f).next()
        vertexConsumer.vertex(matrix4f, g, i, l).color(red, green, blue, 1.0f).next()
        vertexConsumer.vertex(matrix4f, f, i, m).color(red, green, blue, 1.0f).next()
    }


    private fun getLayersToRender(d: Double): Int {
        return when {
            d > 36864.0 -> 1
            d > 25600.0 -> 3
            d > 16384.0 -> 5
            d > 9216.0 -> 7
            d > 4096.0 -> 9
            d > 1024.0 -> 11
            d > 576.0 -> 13
            else -> if (d > 256.0) 14 else 15
        }
    }

}