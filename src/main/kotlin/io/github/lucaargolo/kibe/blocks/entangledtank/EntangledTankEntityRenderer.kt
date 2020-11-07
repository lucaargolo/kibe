package io.github.lucaargolo.kibe.blocks.entangledtank

import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.tank.TankBlockEntityRenderer
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.minecraft.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.render.entity.model.EntityModelLayers
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
import java.util.function.Function

class EntangledTankEntityRenderer(private val arg: BlockEntityRendererFactory.Arguments): BlockEntityRenderer<EntangledTankEntity> {

    companion object {
        val bottomModelLayer = EntityModelLayers.register("entangled_tank", "bottom")
        val topModelLayer = EntityModelLayers.register("entangled_tank", "top")

        val runeLayers: List<EntityModelLayer> = listOf<EntityModelLayer>(
            EntityModelLayers.register("entangled_tank", "rune1"),
            EntityModelLayers.register("entangled_tank", "rune2"),
            EntityModelLayers.register("entangled_tank", "rune3"),
            EntityModelLayers.register("entangled_tank", "rune4"),
            EntityModelLayers.register("entangled_tank", "rune5"),
            EntityModelLayers.register("entangled_tank", "rune6"),
            EntityModelLayers.register("entangled_tank", "rune7"),
            EntityModelLayers.register("entangled_tank", "rune8")
        )

        val coreModelLayer = EntityModelLayers.register("entangled_tank", "core")

        fun setupBottomModel(): class_5607 {
            val lv = class_5609()
            val lv2 = lv.method_32111()
            lv2.method_32117("bottom", class_5606.method_32108().method_32101(0, 0).method_32097(1.0f, 0.0f, 1.0f, 14.0f, 1.0f, 14.0f), class_5603.field_27701)
            return class_5607.method_32110(lv, 64, 64)
        }

        fun setupTopModel(): class_5607 {
            val lv = class_5609()
            val lv2 = lv.method_32111()
            lv2.method_32117("top1", class_5606.method_32108().method_32101(32, 43).method_32097(1F, 14F, 1F, 2F, 1F, 14F), class_5603.field_27701)
            lv2.method_32117("top2", class_5606.method_32108().method_32101(56, 0).method_32097(3F, 14F, 1F, 2F, 1F, 2F), class_5603.field_27701)
            lv2.method_32117("top3", class_5606.method_32108().method_32101(56, 3).method_32097(7F, 14F, 1F, 2F, 1F, 2F), class_5603.field_27701)
            lv2.method_32117("top4", class_5606.method_32108().method_32101(56, 6).method_32097(11F, 14F, 1F, 2F, 1F, 2F), class_5603.field_27701)
            lv2.method_32117("top5", class_5606.method_32108().method_32101(56, 31).method_32097(11F, 14F, 13F, 2F, 1F, 2F), class_5603.field_27701)
            lv2.method_32117("top6", class_5606.method_32108().method_32101(56, 28).method_32097(7F, 14F, 13F, 2F, 1F, 2F), class_5603.field_27701)
            lv2.method_32117("top7", class_5606.method_32108().method_32101(56, 25).method_32097(3F, 14F, 13F, 2F, 1F, 2F), class_5603.field_27701)
            lv2.method_32117("top8", class_5606.method_32108().method_32101(56, 17).method_32097(3F, 14F, 9F, 2F, 1F, 2F), class_5603.field_27701)
            lv2.method_32117("top9", class_5606.method_32108().method_32101(56, 9).method_32097(3F, 14F, 5F, 2F, 1F, 2F), class_5603.field_27701)
            lv2.method_32117("top10", class_5606.method_32108().method_32101(56, 14).method_32097(11F, 14F, 5F, 2F, 1F, 2F), class_5603.field_27701)
            lv2.method_32117("top11", class_5606.method_32108().method_32101(58, 12).method_32097(7F, 14F, 5F, 2F, 1F, 1F), class_5603.field_27701)
            lv2.method_32117("top12", class_5606.method_32108().method_32101(56, 22).method_32097(11F, 14F, 9F, 2F, 1F, 2F), class_5603.field_27701)
            lv2.method_32117("top13", class_5606.method_32108().method_32101(58, 20).method_32097(7F, 14F, 10F, 2F, 1F, 1F), class_5603.field_27701)
            lv2.method_32117("top14", class_5606.method_32108().method_32101(0, 43).method_32097(13F, 14F, 1F, 2F, 1F, 14F), class_5603.field_27701)
            lv2.method_32117("top15", class_5606.method_32108().method_32101(16, 42).method_32097(10F, 14F, 1F, 1F, 1F, 14F), class_5603.field_27701)
            lv2.method_32117("top16", class_5606.method_32108().method_32101(0, 0).method_32097(9F, 14F, 1F, 1F, 1F, 6F), class_5603.field_27701)
            lv2.method_32117("top17", class_5606.method_32108().method_32101(0, 24).method_32097(6F, 14F, 1F, 1F, 1F, 6F), class_5603.field_27701)
            lv2.method_32117("top18", class_5606.method_32108().method_32101(0, 31).method_32097(6F, 14F, 9F, 1F, 1F, 6F), class_5603.field_27701)
            lv2.method_32117("top19", class_5606.method_32108().method_32101(0, 7).method_32097(9F, 14F, 9F, 1F, 1F, 6F), class_5603.field_27701)
            lv2.method_32117("top20", class_5606.method_32108().method_32101(16, 42).method_32097(5F, 14F, 1F, 1F, 1F, 14F), class_5603.field_27701)
            return class_5607.method_32110(lv, 64, 64)
        }

        fun setupRuneModel(runeId: Int): class_5607 {
            val lv = class_5609()
            val lv2 = lv.method_32111()
            when(runeId) {
                0 -> lv2.method_32117("rune1", class_5606.method_32108().method_32101(getRuneTextureU(DyeColor.WHITE), getRuneTextureV(DyeColor.WHITE)).method_32097(11f, 13f, 11f, 2f, 2f, 2f), class_5603.field_27701)
                1 -> lv2.method_32117("rune2", class_5606.method_32108().method_32101(getRuneTextureU(DyeColor.WHITE), getRuneTextureV(DyeColor.WHITE)).method_32097(7f, 13f, 11f, 2f, 2f, 2f), class_5603.field_27701)
                2 -> lv2.method_32117("rune3", class_5606.method_32108().method_32101(getRuneTextureU(DyeColor.WHITE), getRuneTextureV(DyeColor.WHITE)).method_32097(3f, 13f, 11f, 2f, 2f, 2f), class_5603.field_27701)
                3 -> lv2.method_32117("rune4", class_5606.method_32108().method_32101(getRuneTextureU(DyeColor.WHITE), getRuneTextureV(DyeColor.WHITE)).method_32097(3f, 13f, 7f, 2f, 2f, 2f), class_5603.field_27701)
                4 -> lv2.method_32117("rune5", class_5606.method_32108().method_32101(getRuneTextureU(DyeColor.WHITE), getRuneTextureV(DyeColor.WHITE)).method_32097(3f, 13f, 3f, 2f, 2f, 2f), class_5603.field_27701)
                5 -> lv2.method_32117("rune6", class_5606.method_32108().method_32101(getRuneTextureU(DyeColor.WHITE), getRuneTextureV(DyeColor.WHITE)).method_32097(7f, 13f, 3f, 2f, 2f, 2f), class_5603.field_27701)
                6 -> lv2.method_32117("rune7", class_5606.method_32108().method_32101(getRuneTextureU(DyeColor.WHITE), getRuneTextureV(DyeColor.WHITE)).method_32097(11f, 13f, 3f, 2f, 2f, 2f), class_5603.field_27701)
                7 -> lv2.method_32117("rune8", class_5606.method_32108().method_32101(getRuneTextureU(DyeColor.WHITE), getRuneTextureV(DyeColor.WHITE)).method_32097(11f, 13f, 7f, 2f, 2f, 2f), class_5603.field_27701)
            }
            return class_5607.method_32110(lv, 32, 32)
        }

        fun setupCoreModel(): class_5607 {
            val lv = class_5609()
            val lv2 = lv.method_32111()
            lv2.method_32117("idk", class_5606.method_32108().method_32101(58, 50).method_32097(9f, 14f, 7f, 1f, 2f, 2f), class_5603.field_27701)
            lv2.method_32117("what", class_5606.method_32108().method_32101(52, 44).method_32097(7f, 14f, 6f, 2f, 2f, 4f), class_5603.field_27701)
            lv2.method_32117("amdoing", class_5606.method_32108().method_32101(52, 50).method_32097(6f, 14f, 7f, 1f, 2f, 2f), class_5603.field_27701)
            return class_5607.method_32110(lv, 64, 64)
        }

        private fun getRuneTextureV(color: DyeColor): Int {
            return if(color.id >= 8) (color.id-8)*4 else color.id*4
        }

        private fun getRuneTextureU(color: DyeColor): Int {
            return if(color.id >= 8) 8 else 0
        }

    }

