package io.github.lucaargolo.kibe.items.cooler

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class CoolerBlockItemScreen(handler: CoolerBlockItemScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<CoolerBlockItemScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/cooler.png")

    override fun init() {
        super.init()
        backgroundHeight = 131
        x = width/2-backgroundWidth/2
        y = height/2-backgroundHeight/2
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.drawCenteredTextWithShadow(textRenderer, title.string,backgroundWidth/2, 6, 0xFFFFFF)
        context.drawText(textRenderer, playerInventoryTitle, 8, backgroundHeight - 96 + 4, 4210752, false)
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.drawTexture(texture, x, y, 0, 0, 176, 131)
    }

}