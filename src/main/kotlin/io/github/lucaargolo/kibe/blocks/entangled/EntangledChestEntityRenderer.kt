package io.github.lucaargolo.kibe.blocks.entangled

import io.github.lucaargolo.kibe.ENTANGLED_HANDLER
import net.minecraft.block.enums.ChestType
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import java.util.function.Function

class EntangledChestEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<EntangledChestEntity>(dispatcher) {

    val topPart: ModelPart = ModelPart(64, 64, 0, 0);
    val bottomPart: ModelPart = ModelPart(64, 64, 0, 0);
    val handle: ModelPart = ModelPart(64, 64, 0, 0)

    init {
        this.topPart.addCuboid(1.0f, 0.0f, 1.0f, 14.0f, 10.0f, 14.0f, 0.0f)
        this.bottomPart.addCuboid(1.0f, 0.0f, 0.0f, 14.0f, 5.0f, 14.0f, 0.0f)
        this.bottomPart.pivotY = 9.0f
        this.bottomPart.pivotZ = 1.0f
        this.handle.addCuboid(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
        this.handle.pivotY = 8.0f
    }

    override fun render(blockEntity: EntangledChestEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val world = blockEntity.world
        val blockState =
            if (world != null) blockEntity.cachedState
            else (ENTANGLED_HANDLER.ENTANGLED_CHEST.defaultState.with(
                Properties.HORIZONTAL_FACING,
                Direction.SOUTH
            ))
        matrices.push()
        val f = (blockState.get(Properties.HORIZONTAL_FACING) as Direction).asRotation()
        matrices.translate(0.5, 0.5, 0.5)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-f))
        matrices.translate(-0.5, -0.5, -0.5)
        var g = 0F
        g = 1.0f - g
        g = 1.0f - g * g * g
        val spriteIdentifier = TexturedRenderLayers.getChestTexture(blockEntity, ChestType.SINGLE, false)
        val vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumers, Function { texture: Identifier? -> RenderLayer.getEntityCutout(texture) })

        val i = 1 // LIGHT?
        bottomPart.pitch = -(g * 1.5707964f)
        handle.pitch = bottomPart.pitch
        bottomPart.render(matrices, vertexConsumer, i, overlay)
        handle.render(matrices, vertexConsumer, i, overlay)
        topPart.render(matrices, vertexConsumer, i, overlay)
        matrices.pop()
    }

}