    private val bottomModel = arg.getLayerModelPart(bottomModelLayer)
    private val topModel = arg.getLayerModelPart(topModelLayer)
    private val runeModels = arrayListOf<ModelPart>(
        arg.getLayerModelPart(runeLayers[0]),
        arg.getLayerModelPart(runeLayers[1]),
        arg.getLayerModelPart(runeLayers[2]),
        arg.getLayerModelPart(runeLayers[3]),
        arg.getLayerModelPart(runeLayers[4]),
        arg.getLayerModelPart(runeLayers[5]),
        arg.getLayerModelPart(runeLayers[6]),
        arg.getLayerModelPart(runeLayers[7])
    )
    private val coreModel = arg.getLayerModelPart(coreModelLayer)

    override fun render(entity: EntangledTankEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        val world = entity.world
        val blockState = if (world != null) entity.cachedState else (ENTANGLED_TANK.defaultState.with(Properties.HORIZONTAL_FACING, Direction.SOUTH))

        matrices.push()

        val f = (blockState.get(Properties.HORIZONTAL_FACING) as Direction).asRotation()
        matrices.translate(0.5, 0.5, 0.5)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-f))
        matrices.translate(-0.5, -0.5, -0.5)

        val chestIdentifier = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/entangled_chest"))
        val chestConsumer = chestIdentifier.getVertexConsumer(vertexConsumers, Function { texture: Identifier? -> RenderLayer.getEntityCutout(texture) })

        val runesIdentifier = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("kibe:block/entangled_chest_runes"))
        val runesConsumer = runesIdentifier.getVertexConsumer(vertexConsumers, Function { texture: Identifier? -> RenderLayer.getEntityCutout(texture) })

        val lightAbove = entity.world?.let { WorldRenderer.getLightmapCoordinates(it, entity.pos) } ?: light
        bottomModel.render(matrices, chestConsumer, lightAbove, overlay)

        val popup = if(
            MinecraftClient.getInstance().crosshairTarget!!.type == HitResult.Type.BLOCK &&
            (MinecraftClient.getInstance().crosshairTarget!! as BlockHitResult).blockPos == entity.pos &&
            MinecraftClient.getInstance().player!!.getStackInHand(Hand.MAIN_HAND).item is Rune
        ) 1 else 0

        (1..8).forEach {
            val rune = runeModels[it-1]
            rune.render(matrices, runesConsumer, lightAbove, overlay)
        }

        val upUV = if(entity.key != EntangledTank.DEFAULT_KEY) -10 else 0;

        coreModel.render(matrices, chestConsumer, lightAbove, overlay)

        topModel.render(matrices, chestConsumer, lightAbove, overlay)

        matrices.pop()

        val fluidInv = entity.fluidInv
        val fluid = fluidInv.getInvFluid(0).rawFluid ?: Fluids.EMPTY

        val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid) ?: return
        val fluidColor = fluidRenderHandler.getFluidColor(entity.world, entity.pos, fluid.defaultState)
        val sprite = fluidRenderHandler.getFluidSprites(entity.world, entity.pos, fluid.defaultState)[0]
        val color = Color((fluidColor shr 16 and 255), (fluidColor shr 8 and 255), (fluidColor and 255))

        val bb = vertexConsumers.getBuffer(if (fluid != Fluids.EMPTY) RenderLayers.getFluidLayer(fluid.defaultState) else RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE))
        val entry = matrices.peek()
        val normal = Direction.NORTH.unitVector

        var p = MathHelper.lerp(tickDelta, entity.lastRenderedFluid, fluidInv.getInvFluid(0).amount().asLong(1000L) / 1000f)
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
        bb.vertex(entry.model, f, h, j).color(color.red / 255f, color.green / 255f, color.blue / 255f, 1f).texture(uv.maxU, uv.minV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, g, h, k).color(color.red / 255f, color.green / 255f, color.blue / 255f, 1f).texture(uv.minU, uv.minV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, g, i, l).color(color.red / 255f, color.green / 255f, color.blue / 255f, 1f).texture(uv.minU, uv.maxV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, f, i, m).color(color.red / 255f, color.green / 255f, color.blue / 255f, 1f).texture(uv.maxU, uv.maxV).overlay(overlay).light(light).normal(entry.normal, normal.x, normal.y, normal.z).next()
    }




}