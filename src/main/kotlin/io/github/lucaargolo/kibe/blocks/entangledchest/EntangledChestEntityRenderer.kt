package io.github.lucaargolo.kibe.blocks.entangledchest

import com.google.common.collect.ImmutableList
import io.github.lucaargolo.kibe.blocks.ENTANGLED_CHEST
import io.github.lucaargolo.kibe.items.entangledbag.EntangledBagScreen
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.state.property.Properties
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Matrix4f
import java.util.*
import java.util.function.Function
import java.util.stream.IntStream

class EntangledChestEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<EntangledChestEntity>(dispatcher) {

    @Suppress("UnstableApiUsage")
    private val layerList: List<RenderLayer> = IntStream.range(0, 16).mapToObj {
            i: Int -> RenderLayer.getEndPortal(i + 1)
    }.collect(ImmutableList.toImmutableList())

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

    private val bottomModel = ModelPart(64, 64, 0, 0)
    private val topModel = ModelPart(64, 64, 0, 0)

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

        var context = Context()

        if(contextMap.contains(blockEntity.pos)) {
            context = contextMap[blockEntity.pos]!!
        }else{
            contextMap[blockEntity.pos] = context
        }

        var isScreenOpen = context.isScreenOpen
        var currentState = context.currentState
        var counter = context.counter

        val screen = MinecraftClient.getInstance().currentScreen
        val isChestScreenOpen = if(screen is EntangledChestScreen) {
            screen.screenHandler.entity.runeColors == blockEntity.runeColors
        }else false
        val isBagScreenOpen = if(screen is EntangledBagScreen) {
            screen.hasSameColors(blockEntity.runeColors)
        }else false

