@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.kibe.blocks.tank

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.fluid.Fluids
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3f
import java.awt.Color

class TankBlockEntityRenderer(private val arg: BlockEntityRendererFactory.Context): BlockEntityRenderer<TankBlockEntity> {

    class UV(var minU: Float, var minV: Float, var maxU: Float, var maxV: Float) {
        constructor(sprite: Sprite): this(sprite.minU, sprite.minV, sprite.maxU, sprite.maxV)
    }

    override fun render(entity: TankBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        val fluid = entity.tank.variant
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

        var p = MathHelper.lerp(tickDelta, entity.lastRenderedFluid, entity.tank.amount / 81000f)
        entity.lastRenderedFluid = p

        val partUv = UV(sprite)
        partUv.maxV -= (sprite.maxV - sprite.minV)*((16f-p)/16f)
        p /= 16f
        p -= 0.001f

        renderVertices(bb, entry, normal, color, overlay, light, partUv, 0.001f, 0.999f, 0.001f, p, 0.999f, 0.999f, 0.999f, 0.999f) //Direction.SOUTH
        renderVertices(bb, entry, normal, color, overlay, light, partUv, 0.001f, 0.999f, p, 0.001f, 0.001f, 0.001f, 0.001f, 0.001f) //Direction.NORTH
        renderVertices(bb, entry, normal, color, overlay, light, partUv, 0.999f, 0.999f, p, 0.001f, 0.001f, 0.999f, 0.999f, 0.001f) //Direction.EAST
        renderVertices(bb, entry, normal, color, overlay, light, partUv, 0.001f, 0.001f, 0.001f, p, 0.001f, 0.999f, 0.999f, 0.001f) //Direction.WEST

        val fullUv = UV(sprite)

        renderVertices(bb, entry, normal, color, overlay, light, fullUv, 0.001f, 0.999f, 0.001f, 0.001f, 0.001f, 0.001f, 0.999f, 0.999f) //Direction.DOWN
        renderVertices(bb, entry, normal, color, overlay, light, fullUv, 0.001f, 0.999f, p, p, 0.999f, 0.999f, 0.001f, 0.001f) //Direction.UP

    }

    private fun renderVertices(bb: VertexConsumer, entry: MatrixStack.Entry, normal: Vec3f, color: Color, overlay: Int, light: Int,  uv: UV,  f: Float, g: Float, h: Float, i: Float, j: Float, k: Float, l: Float, m: Float) {
        bb.vertex(entry.positionMatrix, f, h, j).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(uv.maxU, uv.minV).overlay(overlay).light(light).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.positionMatrix, g, h, k).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(uv.minU, uv.minV).overlay(overlay).light(light).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.positionMatrix, g, i, l).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(uv.minU, uv.maxV).overlay(overlay).light(light).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.positionMatrix, f, i, m).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(uv.maxU, uv.maxV).overlay(overlay).light(light).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
    }


}