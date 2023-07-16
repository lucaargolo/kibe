@file:Suppress("unused")

package io.github.lucaargolo.kibe

import io.github.lucaargolo.kibe.blocks.*
import io.github.lucaargolo.kibe.blocks.COOLER
import io.github.lucaargolo.kibe.blocks.ENTANGLED_CHEST
import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.drawbridge.DrawbridgeCustomModel
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChestEntityRenderer
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntityRenderer
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import io.github.lucaargolo.kibe.blocks.miscellaneous.RedstoneTimerEntityRenderer.Companion.selectorModelLayers
import io.github.lucaargolo.kibe.blocks.miscellaneous.RedstoneTimerEntityRenderer.Companion.setupSelectorModel
import io.github.lucaargolo.kibe.blocks.tank.TankCustomModel
import io.github.lucaargolo.kibe.entities.initEntitiesClient
import io.github.lucaargolo.kibe.fluids.initFluidsClient
import io.github.lucaargolo.kibe.items.*
import io.github.lucaargolo.kibe.items.cooler.CoolerTooltipComponent
import io.github.lucaargolo.kibe.items.cooler.CoolerTooltipData
import io.github.lucaargolo.kibe.items.entangledchest.EntangledChestBlockItemDynamicRenderer
import io.github.lucaargolo.kibe.items.entangledtank.EntangledTankBlockItemDynamicRenderer
import io.github.lucaargolo.kibe.items.miscellaneous.GliderDynamicRenderer
import io.github.lucaargolo.kibe.items.miscellaneous.MeasuringTape
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.FlameParticle
import net.minecraft.client.render.*
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.resource.ResourceManager
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import java.util.function.Consumer

var immediate = VertexConsumerProvider.immediate(BufferBuilder(128))

fun initClient() {
    initBlocksClient()
    initItemsClient()
    initEntitiesClient()
    initFluidsClient()
    initExtrasClient()
    initPacketsClient()
}

fun initPacketsClient() {

    ClientPlayNetworking.registerGlobalReceiver(SYNCHRONIZE_DIRTY_TANK_STATES) { client, _, buf, _ ->
        val tot = buf.readInt()
        repeat(tot) {
            val key = buf.readString()
            val qnt = buf.readInt()
            val map = mutableMapOf<String, Pair<FluidVariant, Long>>()
            repeat(qnt) {
                val colorCode = buf.readString()
                val fluidVolume = Pair(FluidVariant.fromPacket(buf), buf.readLong())
                map[colorCode] = fluidVolume
            }
            client.execute {
                val state = EntangledTankState.getOrCreateClientState(key)
                map.forEach { (colorCode, fluidVolume) ->
                    state.getOrCreateInventory(colorCode).let {
                        it.variant = fluidVolume.first
                        it.amount = fluidVolume.second
                    }
                }
            }
        }
    }

}

