package io.github.lucaargolo.kibe.client.model

import io.github.lucaargolo.kibe.utils.ModIdentifier
import io.github.lucaargolo.kibe.utils.helper.FluidHelper
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.awt.Color
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.util.function.Function
import java.util.function.Supplier

class TankBlockItemBakedModel: UnbakedModel, BakedModel, FabricBakedModel {

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
    override fun setParents(modelLoader: Function<Identifier, UnbakedModel>?) {}
    override fun bake(baker: Baker?, textureGetter: Function<SpriteIdentifier, Sprite>?, rotationContainer: ModelBakeSettings?) = this

    override fun isVanillaAdapter(): Boolean = false

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {

        val client = MinecraftClient.getInstance()
        val tankBlockModel = client.bakedModelManager.getModel(ModelIdentifier(ModIdentifier.of("tank"), "level=0"))

        (tankBlockModel as? TankCustomModel)?.emitBlockQuads(null, null, BlockPos.ORIGIN, randSupplier, context)

        val blockEntityTag = stack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt() ?: NbtCompound()

        val dummyFluidTank = object: SingleVariantStorage<FluidVariant>() {
            override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
            override fun getCapacity(variant: FluidVariant?): Long = FluidConstants.BUCKET * 16
        }
        FluidHelper.readTank(blockEntityTag, dummyFluidTank)

        val player = client.player
        val world = player?.world
        val pos = player?.blockPos

        val fluid = dummyFluidTank.resource.fluid
        val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid) ?: return
        val fluidColor = fluidRenderHandler.getFluidColor(world, pos, fluid.defaultState)
        val fluidSprite = fluidRenderHandler.getFluidSprites(world, pos, fluid.defaultState)[0]
        val color = Color((fluidColor shr 16 and 255), (fluidColor shr 8 and 255), (fluidColor and 255)).rgb

        context.pushTransform { quad ->
            quad.color(color, color, color, color)
            true
        }

        val emitter = context.emitter

        val p = dummyFluidTank.amount/(FluidConstants.BUCKET*16f)
        emitter.draw(Direction.UP, fluidSprite, 0f, 0f, 1f, 1f, (1f-p)+0.001f )
        emitter.draw(Direction.DOWN, fluidSprite, 0f, 0f, 1f, 1f, 0.001f)
        emitter.draw(Direction.NORTH, fluidSprite, 0f, 0f, 1f, p, 0.001f)
        emitter.draw(Direction.SOUTH, fluidSprite, 0f, 0f, 1f, p, 0.001f)
        emitter.draw(Direction.EAST, fluidSprite, 0f, 0f, 1f, p, 0.001f)
        emitter.draw(Direction.WEST, fluidSprite, 0f, 0f, 1f, p, 0.001f)

        context.popTransform()
    }

    private fun QuadEmitter.draw(side: Direction, sprite: Sprite, left: Float, bottom: Float, right: Float, top: Float, depth: Float) {
        square(side, left, bottom, right, top, depth)
        spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
        color(-1, -1, -1, -1)
        emit()
    }

    override fun emitBlockQuads(p0: BlockRenderView?, p1: BlockState?, p2: BlockPos?, p3: Supplier<Random>?, p4: RenderContext?) {}

    @Throws(IOException::class, NoSuchElementException::class)
    private fun getReaderForResource(location: Identifier): Reader {
        val file = Identifier.of(location.namespace, location.path + ".json")
        val resource = MinecraftClient.getInstance().resourceManager.getResource(file).get()
        return BufferedReader(InputStreamReader(resource.inputStream, Charsets.UTF_8))
    }

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun getParticleSprite() = null

    override fun hasDepth(): Boolean = false

    private val transform: ModelTransformation? by lazy {
        loadTransformFromJson(Identifier.of("minecraft:models/block/block"))
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

}