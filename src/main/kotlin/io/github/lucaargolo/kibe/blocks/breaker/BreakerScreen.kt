package io.github.lucaargolo.kibe.blocks.breaker

import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class BreakerScreen(handler: BreakerScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<BreakerScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/breaker.png")

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        textRenderer.draw(matrices, title, ((backgroundWidth / 2) - (textRenderer.getWidth(title)/2)).toFloat(), 6f, 4210752)
        textRenderer.draw(matrices, playerInventory.displayName, 8f, backgroundHeight - 96 + 4f, 4210752)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        client?.textureManager?.bindTexture(texture)
        drawTexture(matrices, x, y, 0, 0, 176, 168)
    }
}