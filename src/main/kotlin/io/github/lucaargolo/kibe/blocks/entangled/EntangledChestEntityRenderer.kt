package io.github.lucaargolo.kibe.blocks.entangled

import com.google.common.collect.ImmutableList
import io.github.lucaargolo.kibe.blocks.ENTANGLED_CHEST
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.Matrix4f
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.container.PlayerContainer
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import java.util.*
import java.util.function.Function
import java.util.stream.IntStream

class EntangledChestEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<EntangledChestEntity>(dispatcher) {

    private val LAYER_LIST: List<RenderLayer> = IntStream.range(0, 16).mapToObj { i: Int -> RenderLayer.getEndPortal(i + 1) }.collect(ImmutableList.toImmutableList())
    private val RANDOM = Random(31100L)

    enum class ANIMATION_STATE {
        GOING_UP,
        GOING_DOWN,
        UP,
        DOWN
    }

    var isScreenOpen = false
    var currentState = ANIMATION_STATE.DOWN
    var counter = 0f

    val bottomModel = ModelPart(64, 64, 0, 0)
    val topModel = ModelPart(64, 64, 0, 0)

    init {
        bottomModel.addCuboid(1F, 0F, 1F, 14F, 10F, 14F) // CHEST_BOTTOM

        topModel.setTextureOffset(32, 43)
        topModel.addCuboid(1F, 14F, 1F, 2F, 1F, 14F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(56, 0)
        topModel.addCuboid(3F, 14F, 1F, 2F, 1F, 2F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(56, 3)
        topModel.addCuboid(7F, 14F, 1F, 2F, 1F, 2F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(56, 6)
        topModel.addCuboid(11F, 14F, 1F, 2F, 1F, 2F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(56, 31)
        topModel.addCuboid(11F, 14F, 13F, 2F, 1F, 2F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(56, 28)
        topModel.addCuboid(7F, 14F, 13F, 2F, 1F, 2F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(56, 25)
        topModel.addCuboid(3F, 14F, 13F, 2F, 1F, 2F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(56, 17)
        topModel.addCuboid(3F, 14F, 9F, 2F, 1F, 2F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(56, 9)
        topModel.addCuboid(3F, 14F, 5F, 2F, 1F, 2F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(56, 14)
        topModel.addCuboid(11F, 14F, 5F, 2F, 1F, 2F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(58, 12)
        topModel.addCuboid(7F, 14F, 5F, 2F, 1F, 1F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(56, 22)
        topModel.addCuboid(11F, 14F, 9F, 2F, 1F, 2F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(58, 20)
        topModel.addCuboid(7F, 14F, 10F, 2F, 1F, 1F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(0, 43)
        topModel.addCuboid(13F, 14F, 1F, 2F, 1F, 14F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(16, 42)
        topModel.addCuboid(10F, 14F, 1F, 1F, 1F, 14F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(0, 0)
        topModel.addCuboid(9F, 14F, 1F, 1F, 1F, 6F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(0, 24)
        topModel.addCuboid(6F, 14F, 1F, 1F, 1F, 6F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(0, 31)
        topModel.addCuboid(6F, 14F, 9F, 1F, 1F, 6F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(0, 7)
        topModel.addCuboid(9F, 14F, 9F, 1F, 1F, 6F) // CHEST_TOP_PLATE
        topModel.setTextureOffset(16, 42)
        topModel.addCuboid(5F, 14F, 1F, 1F, 1F, 14F) // CHEST_TOP_PLATE
    }

    override fun render(blockEntity: EntangledChestEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        if(MinecraftClient.getInstance().currentScreen is EntangledChestScreen) {
            if(!isScreenOpen) {
                isScreenOpen = true
                when(currentState){
                    ANIMATION_STATE.DOWN -> {
                        currentState = ANIMATION_STATE.GOING_UP
                        counter = 0f
                    }
                    ANIMATION_STATE.GOING_DOWN -> {
                        currentState = ANIMATION_STATE.GOING_UP
                        counter = 30f-counter
                    }
                    else -> print("AAAAAAAAAAAAAAAAaaa")
                }
            }
        }else{
            if(isScreenOpen) {
                isScreenOpen = false
                when(currentState){
                    ANIMATION_STATE.UP -> {
                        currentState = ANIMATION_STATE.GOING_DOWN
                        counter = 0f
                    }
                    ANIMATION_STATE.GOING_UP -> {
                        currentState = ANIMATION_STATE.GOING_DOWN
                        counter = 30f-counter
                    }
                    else -> print("BBBBBBBBBBBbbb")
                }
            }
        }

        val world = blockEntity.world
        val blockState =
            if (world != null) blockEntity.cachedState else (
                ENTANGLED_CHEST.defaultState.with(
                Properties.HORIZONTAL_FACING,
                Direction.SOUTH
            ))

        matrices.push()
        val f = (blockState.get(Properties.HORIZONTAL_FACING) as Direction).asRotation()
        matrices.translate(0.5, 0.5, 0.5)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-f))
        matrices.translate(-0.5, -0.5, -0.5)

        val chestIdentifier = SpriteIdentifier(PlayerContainer.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/entangled_chest"))
        val chestConsumer = chestIdentifier.getVertexConsumer(vertexConsumers, Function { texture: Identifier? -> RenderLayer.getEntityCutout(texture) })

        val runesIdentifier = SpriteIdentifier(PlayerContainer.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/entangled_chest_runes"))
        val runesConsumer = runesIdentifier.getVertexConsumer(vertexConsumers, Function { texture: Identifier? -> RenderLayer.getEntityCutout(texture) })

        val lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.world, blockEntity.pos)
        bottomModel.render(matrices, chestConsumer, lightAbove, overlay)

        val d = blockEntity.pos.getSquaredDistance(dispatcher.camera.pos, true)
        var m = matrices.peek().model
        renderMiddleDownPart(0.15f, m, vertexConsumers.getBuffer(LAYER_LIST[0]))
        for (l in 1 until getLayersToRender(d)) {
            renderMiddleDownPart(2.0f / (18 - l).toFloat(), m, vertexConsumers.getBuffer(LAYER_LIST[l]))
        }

        matrices.translate(0.5, 0.0, 0.5)
        when(currentState) {
            ANIMATION_STATE.GOING_UP -> {
                counter += tickDelta
                matrices.multiply(Vector3f(0F, 1F, 0F).getDegreesQuaternion(counter*6))
                matrices.translate(0.0, counter/90.0, 0.0)
                if(counter >= 30f) currentState = ANIMATION_STATE.UP
            }
            ANIMATION_STATE.GOING_DOWN -> {
                counter += tickDelta
                matrices.multiply(Vector3f(0F, 1F, 0F).getDegreesQuaternion(360-counter*6))
                matrices.translate(0.0, 0.333-counter/90.0, 0.0)
                if(counter >= 30f) currentState = ANIMATION_STATE.DOWN
            }
            ANIMATION_STATE.UP -> {
                matrices.multiply(Vector3f(0F, 1F, 0F).getDegreesQuaternion(360f))
                matrices.translate(0.0, 30.0/90.0, 0.0)
                counter = 0f
            }
            ANIMATION_STATE.DOWN -> {
                counter = 0f
            }
        }
        matrices.translate(-0.5, 0.0, -0.5)

        topModel.render(matrices, chestConsumer, lightAbove, overlay)

        m = matrices.peek().model
        renderMiddlePart(0.15f, m, vertexConsumers.getBuffer(LAYER_LIST[0]))
        for (l in 1 until getLayersToRender(d)) {
            renderMiddlePart(2.0f / (18 - l).toFloat(), m, vertexConsumers.getBuffer(LAYER_LIST[l]))
        }

        matrices.pop()
    }

    private fun renderMiddleDownPart(g: Float, matrix4f: Matrix4f, vertexConsumer: VertexConsumer) {
        val red = (RANDOM.nextFloat() * 0.5f + 0.1f) * g
        val green = (RANDOM.nextFloat() * 0.5f + 0.4f) * g
        val blue = (RANDOM.nextFloat() * 0.5f + 0.5f) * g

        renderVertices(matrix4f, vertexConsumer, 0.125f, 0.875f, 0.626f, 0.626f, 0.875f, 0.875f, 0.125f, 0.125f, red, green, blue) //Direction.UP
    }

    private fun renderMiddlePart(g: Float, matrix4f: Matrix4f, vertexConsumer: VertexConsumer) {
        val red = (RANDOM.nextFloat() * 0.5f + 0.1f) * g
        val green = (RANDOM.nextFloat() * 0.5f + 0.4f) * g
        val blue = (RANDOM.nextFloat() * 0.5f + 0.5f) * g

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