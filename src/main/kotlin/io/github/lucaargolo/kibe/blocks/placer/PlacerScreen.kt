package io.github.lucaargolo.kibe.blocks.placer

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class PlacerScreen(handler: PlacerScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<PlacerScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/placer.png")

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        textRenderer.draw(matrices, title, ((backgroundWidth / 2) - (textRenderer.getWidth(title)/2)).toFloat(), 6f, 4210752)
        textRenderer.draw(matrices, playerInventoryTitle, 8f, backgroundHeight - 96 + 4f, 4210752)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShaderTexture(0, texture)
        drawTexture(matrices, x, y, 0, 0, 176, 168)
    }
}