@Suppress("DEPRECATION", "UnstableApiUsage")
fun initExtrasClient() {

    EntangledChestEntityRenderer.helper.getEntries().forEach { (entityLayer, texturedModelData) ->
        EntityModelLayerRegistry.registerModelLayer(entityLayer) { texturedModelData }
    }
    EntangledTankEntityRenderer.helper.getEntries().forEach { (entityLayer, texturedModelData) ->
        EntityModelLayerRegistry.registerModelLayer(entityLayer) { texturedModelData }
    }
    selectorModelLayers.forEachIndexed{ index, entityModelLayer ->
        EntityModelLayerRegistry.registerModelLayer(entityModelLayer) { setupSelectorModel(index) }
    }
    WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
        drawMeasuringTapeOverlay(context)
        immediate.draw()
    }
    ParticleFactoryRegistry.getInstance().register(WATER_DROPS) { sprite -> FlameParticle.Factory(sprite) }
    ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
        EntangledTankState.CLIENT_STATES.clear()
        EntangledTankState.PAST_CLIENT_PLAYER_REQUESTS = linkedSetOf()
        EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS = linkedSetOf()
    }
    ClientTickEvents.END_CLIENT_TICK.register { client ->
        client.world?.let { _ ->
            if (EntangledTankState.PAST_CLIENT_PLAYER_REQUESTS != EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS) {
                val passedData = PacketByteBuf(Unpooled.buffer())
                passedData.writeInt(EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.size)
                EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.forEach {
                    passedData.writeString(it.first)
                    passedData.writeString(it.second)
                }
                ClientPlayNetworking.send(REQUEST_DIRTY_TANK_STATES, passedData)
            }
            EntangledTankState.PAST_CLIENT_PLAYER_REQUESTS = EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS
            EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS = linkedSetOf()
        }
    }
    ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(ClientSpriteRegistryCallback { _, registry ->
        registry.register(Identifier(MOD_ID, "block/entangled_chest"))
        registry.register(Identifier(MOD_ID, "block/entangled_chest_runes"))
        (0..15).forEach {
            registry.register(Identifier(MOD_ID, "block/redstone_timer_$it"))
        }
        registry.register(Identifier(MOD_ID, "block/tank"))
    })
    ModelLoadingRegistry.INSTANCE.registerModelProvider { _: ResourceManager, out: Consumer<Identifier> ->
        out.accept(ModelIdentifier(Identifier(MOD_ID, "redstone_timer_structure"), ""))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "glider_handle"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "white_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "white_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "orange_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "orange_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "magenta_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "magenta_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "light_blue_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "light_blue_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "yellow_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "yellow_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "lime_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "lime_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "pink_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "pink_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "gray_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "gray_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "light_gray_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "light_gray_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "cyan_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "cyan_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "blue_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "blue_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "purple_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "purple_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "green_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "green_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "brown_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "brown_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "red_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "red_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "black_glider_active"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "black_glider_inactive"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_ring"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_background"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_gold_core"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_diamond_core"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_fluid"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_background"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_foreground"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_gold_core"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_diamond_core"), "inventory"))
    }
    BlockRenderLayerMap.INSTANCE.putBlock(DRAWBRIDGE, RenderLayer.getCutoutMipped())
    BlockRenderLayerMap.INSTANCE.putBlock(VACUUM_HOPPER, RenderLayer.getTranslucent())
    BlockRenderLayerMap.INSTANCE.putBlock(BIG_TORCH, RenderLayer.getCutoutMipped())
    BlockRenderLayerMap.INSTANCE.putBlock(COOLER, RenderLayer.getTranslucent())
    BlockRenderLayerMap.INSTANCE.putBlock(WITHER_PROOF_GLASS, RenderLayer.getTranslucent())
    BlockRenderLayerMap.INSTANCE.putBlock(ENTANGLED_TANK, RenderLayer.getCutoutMipped())
    ModelLoadingRegistry.INSTANCE.registerVariantProvider {
        ModelVariantProvider { modelIdentifier, _ ->
            if(modelIdentifier.namespace == MOD_ID && modelIdentifier.path == "drawbridge") {
                return@ModelVariantProvider DrawbridgeCustomModel()
            }
            if(modelIdentifier.namespace == MOD_ID && modelIdentifier.path == "tank" && modelIdentifier.variant != "inventory") {
                return@ModelVariantProvider TankCustomModel()
            }
            return@ModelVariantProvider null
        }
    }
    BuiltinItemRendererRegistry.INSTANCE.register(ENTANGLED_CHEST, EntangledChestBlockItemDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(ENTANGLED_TANK, EntangledTankBlockItemDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(WHITE_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(ORANGE_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(MAGENTA_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(LIGHT_BLUE_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(YELLOW_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(LIME_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(PINK_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(GRAY_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(LIGHT_GRAY_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(CYAN_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(BLUE_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(PURPLE_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(GREEN_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(BROWN_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(RED_GLIDER, GliderDynamicRenderer())
    BuiltinItemRendererRegistry.INSTANCE.register(BLACK_GLIDER, GliderDynamicRenderer())
    TooltipComponentCallback.EVENT.register { data ->
        if(data is CoolerTooltipData) {
            return@register CoolerTooltipComponent(data)
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
                context.matrixStack().push()
                context.matrixStack().translate(fromPos.x-camera.pos.x, fromPos.y-camera.pos.y, fromPos.z-camera.pos.z)
                WorldRenderer.drawBox(context.matrixStack(), immediate.getBuffer(RenderLayer.getLines()), box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, ((color shr 16) and 0xFF)/255f, ((color shr 8) and 0xFF)/255f, (color and 0xFF)/255f, 1f)
                drawBox(immediate.getBuffer(RenderLayer.getEntityTranslucent(Identifier("minecraft:textures/block/white_concrete.png"))), context.matrixStack(), box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat(), box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat(), ((color shr 16) and 0xFF)/255f, ((color shr 8) and 0xFF)/255f, (color and 0xFF)/255f, 0.3f)
                context.matrixStack().pop()
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
    val sprite = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/overlay")).sprite

    //Render cube
    vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()

    vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()

    vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()

    vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()

    vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()

    vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.minU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z1)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.maxV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
    vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z2)?.color(red, green, blue, alpha)?.texture(sprite.maxU, sprite.minV)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
}

