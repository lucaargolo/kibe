package io.github.lucaargolo.kibe.items.entangledbucket

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTank
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankState
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.fluid.Fluids
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
import java.util.function.Consumer
import java.util.function.Supplier

class EntangledBucketBakedModel: BakedModel, FabricBakedModel {

    override fun isVanillaAdapter(): Boolean = false

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {

        val background = ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_background"), "inventory")
        val backgroundModel = MinecraftClient.getInstance().bakedModelManager.getModel(background)
        context.fallbackConsumer().accept(backgroundModel)

        val tag = if(stack.hasTag()) {
            stack.orCreateTag
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
        (MinecraftClient.getInstance().player)?.let { player ->
            val list = EntangledTankState.CLIENT_PLAYER_REQUESTS.getOrPut(player) { linkedSetOf() }
            list.add(Pair(key, colorCode))
        }
        val fluidInv = EntangledTankState.CLIENT_STATES[key]?.fluidInvMap?.get(colorCode) ?: SimpleFixedFluidInv(1, FluidAmount.ONE)
        val fluid = fluidInv.getInvFluid(0).rawFluid ?: Fluids.EMPTY

        if(fluid != Fluids.EMPTY) {
            val fluidRenderHandler: FluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid)
            val fluidIdentifier = ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_fluid"), "inventory")
            val fluidModel = MinecraftClient.getInstance().bakedModelManager.getModel(fluidIdentifier)

            val fluidColor: Int = fluidRenderHandler.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player!!.blockPos, fluid.defaultState)
            val fluidSprite: Sprite = fluidRenderHandler.getFluidSprites(MinecraftClient.getInstance().world, BlockPos.ORIGIN, fluid.defaultState)[0]
            val colorInt = Color((fluidColor shr 16 and 255), (fluidColor shr 8 and 255), (fluidColor and 255)).rgb

            context.pushTransform { quad ->
                quad.nominalFace(GeometryHelper.lightFace(quad))
                quad.spriteColor(0, colorInt, colorInt, colorInt, colorInt)
                quad.spriteBake(0, fluidSprite, MutableQuadView.BAKE_LOCK_UV)
                true
            }

            val emitter = context.emitter
            fluidModel.getQuads(null, null, randSupplier.get()).forEach(Consumer { q: BakedQuad ->
                emitter.fromVanilla(q.vertexData, 0, false)
                emitter.emit()
            })
            context.popTransform()

        }

        val foreground = ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_foreground"), "inventory")
        val foregroundModel = MinecraftClient.getInstance().bakedModelManager.getModel(foreground)
        context.fallbackConsumer().accept(foregroundModel)

        val core = if(stack.hasTag() && stack.tag!!.contains("key") && stack.tag!!.getString("key") != EntangledTank.DEFAULT_KEY)
            ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_diamond_core"), "inventory")
        else ModelIdentifier(Identifier(MOD_ID, "entangled_bucket_gold_core"), "inventory")
        val coreModel = MinecraftClient.getInstance().bakedModelManager.getModel(core)
        context.fallbackConsumer().accept(coreModel)

        var color = Color.WHITE.rgb
        if(stack.hasTag() && stack.tag!!.contains("rune1")) {
            var sumr = 0
            var sumg = 0
            var sumb = 0
            (1..8).forEach {
                val dye = DyeColor.byName(stack.tag!!.getString("rune$it"), DyeColor.WHITE)
                val dyeColor = Color(dye.materialColor.color)
                sumr += dyeColor.red
                sumg += dyeColor.green
                sumb += dyeColor.blue
            }
            color = Color(sumr / 8, sumg / 8, sumb / 8, 255).rgb
        }
        context.pushTransform { quad ->
            quad.spriteColor(0, color, color, color, color)
            true
        }
        val emitter = context.emitter
        val ring = ModelIdentifier(Identifier(MOD_ID, "entangled_ring"), "inventory")
        val ringModel = MinecraftClient.getInstance().bakedModelManager.getModel(ring)
        ringModel.getQuads(null, null, randSupplier.get()).forEach { q ->
            emitter.fromVanilla(q.vertexData, 0, true)
            emitter.emit()
        }
        context.popTransform()
    }


    override fun emitBlockQuads(p0: BlockRenderView?, p1: BlockState?, p2: BlockPos?, p3: Supplier<Random>?, p4: RenderContext?) {}

    @Throws(IOException::class)
    private fun getReaderForResource(location: Identifier): Reader {
        val file = Identifier(location.namespace, location.path + ".json")
        val resource = MinecraftClient.getInstance().resourceManager.getResource(file)
        return BufferedReader(InputStreamReader(resource.inputStream, Charsets.UTF_8))
    }

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun getSprite() = null

    override fun hasDepth(): Boolean = false

    override fun getTransformation(): ModelTransformation? = loadTransformFromJson(Identifier("minecraft:models/item/generated"))

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

}