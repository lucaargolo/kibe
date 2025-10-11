package io.github.lucaargolo.kibe.client.model

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.item.TankBlockItem
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.awt.Color
import java.util.function.Function
import java.util.function.Supplier

class TankCustomModel: UnbakedModel, BakedModel, FabricBakedModel {

    private val spriteIdList = mutableListOf(
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier.of("block/tank"))
    )
    val spriteList = mutableListOf<Sprite>()

    private val modelIdList = mutableListOf(
        Identifier.of("block/stone")
    )

    override fun getModelDependencies(): Collection<Identifier> = listOf()

    override fun setParents(modelLoader: Function<Identifier, UnbakedModel>?) { }

    lateinit var modelTransformation: ModelTransformation

    override fun bake(baker: Baker, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings): BakedModel {
        val model = baker.getOrLoadModel(modelIdList[0])
        val baked = model.bake(baker, textureGetter, ModelRotation.X0_Y0)!!

        modelTransformation = baked.transformation

        spriteIdList.forEach { spriteIdentifier ->
            spriteList.add(textureGetter.apply(spriteIdentifier))
        }
        return this
    }

    override fun getParticleSprite() = spriteList[0]

    override fun isVanillaAdapter() = false

    override fun emitBlockQuads(world: BlockRenderView?, state: BlockState?, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        val color = Color(255, 255, 255, 255).rgb

        context.pushTransform { quad ->
            quad.color(color, color, color, color)
            true
        }

        val emitter = context.emitter

        Direction.entries.forEach {
            if(world?.getBlockState(pos.add(it.vector))?.block != BlockCompendium.TANK) emitter.drawSide(it, world, pos)
        }

        context.popTransform()
    }

    private fun Direction.getLeft(): Direction {
        return when(this) {
            Direction.NORTH -> Direction.WEST
            Direction.SOUTH -> Direction.EAST
            Direction.EAST -> Direction.NORTH
            Direction.WEST -> Direction.SOUTH
            Direction.UP, Direction.DOWN -> Direction.EAST
        }
    }

    private fun Direction.getRight(): Direction {
        return when(this) {
            Direction.NORTH -> Direction.EAST
            Direction.SOUTH -> Direction.WEST
            Direction.EAST -> Direction.SOUTH
            Direction.WEST -> Direction.NORTH
            Direction.UP, Direction.DOWN -> Direction.WEST
        }
    }

    private fun Direction.getUp(): Direction {
        return when(this) {
            Direction.UP -> Direction.NORTH
            Direction.DOWN -> Direction.SOUTH
            else -> Direction.UP
        }
    }

    private fun Direction.getDown(): Direction {
        return when(this) {
            Direction.UP -> Direction.SOUTH
            Direction.DOWN -> Direction.NORTH
            else -> Direction.DOWN
        }
    }

    private fun QuadEmitter.drawSide(side: Direction, world: BlockRenderView?, pos: BlockPos) {
        val bl1 = world?.getBlockState(pos.add(side.getUp().vector))?.block != BlockCompendium.TANK
        val bl2 = world?.getBlockState(pos.add(side.getDown().vector))?.block != BlockCompendium.TANK
        val bl3 = world?.getBlockState(pos.add(side.getLeft().vector))?.block != BlockCompendium.TANK
        val bl4 = world?.getBlockState(pos.add(side.getRight().vector))?.block != BlockCompendium.TANK

        val bl5 = world?.getBlockState(pos.add(side.getUp().vector).add(side.getLeft().vector))?.block != BlockCompendium.TANK
        val bl6 = world?.getBlockState(pos.add(side.getUp().vector).add(side.getRight().vector))?.block != BlockCompendium.TANK
        val bl7 = world?.getBlockState(pos.add(side.getDown().vector).add(side.getLeft().vector))?.block != BlockCompendium.TANK
        val bl8 = world?.getBlockState(pos.add(side.getDown().vector).add(side.getRight().vector))?.block != BlockCompendium.TANK

        if(bl1) draw(side, 15/16f, 1f, 1/16f, 15/16f, 0f) //UP
        if(bl2) draw(side, 15/16f, 1/16f, 1/16f, 0f, 0f) //DOWN
        if(bl3) draw(side, 1f, 15/16f, 15/16f, 1/16f, 0f) //LEFT
        if(bl4) draw(side, 1/16f, 15/16f, 0f, 1/16f, 0f) //RIGHT

        if(bl1 || bl3 || bl5) draw(side, 1f, 1f, 15/16f, 15/16f, 0f) //UP_LEFT
        if(bl1 || bl4 || bl6) draw(side, 1/16f, 1f, 0f, 15/16f, 0f) //UP_RIGHT

        if(bl2 || bl3 || bl7) draw(side, 1f, 1/16f, 15/16f, 0f, 0f) //DOWN_LEFT
        if(bl2 || bl4 || bl8) draw(side, 1/16f, 1/16f, 0f, 0f, 0f) //DOWN_RIGHT

    }

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {
        val client = MinecraftClient.getInstance()
        this.emitBlockQuads(null, null, BlockPos.ORIGIN, randSupplier, context)

        val fluidTank = TankBlockItem.getFluidTank(stack)
        val player = client.player
        val world = player?.world
        val pos = player?.blockPos

        val fluid = fluidTank.resource.fluid
        val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid) ?: return
        val fluidColor = fluidRenderHandler.getFluidColor(world, pos, fluid.defaultState)
        val fluidSprite = fluidRenderHandler.getFluidSprites(world, pos, fluid.defaultState)[0]
        val color = Color((fluidColor shr 16 and 255), (fluidColor shr 8 and 255), (fluidColor and 255)).rgb

        context.pushTransform { quad ->
            quad.color(color, color, color, color)
            true
        }

        val emitter = context.emitter

        val p = fluidTank.amount/(FluidConstants.BUCKET*16f)
        emitter.draw(Direction.UP, fluidSprite, 0f, 0f, 1f, 1f, (1f-p)+0.001f )
        emitter.draw(Direction.DOWN, fluidSprite, 0f, 0f, 1f, 1f, 0.001f)
        emitter.draw(Direction.NORTH, fluidSprite, 0f, 0f, 1f, p, 0.001f)
        emitter.draw(Direction.SOUTH, fluidSprite, 0f, 0f, 1f, p, 0.001f)
        emitter.draw(Direction.EAST, fluidSprite, 0f, 0f, 1f, p, 0.001f)
        emitter.draw(Direction.WEST, fluidSprite, 0f, 0f, 1f, p, 0.001f)

        context.popTransform()
    }

    private fun QuadEmitter.draw(side: Direction, left: Float, bottom: Float, right: Float, top: Float, depth: Float) {
        draw(side, particleSprite, left, bottom, right, top, depth)
    }

    private fun QuadEmitter.draw(side: Direction, sprite: Sprite, left: Float, bottom: Float, right: Float, top: Float, depth: Float) {
        square(side, left, bottom, right, top, depth)
        spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
        color(-1, -1, -1, -1)
        emit()
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun useAmbientOcclusion() = false
    override fun hasDepth() = false
    override fun isSideLit() = false
    override fun isBuiltin() = false

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY
    override fun getTransformation() = modelTransformation

}