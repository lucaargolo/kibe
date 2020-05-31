package io.github.lucaargolo.kibe.blocks.vacuum

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import io.github.lucaargolo.kibe.fluids.getFluidStill
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.awt.Color

class VacuumHopperScreen(container: VacuumHopperContainer, inventory: PlayerInventory, title: Text): HandledScreen<VacuumHopperContainer>(container, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/vacuum_hopper.png")

    private var startX = 0
    private var startY = 0

    override fun init() {
        super.init()
        startX = width/2-backgroundWidth/2
        startY = height/2-backgroundHeight/2
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
        if(mouseX in (startX+100..startX+112) && mouseY in (startY+18..startY+70)) {
            renderTooltip(matrices, listOf(LiteralText("Liquid Xp"), LiteralText("${handler.entity.liquidXp} / 16000 mB")), mouseX, mouseY)
        }
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        drawCenteredString(matrices, textRenderer, title.asString(),backgroundWidth/2, 6, 0xFFFFFF)
        textRenderer.draw(matrices, playerInventory.displayName, 8f, backgroundHeight - 96 + 4f, 0xFFFFFF)
    }

    override fun drawMouseoverTooltip(matrices: MatrixStack?, x: Int, y: Int) {
        super.drawMouseoverTooltip(matrices, x, y)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, startX, startY, 0, 0, 176, 166)

        val fluid = getFluidStill(LIQUID_XP)!!
        val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid)
        val fluidColor = fluidRenderHandler.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player!!.blockPos, fluid.defaultState)
        val sprite = fluidRenderHandler.getFluidSprites(MinecraftClient.getInstance().world, BlockPos.ORIGIN, fluid.defaultState)[0]
        val color  = Color((fluidColor shr 16 and 255), (fluidColor shr 8 and 255), (fluidColor and 255))
        MinecraftClient.getInstance().textureManager.bindTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
        val tess = Tessellator.getInstance()
        val bb = tess.buffer
        val matrix = matrices.peek().model;

        var percentage = (handler.entity.liquidXp/16000f)*52f
        (0..(percentage/16).toInt()).forEach { index ->
            val p = if(percentage > 16f) 16f else percentage
            bb.begin(7, VertexFormats.POSITION_COLOR_TEXTURE)
            bb.vertex(matrix, startX+100f, startY+70f-(index*16f), 0f).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(sprite.maxU, sprite.minV).next()
            bb.vertex(matrix, startX+112f, startY+70f-(index*16f), 0f).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(sprite.minU, sprite.minV).next()
            bb.vertex(matrix, startX+112f, startY+70f-p-(index*16f), 0f).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(sprite.minU, (sprite.maxV-((16f-p)/1024f)).toFloat()).next()
            bb.vertex(matrix, startX+100f, startY+70f-p-(index*16f), 0f).color(color.red/255f, color.green/255f, color.blue/255f, 1f).texture(sprite.maxU, (sprite.maxV-((16f-p)/1024f)).toFloat()).next()
            tess.draw()
            percentage -= p
        }

        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, startX+100, startY+18, 172, 0, 12, 52)

    }

}
