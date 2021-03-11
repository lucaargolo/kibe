package io.github.lucaargolo.kibe.blocks.entangledtank

import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.tank.TankBlockEntityRenderer
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.options.GraphicsMode
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.fluid.Fluids
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.state.property.Properties
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import java.awt.Color

class EntangledTankEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<EntangledTankEntity>(dispatcher) {

    private val bottomModel = ModelPart(64, 64, 0, 0)
    private val topModel = ModelPart(64, 64, 0, 0)

    init {
        bottomModel.addCuboid(1F, 0F, 1F, 14F, 1F, 14F) // TANK_BOTTOM

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

    override fun render(entity: EntangledTankEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        val world = entity.world
        val blockState = if (world != null) entity.cachedState else (ENTANGLED_TANK.defaultState.with(Properties.HORIZONTAL_FACING, Direction.SOUTH))

        matrices.push()

        val f = (blockState.get(Properties.HORIZONTAL_FACING) as Direction).asRotation()
        matrices.translate(0.5, 0.5, 0.5)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-f))
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
        ) 1 else 0

        (1..8).forEach {
            val rune = ModelPart(32, 32, 0, 0)
            rune.setTextureOffset(getRuneTextureU(entity.runeColors[it]!!), getRuneTextureV(entity.runeColors[it]!!))
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

        val upUV = if(entity.key != EntangledTank.DEFAULT_KEY) -10 else 0

        val core = ModelPart(64, 64, 0, 0)
        core.setTextureOffset(58, 50+upUV)
        core.addCuboid(9f, 14f, 7f, 1f, 2f, 2f)
        core.setTextureOffset(52, 44+upUV)
        core.addCuboid(7f, 14f, 6f, 2f, 2f, 4f)
        core.setTextureOffset(52, 50+upUV)
        core.addCuboid(6f, 14f, 7f, 1f, 2f, 2f)
        core.render(matrices, chestConsumer, lightAbove, overlay)

        topModel.render(matrices, chestConsumer, lightAbove, overlay)

        matrices.pop()

        (MinecraftClient.getInstance().player)?.let { player ->
            val list = EntangledTankState.CLIENT_PLAYER_REQUESTS.getOrPut(player) { linkedSetOf() }
            list.add(Pair(entity.key, entity.colorCode))
        }
        val fluidInv = EntangledTankState.CLIENT_STATES[entity.key]?.fluidInvMap?.get(entity.colorCode)
        val fluid = fluidInv?.getInvFluid(0)?.rawFluid ?: Fluids.EMPTY

        val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid) ?: return
        val fluidColor = fluidRenderHandler.getFluidColor(entity.world, entity.pos, fluid.defaultState)
        val sprite = fluidRenderHandler.getFluidSprites(entity.world, entity.pos, fluid.defaultState)[0]
        val color = Color((fluidColor shr 16 and 255), (fluidColor shr 8 and 255), (fluidColor and 255))

        val renderLayer = if(fluid != Fluids.EMPTY) {
            if(MinecraftClient.isFabulousGraphicsOrBetter()) {
                RenderLayer.getSolid()
            }else{
                RenderLayers.getFluidLayer(fluid.defaultState)
            }
        } else {
            RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
        }

        val bb = vertexConsumers.getBuffer(renderLayer)
        val entry = matrices.peek()
        val normal = Direction.NORTH.unitVector

        var p = MathHelper.lerp(tickDelta, entity.lastRenderedFluid, (fluidInv?.getInvFluid(0)?.amount()?.asLong(1000L) ?: 0L)/1000f)
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

    private fun renderVertices(bb: VertexConsumer, entry: MatrixStack.Entry, normal: Vector3f, color: Color, overlay: Int, light: Int, uv: TankBlockEntityRenderer.UV, f: Float, g: Float, h: Float, i: Float, j: Float, k: Float, l: Float, m: Float) {
        bb.vertex(entry.model, f, h, j).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(uv.maxU, uv.minV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, g, h, k).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(uv.minU, uv.minV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, g, i, l).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(uv.minU, uv.maxV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, f, i, m).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(uv.maxU, uv.maxV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
    }


    private fun getRuneTextureV(color: DyeColor): Int {
        return if(color.id >= 8) (color.id-8)*4 else color.id*4
    }

    private fun getRuneTextureU(color: DyeColor): Int {
        return if(color.id >= 8) 8 else 0
    }

}