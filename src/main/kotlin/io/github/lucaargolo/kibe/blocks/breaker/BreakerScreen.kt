package io.github.lucaargolo.kibe.blocks.breaker

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class BreakerScreen(handler: BreakerScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<BreakerScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/breaker.png")

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.drawText(textRenderer, title, (backgroundWidth / 2) - (textRenderer.getWidth(title)/2), 6, 4210752, false)
        context.drawText(textRenderer, playerInventoryTitle, 8, backgroundHeight - 96 + 4, 4210752, false)
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.drawTexture(texture, x, y, 0, 0, 176, 168)
    }
}