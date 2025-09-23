@file:Suppress("unused")

package io.github.lucaargolo.kibe.client

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.blockentity.BlockEntityCompendium
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import io.github.lucaargolo.kibe.effect.EffectCompendium
import io.github.lucaargolo.kibe.entity.EntityCompendium
import io.github.lucaargolo.kibe.fluid.FluidCompendium
import io.github.lucaargolo.kibe.item.CoolerBlockItem
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.item.MeasuringTape
import io.github.lucaargolo.kibe.menu.ScreenHandlerCompendium
import io.github.lucaargolo.kibe.network.PacketCompendium
import io.github.lucaargolo.kibe.particle.ParticleCompendium
import io.github.lucaargolo.kibe.recipes.RecipeSerializerCompendium
import io.github.lucaargolo.kibe.recipes.RecipeTypeCompendium
import io.github.lucaargolo.kibe.utils.EntangledTankSync
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.util.BufferAllocator
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction

object KibeModClient {

    var immediate = VertexConsumerProvider.immediate(BufferAllocator(128))

    init {
        RecipeSerializerCompendium.initializeClient()
        RecipeTypeCompendium.initializeClient()
        FluidCompendium.initializeClient()
        BlockCompendium.initializeClient()
        ItemCompendium.initializeClient()
        ComponentTypeCompendium.initializeClient()
        BlockEntityCompendium.initializeClient()
        ScreenHandlerCompendium.initializeClient()
        EntityCompendium.initializeClient()
        EffectCompendium.initializeClient()
        ParticleCompendium.initializeClient()
        PacketCompendium.initializeClient()
        EntangledTankSync.initializeClient()
        initImmediateRendering()
        initExtraModels()
        initTooltipComponents()
    }

