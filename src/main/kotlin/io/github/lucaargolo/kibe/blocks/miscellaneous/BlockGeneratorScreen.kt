package io.github.lucaargolo.kibe.blocks.miscellaneous

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.*
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.registry.Registry
import net.minecraft.registry.Registries
import org.lwjgl.opengl.GL11

class BlockGeneratorScreen(handler: BlockGeneratorScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<BlockGeneratorScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/block_generator.png")

    private val atlas = PlayerScreenHandler.BLOCK_ATLAS_TEXTURE
    private var atlasWidth = 0f
    private var atlasHeight = 0f

    override fun init() {
        super.init()
        backgroundHeight = 186
        x = width/2-backgroundWidth/2
        y = height/2-backgroundHeight/2
        client?.textureManager?.bindTexture(atlas)
        atlasWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH).toFloat()
        atlasHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT).toFloat()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.drawText(textRenderer, title, 8, 6, 4210752, false)
        context.drawText(textRenderer, playerInventoryTitle, 8, backgroundHeight - 96 + 4, 4210752, false)
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.drawTexture(texture, x, y, 0, 0, 176, 186)
        context.drawItem(ItemStack(handler.entity.block), x+80, y+18)
        //Draw fluids
        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram)
        RenderSystem.setShaderTexture(0, atlas)
        if(handler.entity.block == Blocks.BASALT) {
            drawBlockBar(context, delta, Blocks.BLUE_ICE, true)
        }else{
            drawFluidBar(context, delta, Fluids.WATER, true)
        }
        drawFluidBar(context, delta, Fluids.LAVA, false)
    }

    private fun drawBlockBar(context: DrawContext, delta: Float, block: Block, left: Boolean) {
        val blockId = Registries.BLOCK.getId(block)
        val supposedBlockSpriteId = SpriteIdentifier(atlas, Identifier(blockId.namespace, "block/${blockId.path}"))
        val sprite = supposedBlockSpriteId.sprite
        drawBar(context, delta, sprite, -1, left)
    }

    private fun drawFluidBar(context: DrawContext, delta: Float, fluid: Fluid, left: Boolean) {
        val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid) ?: return
        val sprite = fluidRenderHandler.getFluidSprites(handler.entity.world, handler.entity.pos, fluid.defaultState)[0]
        val color = fluidRenderHandler.getFluidColor(handler.entity.world, handler.entity.pos, fluid.defaultState)
        drawBar(context, delta, sprite, color, left)
    }

    private fun drawBar(context: DrawContext, delta: Float, sprite: Sprite, color: Int, left: Boolean) {
        var off = 0
        handler.entity.lastRenderProgress = MathHelper.lerp(delta, handler.entity.lastRenderProgress, handler.entity.renderProgress)
        var bar = handler.entity.lastRenderProgress*70f % 70f

        while(bar > sprite.contents.width) {
            if(left) {
                drawColoredTextureQuad(context, x+8f+off, x+8f+off+16f, y+19f, y+19f+14f, sprite.minU, sprite.maxU, sprite.minV, sprite.maxV-2f/atlasHeight, color)
            }else{
                drawColoredTextureQuad(context, x+168f-off-16f, x+168f-off, y+19f, y+19f+14f, sprite.minU, sprite.maxU, sprite.minV, sprite.maxV-2f/atlasHeight, color)
            }
            off += 16
            bar -= 16
        }
        if(left) {
            drawColoredTextureQuad(context, x+8f+off, x+8f+off+bar, y+19f, y+19f+14f, sprite.minU, sprite.maxU-(16f-bar)/atlasWidth, sprite.minV, sprite.maxV-2f/atlasHeight, color)
        }else{
            drawColoredTextureQuad(context, x+168f-off-bar, x+168f-off, y+19f, y+19f+14f, sprite.minU+(16f-bar)/atlasWidth, sprite.maxU, sprite.minV, sprite.maxV-2f/atlasHeight, color)
        }
    }

    private fun drawColoredTextureQuad(context: DrawContext, x0: Float, x1: Float, y0: Float, y1: Float, u0: Float, u1: Float, v0: Float, v1: Float, color: Int) {
        val z = 0f
        val r = (color shr 16 and 255)/255f
        val g = (color shr 8 and 255)/255f
        val b = (color and 255)/255f

        val matrix = context.matrices.peek().positionMatrix
        val bufferBuilder = Tessellator.getInstance().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)
        bufferBuilder.vertex(matrix, x0, y1, z).color(r, g, b, 1.0f).texture(u0, v1).next()
        bufferBuilder.vertex(matrix, x1, y1, z).color(r, g, b, 1.0f).texture(u1, v1).next()
        bufferBuilder.vertex(matrix, x1, y0, z).color(r, g, b, 1.0f).texture(u1, v0).next()
        bufferBuilder.vertex(matrix, x0, y0, z).color(r, g, b, 1.0f).texture(u0, v0).next()
        BufferRenderer.draw(bufferBuilder.end())
    }

}