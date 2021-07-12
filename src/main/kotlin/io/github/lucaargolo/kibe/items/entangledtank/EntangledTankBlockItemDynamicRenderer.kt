package io.github.lucaargolo.kibe.items.entangledtank

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTank
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntity
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntityRenderer
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.*

class EntangledTankBlockItemDynamicRenderer: BuiltinItemRendererRegistry.DynamicItemRenderer {

    override fun render(stack: ItemStack, mode: ModelTransformation.Mode, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, lightmap: Int, overlay: Int) {
        val tag = if(stack.hasNbt() && stack.nbt!!.contains("BlockEntityTag") ) {
            stack.orCreateNbt.get("BlockEntityTag") as NbtCompound
        }else{
            val newTag = NbtCompound()
            newTag.putString("key", EntangledTank.DEFAULT_KEY)
            (1..8).forEach {
                newTag.putString("rune$it", DyeColor.WHITE.name)
            }
            newTag.putString("colorCode", "00000000")
            newTag
        }

        var colorCode = ""
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
            colorCode += dc.id.let { int -> Integer.toHexString(int) }
        }

        val key = tag.getString("key")

        EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.add(Pair(key, colorCode))
        val fluidInv = EntangledTankState.CLIENT_STATES[key]?.fluidInvMap?.get(colorCode) ?: SimpleFixedFluidInv(1, FluidAmount.ONE)
        fluidInv.toTag(tag)

        val dummyTank = EntangledTankEntity(ENTANGLED_TANK as EntangledTank, MinecraftClient.getInstance().player?.blockPos ?: BlockPos.ORIGIN, ENTANGLED_TANK.defaultState)
        dummyTank.fromClientTag(tag)
        dummyTank.lastRenderedFluid = dummyTank.fluidInv.getInvFluid(0).amount().asLong(1000L) / 1000f

        val dummyRenderer = EntangledTankEntityRenderer(BlockEntityRendererFactory.Context(MinecraftClient.getInstance().blockEntityRenderDispatcher, MinecraftClient.getInstance().blockRenderManager, MinecraftClient.getInstance().entityModelLoader, MinecraftClient.getInstance().textRenderer))
        dummyRenderer.render(dummyTank, MinecraftClient.getInstance().tickDelta, matrixStack, vertexConsumerProvider, lightmap, overlay)

        val tankGlassIdentifier = ModelIdentifier(Identifier(MOD_ID, "entangled_tank"), "facing=north,level=0")
        val tankGlassModel = MinecraftClient.getInstance().bakedModelManager.getModel(tankGlassIdentifier)

        val cutoutBuffer = vertexConsumerProvider.getBuffer(RenderLayer.getCutout())
        tankGlassModel.getQuads(null, null, Random()).forEach { q ->
            cutoutBuffer.quad(matrixStack.peek(), q, 1f, 1f, 1f, lightmap, overlay)
        }

    }

}