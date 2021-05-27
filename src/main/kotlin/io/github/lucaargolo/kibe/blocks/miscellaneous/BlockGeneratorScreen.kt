package io.github.lucaargolo.kibe.blocks.miscellaneous

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
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
import net.minecraft.util.registry.Registry
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

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        textRenderer.draw(matrices, title, 8f, 6f, 4210752)
        textRenderer.draw(matrices, playerInventory.displayName, 8f, backgroundHeight - 96 + 4f, 4210752)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        client?.textureManager?.bindTexture(texture)
        drawTexture(matrices, x, y, 0, 0, 176, 186)
        //Draw fluids
        client?.textureManager?.bindTexture(atlas)
        if(handler.entity.block == Blocks.BASALT) {
            drawBlockBar(matrices, delta, Blocks.BLUE_ICE, true)
        }else{
            drawFluidBar(matrices, delta, Fluids.WATER, true)
        }
        drawFluidBar(matrices, delta, Fluids.LAVA, false)
        itemRenderer.renderInGuiWithOverrides(ItemStack(handler.entity.block), x+80, y+18)
    }

    private fun drawBlockBar(matrices: MatrixStack, delta: Float, block: Block, left: Boolean) {
        val blockId = Registry.BLOCK.getId(block)
        val supposedBlockSpriteId = SpriteIdentifier(atlas, Identifier(blockId.namespace, "block/${blockId.path}"))
        val sprite = supposedBlockSpriteId.sprite
        drawBar(matrices, delta, sprite, -1, left)
    }

    private fun drawFluidBar(matrices: MatrixStack, delta: Float, fluid: Fluid, left: Boolean) {
        val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid) ?: return
        val sprite = fluidRenderHandler.getFluidSprites(handler.entity.world, handler.entity.pos, fluid.defaultState)[0]
        val color = fluidRenderHandler.getFluidColor(handler.entity.world, handler.entity.pos, fluid.defaultState)
        drawBar(matrices, delta, sprite, color, left)
    }

    private fun drawBar(matrices: MatrixStack, delta: Float, sprite: Sprite, color: Int, left: Boolean) {
        var off = 0
        handler.entity.lastRenderProgress = MathHelper.lerp(delta, handler.entity.lastRenderProgress, handler.entity.renderProgress)
        var bar = handler.entity.lastRenderProgress*70f % 70f

        while(bar > sprite.width) {
            if(left) {
                drawColoredTextureQuad(matrices, x+8f+off, x+8f+off+16f, y+19f, y+19f+14f, sprite.minU, sprite.maxU, sprite.minV, sprite.maxV-2f/atlasHeight, color)
            }else{
                drawColoredTextureQuad(matrices, x+168f-off-16f, x+168f-off, y+19f, y+19f+14f, sprite.minU, sprite.maxU, sprite.minV, sprite.maxV-2f/atlasHeight, color)
            }
            off += 16
            bar -= 16
        }
        if(left) {
            drawColoredTextureQuad(matrices, x+8f+off, x+8f+off+bar, y+19f, y+19f+14f, sprite.minU, sprite.maxU-(16f-bar)/atlasWidth, sprite.minV, sprite.maxV-2f/atlasHeight, color)
        }else{
            drawColoredTextureQuad(matrices, x+168f-off-bar, x+168f-off, y+19f, y+19f+14f, sprite.minU+(16f-bar)/atlasWidth, sprite.maxU, sprite.minV, sprite.maxV-2f/atlasHeight, color)
        }
    }

    private fun drawColoredTextureQuad(matrices: MatrixStack, x0: Float, x1: Float, y0: Float, y1: Float, u0: Float, u1: Float, v0: Float, v1: Float, color: Int) {
        val z = zOffset.toFloat()
        val r = (color shr 16 and 255)/255f
        val g = (color shr 8 and 255)/255f
        val b = (color and 255)/255f
        val matrix = matrices.peek().model
        val bufferBuilder = Tessellator.getInstance().buffer
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE)
        bufferBuilder.vertex(matrix, x0, y1, z).color(r, g, b, 1.0f).texture(u0, v1).next()
        bufferBuilder.vertex(matrix, x1, y1, z).color(r, g, b, 1.0f).texture(u1, v1).next()
        bufferBuilder.vertex(matrix, x1, y0, z).color(r, g, b, 1.0f).texture(u1, v0).next()
        bufferBuilder.vertex(matrix, x0, y0, z).color(r, g, b, 1.0f).texture(u0, v0).next()
        bufferBuilder.end()
        BufferRenderer.draw(bufferBuilder)
    }

}