    fun initImmediateRendering() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
            drawMeasuringTapeOverlay(context)
            immediate.draw()
        }
    }

    fun initExtraModels() {
        ModelLoadingPlugin.register { out ->
            out.addModels(ModIdentifier.of("block/redstone_timer_structure")) // not inventory
            out.addModels(ModIdentifier.of("item/glider_active"))
            out.addModels(ModIdentifier.of("item/glider_handle"))
            out.addModels(ModIdentifier.of("item/white_glider_active"))
            out.addModels(ModIdentifier.of("item/white_glider_inactive"))
            out.addModels(ModIdentifier.of("item/orange_glider_active"))
            out.addModels(ModIdentifier.of("item/orange_glider_inactive"))
            out.addModels(ModIdentifier.of("item/magenta_glider_active"))
            out.addModels(ModIdentifier.of("item/magenta_glider_inactive"))
            out.addModels(ModIdentifier.of("item/light_blue_glider_active"))
            out.addModels(ModIdentifier.of("item/light_blue_glider_inactive"))
            out.addModels(ModIdentifier.of("item/yellow_glider_active"))
            out.addModels(ModIdentifier.of("item/yellow_glider_inactive"))
            out.addModels(ModIdentifier.of("item/lime_glider_active"))
            out.addModels(ModIdentifier.of("item/lime_glider_inactive"))
            out.addModels(ModIdentifier.of("item/pink_glider_active"))
            out.addModels(ModIdentifier.of("item/pink_glider_inactive"))
            out.addModels(ModIdentifier.of("item/gray_glider_active"))
            out.addModels(ModIdentifier.of("item/gray_glider_inactive"))
            out.addModels(ModIdentifier.of("item/light_gray_glider_active"))
            out.addModels(ModIdentifier.of("item/light_gray_glider_inactive"))
            out.addModels(ModIdentifier.of("item/cyan_glider_active"))
            out.addModels(ModIdentifier.of("item/cyan_glider_inactive"))
            out.addModels(ModIdentifier.of("item/blue_glider_active"))
            out.addModels(ModIdentifier.of("item/blue_glider_inactive"))
            out.addModels(ModIdentifier.of("item/purple_glider_active"))
            out.addModels(ModIdentifier.of("item/purple_glider_inactive"))
            out.addModels(ModIdentifier.of("item/green_glider_active"))
            out.addModels(ModIdentifier.of("item/green_glider_inactive"))
            out.addModels(ModIdentifier.of("item/brown_glider_active"))
            out.addModels(ModIdentifier.of("item/brown_glider_inactive"))
            out.addModels(ModIdentifier.of("item/red_glider_active"))
            out.addModels(ModIdentifier.of("item/red_glider_inactive"))
            out.addModels(ModIdentifier.of("item/black_glider_active"))
            out.addModels(ModIdentifier.of("item/black_glider_inactive"))
            out.addModels(ModIdentifier.of("item/entangled_ring"))
            out.addModels(ModIdentifier.of("item/entangled_bag_background"))
            out.addModels(ModIdentifier.of("item/entangled_bag_gold_core"))
            out.addModels(ModIdentifier.of("item/entangled_bag_diamond_core"))
            out.addModels(ModIdentifier.of("item/entangled_bucket_fluid"))
            out.addModels(ModIdentifier.of("item/entangled_bucket_background"))
            out.addModels(ModIdentifier.of("item/entangled_bucket_foreground"))
            out.addModels(ModIdentifier.of("item/entangled_bucket_gold_core"))
            out.addModels(ModIdentifier.of("item/entangled_bucket_diamond_core"))
        }
    }

    fun initTooltipComponents() {
        TooltipComponentCallback.EVENT.register { data ->
            if(data is CoolerBlockItem.CoolerTooltipData) {
                return@register CoolerBlockItem.CoolerTooltipComponent(data.contents)
            }else{
                return@register null
            }
        }
    }

    fun drawMeasuringTapeOverlay(context: WorldRenderContext) {
        val client = MinecraftClient.getInstance()
        val player = client.player
        val camera = client.gameRenderer.camera
        val target = client.crosshairTarget as? BlockHitResult
        val matrixStack = context.matrixStack() ?: return
        if (camera.isReady && player != null) {
            val world = player.world
            listOf(player.mainHandStack, player.offHandStack).forEach { stack ->
                val measuringFrom = MeasuringTape.measuringFrom(stack)
                val measuringTo = MeasuringTape.measuringTo(stack)
                if(measuringFrom != null && world.registryKey.value == measuringFrom.first) {
                    val fromPos = measuringFrom.second
                    val toPos = measuringTo?.second ?: target?.blockPos ?: return
                    val color = if(measuringTo?.second == null) 0x0000FF else 0xFFFF00
                    val box = getDrawBox(fromPos, toPos)
                    matrixStack.push()
                    matrixStack.translate(fromPos.x-camera.pos.x, fromPos.y-camera.pos.y, fromPos.z-camera.pos.z)
                    WorldRenderer.drawBox(matrixStack, immediate.getBuffer(RenderLayer.getLines()), box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, ((color shr 16) and 0xFF)/255f, ((color shr 8) and 0xFF)/255f, (color and 0xFF)/255f, 1f)
                    drawBox(immediate.getBuffer(RenderLayer.getEntityTranslucent(Identifier.of("minecraft:textures/block/white_concrete.png"))), matrixStack, box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), ((color shr 16) and 0xFF)/255f, ((color shr 8) and 0xFF)/255f, (color and 0xFF)/255f, 0.3f)
                    matrixStack.pop()
                }
            }
        }
    }

    fun getDrawBox(fromPos: BlockPos, toPos: BlockPos): Box {
        var startX = 0.0
        var startY = 0.0
        var startZ = 0.0
        var sizeX = 1.0 - (fromPos.x - toPos.x)
        var sizeY = 1.0 - (fromPos.y - toPos.y)
        var sizeZ = 1.0 - (fromPos.z - toPos.z)
        if (sizeX < 1.0) {
            startX = 1.0
            sizeX -= 1.0
        }
        if (sizeY < 1.0) {
            startY = 1.0
            sizeY -= 1.0
        }
        if (sizeZ < 1.0) {
            startZ = 1.0
            sizeZ -= 1.0
        }

        return Box(startX, startY, startZ, sizeX, sizeY, sizeZ).expand(0.01)
    }

    fun drawBox(vertexConsumer: VertexConsumer?, matrixStack: MatrixStack, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, red: Float, green: Float, blue: Float, alpha: Float) {
        val entry = matrixStack.peek()
        val normal = Direction.NORTH.unitVector
        val sprite = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier.of("block/overlay")).sprite

        //Render cube
        vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)

        vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)

        vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)

        vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)

        vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)

        vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
        vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry, normal.x, normal.y, normal.z)
    }

}
