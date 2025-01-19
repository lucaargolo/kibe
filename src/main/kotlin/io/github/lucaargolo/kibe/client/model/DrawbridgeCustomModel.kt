package io.github.lucaargolo.kibe.client.model

import io.github.lucaargolo.kibe.blockentity.DrawbridgeBlockEntity
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.util.function.Function
import java.util.function.Supplier

class DrawbridgeCustomModel: UnbakedModel, BakedModel, FabricBakedModel {

    private val spriteIdList = mutableListOf(
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier.of("block/drawbridge_front")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier.of("block/drawbridge_side")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier.of("block/drawbridge_back"))
    )
    val spriteList = mutableListOf<Sprite>()

    private val modelIdList = mutableListOf(
        ModIdentifier.of("block/drawbridge")
    )
    val modelList = mutableListOf<BakedModel>()

    override fun getModelDependencies(): Collection<Identifier> = listOf()

    override fun setParents(modelLoader: Function<Identifier, UnbakedModel>?) { }

    lateinit var modelTransformation: ModelTransformation

    override fun bake(baker: Baker, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings): BakedModel {
        val model = baker.getOrLoadModel(modelIdList[0])

        modelList.add(model.bake(baker, textureGetter, ModelRotation.X0_Y0)!!) // NORTH
        modelList.add(model.bake(baker, textureGetter, ModelRotation.X0_Y180)!!) // SOUTH
        modelList.add(model.bake(baker, textureGetter, ModelRotation.X0_Y270)!!) // WEST
        modelList.add(model.bake(baker, textureGetter, ModelRotation.X0_Y90)!!) // EAST
        modelList.add(model.bake(baker, textureGetter, ModelRotation.X270_Y0)!!) // UP
        modelList.add(model.bake(baker, textureGetter, ModelRotation.X90_Y0)!!) // DOWN

        modelTransformation = modelList[0].transformation

        spriteIdList.forEachIndexed { _, spriteIdentifier ->
            spriteList.add(textureGetter.apply(spriteIdentifier))
        }
        return this
    }

    override fun getParticleSprite() = spriteList[0]

    override fun isVanillaAdapter() = false

    override fun emitBlockQuads(world: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        (world.getBlockEntity(pos) as? DrawbridgeBlockEntity)?.let { blockEntity ->
            val coverState = (blockEntity.inventory[1].item as? BlockItem)?.block?.defaultState ?: return@let
            val model = MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(coverState)
            if(model == this) {
                return@let
            }else if(model is FabricBakedModel) {
                model.emitBlockQuads(world, state, pos, randomSupplier, context)
            }
            model.emitFromVanilla(state, context, randomSupplier) { quad -> !quad.hasColor() }

            context.pushTransform { q ->
                val rawColor = ColorProviderRegistry.BLOCK[coverState.block]!!.getColor(coverState, world, pos, 0)
                val color = 255 shl 24 or rawColor
                q.color(color, color, color, color)
                true
            }

            model.emitFromVanilla(state, context, randomSupplier) { quad -> quad.hasColor() }
            context.popTransform()
            return
        }

        when(state[Properties.FACING]) {
            Direction.NORTH -> modelList[0].emitBlockQuads(world, state, pos, randomSupplier, context)
            Direction.SOUTH -> modelList[1].emitBlockQuads(world, state, pos, randomSupplier, context)
            Direction.WEST -> modelList[2].emitBlockQuads(world, state, pos, randomSupplier, context)
            Direction.EAST -> modelList[3].emitBlockQuads(world, state, pos, randomSupplier, context)
            Direction.UP -> modelList[4].emitBlockQuads(world, state, pos, randomSupplier, context)
            Direction.DOWN -> modelList[5].emitBlockQuads(world, state, pos, randomSupplier, context)
            else -> {}
        }
    }

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>, context: RenderContext) {
        modelList[4].emitItemQuads(stack, randomSupplier, context)
    }

    private fun BakedModel.emitFromVanilla(state: BlockState, context: RenderContext, randSupplier: Supplier<Random>, shouldEmit: (BakedQuad) -> Boolean) {
        val emitter = context.emitter
        Direction.entries.forEach { dir ->
            getQuads(state, dir, randSupplier.get()).forEach { quad ->
                if (shouldEmit(quad)) {
                    emitter.fromVanilla(quad.vertexData, 0, false)
                    emitter.emit()
                }
            }
        }
        getQuads(state, null, randSupplier.get()).forEach { quad ->
            if (shouldEmit(quad)) {
                emitter.fromVanilla(quad.vertexData, 0, false)
                emitter.emit()
            }
        }
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun useAmbientOcclusion() = true
    override fun hasDepth() = false
    override fun isSideLit() = true
    override fun isBuiltin() = false

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY
    override fun getTransformation() = modelTransformation

}