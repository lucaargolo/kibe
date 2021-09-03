package io.github.lucaargolo.kibe.blocks.entangledtank

import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.tank.TankBlockEntityRenderer
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import io.github.lucaargolo.kibe.utils.EntangledRendererHelper
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3f
import net.minecraft.fluid.Fluids
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.state.property.Properties
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import java.awt.Color

class EntangledTankEntityRenderer(private val arg: BlockEntityRendererFactory.Context): BlockEntityRenderer<EntangledTankEntity> {

    companion object {
        val helper = EntangledRendererHelper("entangled_tank")
    }

    private val bottomModel = arg.getLayerModelPart(helper.bottomModelLayer)
    private val topModel = arg.getLayerModelPart(helper.topModelLayer)
    private val coreModelGold = arg.getLayerModelPart(helper.coreModelLayerGold)
    private val coreModelDiamond = arg.getLayerModelPart(helper.coreModelLayerDiamond)

    override fun render(entity: EntangledTankEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        val world = entity.world
        val blockState = if (world != null) entity.cachedState else (ENTANGLED_TANK.defaultState.with(Properties.HORIZONTAL_FACING, Direction.SOUTH))

        matrices.push()

        val f = (blockState.get(Properties.HORIZONTAL_FACING) as Direction).asRotation()
        matrices.translate(0.5, 0.5, 0.5)
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-f))
        matrices.translate(-0.5, -0.5, -0.5)

        val chestIdentifier = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/entangled_chest"))
        val chestConsumer = chestIdentifier.getVertexConsumer(vertexConsumers, { texture: Identifier? -> RenderLayer.getEntityCutout(texture) })

        val runesIdentifier = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/entangled_chest_runes"))
        val runesConsumer = runesIdentifier.getVertexConsumer(vertexConsumers, { texture: Identifier? -> RenderLayer.getEntityCutout(texture) })

        val lightAbove = entity.world?.let { WorldRenderer.getLightmapCoordinates(it, entity.pos) } ?: light
        bottomModel.render(matrices, chestConsumer, lightAbove, overlay)

        val popup = if(
            MinecraftClient.getInstance().crosshairTarget!!.type == HitResult.Type.BLOCK &&
            (MinecraftClient.getInstance().crosshairTarget!! as BlockHitResult).blockPos == entity.pos &&
            MinecraftClient.getInstance().player!!.getStackInHand(Hand.MAIN_HAND).item is Rune
        ) 0.0625 else 0.0

        (1..8).forEach { runeId ->
            val runeModelLayer = entity.runeColors[runeId]?.let { helper.getRuneLayer(runeId, it) }
            matrices.translate(0.0, popup, 0.0)
            runeModelLayer?.let {
                val rune = arg.getLayerModelPart(runeModelLayer)
                rune.render(matrices, runesConsumer, lightAbove, overlay)
            }
            matrices.translate(0.0, -popup, 0.0)
        }

        val coreModel = if(entity.key != EntangledTank.DEFAULT_KEY) coreModelDiamond else coreModelGold

        coreModel.render(matrices, chestConsumer, lightAbove, overlay)

        topModel.render(matrices, chestConsumer, lightAbove, overlay)

        matrices.pop()

        EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.add(Pair(entity.key, entity.colorCode))
        val tank = EntangledTankState.CLIENT_STATES[entity.key]?.fluidInvMap?.get(entity.colorCode) ?: return
        val fluid = tank.variant
        val fluidColor = FluidVariantRendering.getColor(fluid, entity.world, entity.pos)
        val sprite = FluidVariantRendering.getSprite(fluid) ?: return
        val color = Color((fluidColor shr 16 and 255), (fluidColor shr 8 and 255), (fluidColor and 255))

        val renderLayer = if(fluid != Fluids.EMPTY) {
            if(MinecraftClient.isFabulousGraphicsOrBetter()) {
                RenderLayer.getSolid()
            }else{
                RenderLayers.getFluidLayer(fluid.fluid.defaultState)
            }
        } else {
            RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
        }

        val bb = vertexConsumers.getBuffer(renderLayer)
        val entry = matrices.peek()
        val normal = Direction.NORTH.unitVector

        var p = MathHelper.lerp(tickDelta, entity.lastRenderedFluid, tank.amount / 81000f)
        entity.lastRenderedFluid = p

        val partUv = TankBlockEntityRenderer.UV(sprite)
        partUv.maxV -= (sprite.maxV - sprite.minV)*((16f-p)/16f)
        p /= 16f
        p = MathHelper.lerp(p, 0.126f, 0.874f)

        renderVertices(bb, entry, normal, color, overlay, light, partUv, 0.126f, 0.874f, 0.126f, p, 0.874f, 0.874f, 0.874f, 0.874f) //Direction.SOUTH
        renderVertices(bb, entry, normal, color, overlay, light, partUv, 0.126f, 0.874f, p, 0.126f, 0.126f, 0.126f, 0.126f, 0.126f) //Direction.NORTH
        renderVertices(bb, entry, normal, color, overlay, light, partUv, 0.874f, 0.874f, p, 0.126f, 0.126f, 0.874f, 0.874f, 0.126f) //Direction.EAST
        renderVertices(bb, entry, normal, color, overlay, light, partUv, 0.126f, 0.126f, 0.126f, p, 0.126f, 0.874f, 0.874f, 0.126f) //Direction.WEST

        val fullUv = TankBlockEntityRenderer.UV(sprite)

        renderVertices(bb, entry, normal, color, overlay, light, fullUv, 0.126f, 0.874f, 0.126f, 0.126f, 0.126f, 0.126f, 0.874f, 0.874f) //Direction.DOWN
        renderVertices(bb, entry, normal, color, overlay, light, fullUv, 0.126f, 0.874f, p, p, 0.874f, 0.874f, 0.126f, 0.126f) //Direction.UP

    }

    private fun renderVertices(bb: VertexConsumer, entry: MatrixStack.Entry, normal: Vec3f, color: Color, overlay: Int, light: Int, uv: TankBlockEntityRenderer.UV, f: Float, g: Float, h: Float, i: Float, j: Float, k: Float, l: Float, m: Float) {
        bb.vertex(entry.model, f, h, j).color(color.red / 255f, color.green / 255f, color.blue / 255f, 1f).texture(uv.maxU, uv.minV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, g, h, k).color(color.red / 255f, color.green / 255f, color.blue / 255f, 1f).texture(uv.minU, uv.minV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, g, i, l).color(color.red / 255f, color.green / 255f, color.blue / 255f, 1f).texture(uv.minU, uv.maxV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, f, i, m).color(color.red / 255f, color.green / 255f, color.blue / 255f, 1f).texture(uv.maxU, uv.maxV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
    }




}