package io.github.lucaargolo.kibe.blocks.tank

import com.mojang.datafixers.util.Pair
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.blocks.TANK
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.*
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import java.awt.Color
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class TankCustomModel: UnbakedModel, BakedModel, FabricBakedModel {

    private val spriteIdList = mutableListOf(
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier(MOD_ID, "block/tank"))
    )
    private val spriteList = mutableListOf<Sprite>()

    override fun getModelDependencies(): Collection<Identifier> = listOf()

    override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>, unresolvedTextureReferences: MutableSet<Pair<String, String>>) = spriteIdList

    override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings, modelId: Identifier): BakedModel? {
        spriteIdList.forEach { spriteIdentifier ->
            spriteList.add(textureGetter.apply(spriteIdentifier))
        }
        return this
    }

    override fun getSprite() = spriteList[0]

    override fun isVanillaAdapter() = false

    override fun emitBlockQuads(world: BlockRenderView?, state: BlockState?, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        val color = Color(255, 255, 255, 255).rgb

        context.pushTransform { quad ->
            quad.spriteColor(0, color, color, color, color)
            true
        }

        val emitter = context.emitter

        Direction.values().forEach {
            if(world?.getBlockState(pos.add(it.vector))?.block != TANK) emitter.drawSide(it, world, pos)
        }

        context.popTransform()
    }

    private fun Direction.getLeft(): Direction {
        return when(this) {
            Direction.NORTH -> Direction.EAST
            Direction.SOUTH -> Direction.WEST
            Direction.EAST -> Direction.NORTH
            Direction.WEST -> Direction.SOUTH
            Direction.UP, Direction.DOWN -> Direction.EAST
        }
    }

    private fun Direction.getRight(): Direction {
        return when(this) {
            Direction.NORTH -> Direction.WEST
            Direction.SOUTH -> Direction.EAST
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
        val bl1 = world?.getBlockState(pos.add(side.getUp().vector))?.block != TANK
        val bl2 = world?.getBlockState(pos.add(side.getDown().vector))?.block != TANK
        val bl3 = world?.getBlockState(pos.add(side.getLeft().vector))?.block != TANK
        val bl4 = world?.getBlockState(pos.add(side.getRight().vector))?.block != TANK

        val bl5 = world?.getBlockState(pos.add(side.getUp().vector).add(side.getLeft().vector))?.block != TANK
        val bl6 = world?.getBlockState(pos.add(side.getUp().vector).add(side.getRight().vector))?.block != TANK
        val bl7 = world?.getBlockState(pos.add(side.getDown().vector).add(side.getLeft().vector))?.block != TANK
        val bl8 = world?.getBlockState(pos.add(side.getDown().vector).add(side.getRight().vector))?.block != TANK

        if(bl1) draw(side, 15/16f, 1f, 1/16f, 15/16f, 0f) //UP
        if(bl2) draw(side, 15/16f, 1/16f, 1/16f, 0f, 0f) //DOWN
        if(bl3) draw(side, 1f, 15/16f, 15/16f, 1/16f, 0f) //LEFT
        if(bl4) draw(side, 1/16f, 15/16f, 0f, 1/16f, 0f) //RIGHT

        if(bl1 || bl3 || bl5) draw(side, 1f, 1f, 15/16f, 15/16f, 0f) //UP_LEFT
        if(bl1 || bl4 || bl6) draw(side, 1/16f, 1f, 0f, 15/16f, 0f) //UP_RIGHT

        if(bl2 || bl3 || bl7) draw(side, 1f, 1/16f, 15/16f, 0f, 0f) //DOWN_LEFT
        if(bl2 || bl4 || bl8) draw(side, 1/16f, 1/16f, 0f, 0f, 0f) //DOWN_RIGHT

    }

    private fun QuadEmitter.draw(side: Direction, left: Float, bottom: Float, right: Float, top: Float, depth: Float) {
        square(side, left, bottom, right, top, depth)
        spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
        spriteColor(0, -1, -1, -1, -1)
        emit()
    }

    override fun emitItemQuads(p0: ItemStack?, p1: Supplier<Random>?, p2: RenderContext?) {}

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun useAmbientOcclusion() = false
    override fun hasDepth() = false
    override fun isSideLit() = false
    override fun isBuiltin() = false

    override fun getOverrides() = null
    override fun getTransformation() = null

}