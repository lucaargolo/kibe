package io.github.lucaargolo.kibe.blocks.cooler

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class CoolerScreen(handler: CoolerScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<CoolerScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/cooler.png")

    override fun init() {
        super.init()
        backgroundHeight = 131
        x = width/2-backgroundWidth/2
        y = height/2-backgroundHeight/2
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        drawCenteredText(matrices, textRenderer, title.string,backgroundWidth/2, 6, 0xFFFFFF)
        textRenderer.draw(matrices, playerInventoryTitle, 8f, backgroundHeight - 96 + 4f, 4210752)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShaderTexture(0, texture)
        drawTexture(matrices, x, y, 0, 0, 176, 131)
    }

}