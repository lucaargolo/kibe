package io.github.lucaargolo.kibe.items.entangledtank

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.REQUEST_ENTANGLED_TANK_SYNC_C2S
import io.github.lucaargolo.kibe.blocks.ENTANGLED_TANK
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTank
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntity
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntityRenderer
import io.github.lucaargolo.kibe.utils.EntangledTankCache
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.impl.client.indigo.renderer.render.ItemRenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import java.awt.Color
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.util.*
import java.util.function.Supplier

class EntangledTankBlockItemBakedModel: BakedModel, FabricBakedModel {

    override fun isVanillaAdapter(): Boolean = false

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {

        try {
            val itemRenderContext: ItemRenderContext = context as? ItemRenderContext ?: return

            val matrixStackField = itemRenderContext.javaClass.getDeclaredField("matrixStack")
            matrixStackField.isAccessible = true
            val matrixStack = matrixStackField.get(itemRenderContext) as MatrixStack

            val vertexConsumerProviderField = itemRenderContext.javaClass.getDeclaredField("vertexConsumerProvider")
            vertexConsumerProviderField.isAccessible = true
            val vertexConsumerProvider = vertexConsumerProviderField.get(itemRenderContext) as VertexConsumerProvider

            val lightmapField = itemRenderContext.javaClass.getDeclaredField("lightmap")
            lightmapField.isAccessible = true
            val lightmap = lightmapField.get(itemRenderContext) as Int

            val overlayField = itemRenderContext.javaClass.getDeclaredField("overlay")
            overlayField.isAccessible = true
            val overlay = overlayField.get(itemRenderContext) as Int

            val tag = if(stack.hasTag() && stack.tag!!.contains("BlockEntityTag") ) {
                stack.orCreateTag.get("BlockEntityTag") as CompoundTag
            }else{
                val newTag = CompoundTag()
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
            if(EntangledTankCache.isDirty(key, colorCode)) {
                val passedData = PacketByteBuf(Unpooled.buffer())
                passedData.writeString(key)
                passedData.writeString(colorCode)
                ClientSidePacketRegistry.INSTANCE.sendToServer(REQUEST_ENTANGLED_TANK_SYNC_C2S, passedData)
            }

            val fluidInv = EntangledTankCache.getOrCreateClientFluidInv(key, colorCode, tag)
            fluidInv.toTag(tag)

            val dummyTank = EntangledTankEntity(ENTANGLED_TANK as EntangledTank)
            dummyTank.fromClientTag(tag)
            dummyTank.pos = MinecraftClient.getInstance().player?.blockPos ?: BlockPos.ORIGIN
            dummyTank.lastRenderedFluid = dummyTank.fluidInv.getInvFluid(0).amount().asLong(1000L) / 1000f

            val dummyRenderer = EntangledTankEntityRenderer(BlockEntityRenderDispatcher.INSTANCE)
            dummyRenderer.render(dummyTank, MinecraftClient.getInstance().tickDelta, matrixStack, vertexConsumerProvider, lightmap, overlay)

            val color = Color(255, 255, 255).rgb

            itemRenderContext.pushTransform { quad ->
                quad.spriteColor(0, color, color, color, color)
                true
            }

            val emitter = itemRenderContext.emitter

            val tankGlassIdentifier = ModelIdentifier(Identifier(MOD_ID, "entangled_tank"), "facing=north,level=0")
            val tankGlassModel = MinecraftClient.getInstance().bakedModelManager.getModel(tankGlassIdentifier)

            tankGlassModel.getQuads(null, null, randSupplier.get()).forEach { q ->
                emitter.fromVanilla(q.vertexData, 0, true)
                emitter.emit()
            }

            itemRenderContext.popTransform()
        }catch (e: Exception) { }

    }

    override fun emitBlockQuads(p0: BlockRenderView?, p1: BlockState?, p2: BlockPos?, p3: Supplier<Random>?, p4: RenderContext?) {}

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun getSprite() = null

    override fun hasDepth(): Boolean = false

    private val transform: ModelTransformation? by lazy {
        loadTransformFromJson(Identifier("minecraft:models/block/block"))
    }

    override fun getTransformation(): ModelTransformation? = transform

    override fun useAmbientOcclusion(): Boolean = true

    override fun isSideLit(): Boolean = false

    override fun isBuiltin(): Boolean = false

    private fun loadTransformFromJson(location: Identifier): ModelTransformation? {
        return try {
            JsonUnbakedModel.deserialize(getReaderForResource(location)).transformations
        } catch (exception: IOException) {
            exception.printStackTrace()
            null
        }
    }

    @Throws(IOException::class)
    private fun getReaderForResource(location: Identifier): Reader {
        val file = Identifier(location.namespace, location.path + ".json")
        val resource = MinecraftClient.getInstance().resourceManager.getResource(file)
        return BufferedReader(InputStreamReader(resource.inputStream, Charsets.UTF_8))
    }

}