        if((isChestScreenOpen || isBagScreenOpen) && blockEntity.world!!.getBlockState(blockEntity.pos.up()).isAir) {
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

        val chestIdentifier = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/entangled_chest"))
        val chestConsumer = chestIdentifier.getVertexConsumer(vertexConsumers, Function { texture: Identifier? -> RenderLayer.getEntityCutout(texture) })

        val runesIdentifier = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/entangled_chest_runes"))
        val runesConsumer = runesIdentifier.getVertexConsumer(vertexConsumers, Function { texture: Identifier? -> RenderLayer.getEntityCutout(texture) })

        val lightAbove = blockEntity.world?.let { WorldRenderer.getLightmapCoordinates(it, blockEntity.pos) } ?: light
        bottomModel.render(matrices, chestConsumer, lightAbove, overlay)

        val d = blockEntity.pos.getSquaredDistance(dispatcher.camera.pos, true)
        var m = matrices.peek().model
        renderMiddleDownPart(0.15f, m, vertexConsumers.getBuffer(layerList[0]))
        for (l in 1 until getLayersToRender(d)) {
            renderMiddleDownPart(2.0f / (18 - l).toFloat(), m, vertexConsumers.getBuffer(layerList[l]))
        }

        matrices.translate(0.5, 0.0, 0.5)
        when(currentState) {
            AnimationState.GOING_UP -> {
                counter += tickDelta
                matrices.multiply(Vector3f(0F, 1F, 0F).getDegreesQuaternion(counter*6))
                matrices.translate(0.0, counter/90.0, 0.0)
                if(counter >= 30f) currentState = AnimationState.UP
            }
            AnimationState.GOING_DOWN -> {
                counter += tickDelta
                matrices.multiply(Vector3f(0F, 1F, 0F).getDegreesQuaternion(360-counter*6))
                matrices.translate(0.0, 0.333-counter/90.0, 0.0)
                if(counter >= 30f) currentState = AnimationState.DOWN
            }
            AnimationState.UP -> {
                matrices.multiply(Vector3f(0F, 1F, 0F).getDegreesQuaternion(360f))
                matrices.translate(0.0, 30.0/90.0, 0.0)
                counter = 0f
            }
            AnimationState.DOWN -> {
                counter = 0f
            }
        }
        matrices.translate(-0.5, 0.0, -0.5)

        val popup = if(
            MinecraftClient.getInstance().crosshairTarget!!.type == HitResult.Type.BLOCK &&
            (MinecraftClient.getInstance().crosshairTarget!! as BlockHitResult).blockPos == blockEntity.pos &&
            MinecraftClient.getInstance().player!!.getStackInHand(Hand.MAIN_HAND).item is Rune
        ) 1 else 0


        (1..8).forEach {
            val rune = ModelPart(32, 32, 0, 0)
            rune.setTextureOffset(getRuneTextureU(blockEntity.runeColors[it]!!), getRuneTextureV(blockEntity.runeColors[it]!!))
            when(it) {
                1 -> rune.addCuboid(11f, 13f+popup, 11f, 2f, 2f, 2f)
                2 -> rune.addCuboid(7f, 13f+popup, 11f, 2f, 2f, 2f)
                3 -> rune.addCuboid(3f, 13f+popup, 11f, 2f, 2f, 2f)
                4 -> rune.addCuboid(3f, 13f+popup, 7f, 2f, 2f, 2f)
                5 -> rune.addCuboid(3f, 13f+popup, 3f, 2f, 2f, 2f)
                6 -> rune.addCuboid(7f, 13f+popup, 3f, 2f, 2f, 2f)
                7 -> rune.addCuboid(11f, 13f+popup, 3f, 2f, 2f, 2f)
                8 -> rune.addCuboid(11f, 13f+popup, 7f, 2f, 2f, 2f)
            }
            rune.render(matrices, runesConsumer, lightAbove, overlay)
        }

        val upuv = if(blockEntity.key != "entangledchest-global") -10 else 0;

        val core = ModelPart(64, 64, 0, 0)
        core.setTextureOffset(58, 50+upuv)
        core.addCuboid(9f, 14f, 7f, 1f, 2f, 2f)
        core.setTextureOffset(52, 44+upuv)
        core.addCuboid(7f, 14f, 6f, 2f, 2f, 4f)
        core.setTextureOffset(52, 50+upuv)
        core.addCuboid(6f, 14f, 7f, 1f, 2f, 2f)
        core.render(matrices, chestConsumer, lightAbove, overlay)

        topModel.render(matrices, chestConsumer, lightAbove, overlay)

        m = matrices.peek().model
        renderMiddlePart(0.15f, m, vertexConsumers.getBuffer(layerList[0]))
        for (l in 1 until getLayersToRender(d)) {
            renderMiddlePart(2.0f / (18 - l).toFloat(), m, vertexConsumers.getBuffer(layerList[l]))
        }

        contextMap[blockEntity.pos]!!.isScreenOpen = isScreenOpen
        contextMap[blockEntity.pos]!!.currentState = currentState
        contextMap[blockEntity.pos]!!.counter = counter

        matrices.pop()
    }

    private fun getRuneTextureV(color: DyeColor): Int {
        return when(color) {
            DyeColor.WHITE -> 0
            DyeColor.ORANGE -> 4
            DyeColor.MAGENTA -> 8
            DyeColor.LIGHT_BLUE -> 12
            DyeColor.YELLOW -> 16
            DyeColor.LIME -> 20
            DyeColor.PINK -> 24
            DyeColor.GRAY -> 28
            DyeColor.LIGHT_GRAY -> 0
            DyeColor.CYAN -> 4
            DyeColor.BLUE -> 8
            DyeColor.PURPLE -> 12
            DyeColor.GREEN -> 16
            DyeColor.BROWN -> 20
            DyeColor.RED -> 24
            DyeColor.BLACK -> 28
        }

    }

    private fun getRuneTextureU(color: DyeColor): Int {
        return when(color) {
            DyeColor.WHITE -> 0
            DyeColor.ORANGE -> 0
            DyeColor.MAGENTA -> 0
            DyeColor.LIGHT_BLUE -> 0
            DyeColor.YELLOW -> 0
            DyeColor.LIME -> 0
            DyeColor.PINK -> 0
            DyeColor.GRAY -> 0
            DyeColor.LIGHT_GRAY -> 8
            DyeColor.CYAN -> 8
            DyeColor.BLUE -> 8
            DyeColor.PURPLE -> 8
            DyeColor.GREEN -> 8
            DyeColor.BROWN -> 8
            DyeColor.RED -> 8
            DyeColor.BLACK -> 8
        }
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