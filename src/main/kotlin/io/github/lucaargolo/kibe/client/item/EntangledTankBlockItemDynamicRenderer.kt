package io.github.lucaargolo.kibe.client.item

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.block.EntangledTank
import io.github.lucaargolo.kibe.blockentity.EntangledTankEntity
import io.github.lucaargolo.kibe.client.blockentity.EntangledTankEntityRenderer
import io.github.lucaargolo.kibe.data.state.EntangledTankState
import io.github.lucaargolo.kibe.utils.ModIdentifier
import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random

class EntangledTankBlockItemDynamicRenderer: BuiltinItemRendererRegistry.DynamicItemRenderer {

    override fun render(stack: ItemStack, mode: ModelTransformationMode, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, lightmap: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()
        val world = client.world ?: return

        val tag = stack.components.get(DataComponentTypes.BLOCK_ENTITY_DATA)?.copyNbt() ?: run{
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
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE) ?: DyeColor.WHITE
            colorCode += dc.id.let { int -> Integer.toHexString(int) }
        }

        val key = tag.getString("key")

        EntangledTankState.CURRENT_CLIENT_PLAYER_REQUESTS.add(Pair(key, colorCode))
        val fluidInv = EntangledTankState.CLIENT_STATES[key]?.fluidInvMap?.get(colorCode) ?: object: SingleVariantStorage<FluidVariant>() {
            override fun getCapacity(variant: FluidVariant?) = 0L
            override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
        }
        FluidHelper.writeTank(tag, fluidInv)

        val dummyTank = EntangledTankEntity(client.player?.blockPos ?: BlockPos.ORIGIN, BlockCompendium.ENTANGLED_TANK.defaultState)
        dummyTank.readClientNbt(tag, world.registryManager)
        dummyTank.lastRenderedFluid = dummyTank.getTank().amount / 81000f

        val dummyRenderer = EntangledTankEntityRenderer(BlockEntityRendererFactory.Context(MinecraftClient.getInstance().blockEntityRenderDispatcher, MinecraftClient.getInstance().blockRenderManager, MinecraftClient.getInstance().itemRenderer, MinecraftClient.getInstance().entityRenderDispatcher, MinecraftClient.getInstance().entityModelLoader, MinecraftClient.getInstance().textRenderer))
        dummyRenderer.render(dummyTank, client.renderTickCounter.getTickDelta(true), matrixStack, vertexConsumerProvider, lightmap, overlay)

        val tankGlassIdentifier = ModelIdentifier(ModIdentifier.of("entangled_tank"), "facing=north,level=0")
        val tankGlassModel = client.bakedModelManager.getModel(tankGlassIdentifier)

        val cutoutBuffer = vertexConsumerProvider.getBuffer(RenderLayer.getCutout())
        tankGlassModel.getQuads(null, null, Random.create()).forEach { q ->
            cutoutBuffer.quad(matrixStack.peek(), q, 1f, 1f, 1f, 1f, lightmap, overlay)
        }

    }

}