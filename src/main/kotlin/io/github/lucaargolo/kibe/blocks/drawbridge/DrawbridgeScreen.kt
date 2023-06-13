package io.github.lucaargolo.kibe.blocks.drawbridge

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.kibe.blocks.DRAWBRIDGE
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class DrawbridgeScreen(handler: DrawbridgeScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<DrawbridgeScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/drawbridge.png")

    override fun init() {
        super.init()
        backgroundHeight = 131
        x = width / 2 - backgroundWidth / 2
        y = height / 2 - backgroundHeight / 2
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.drawCenteredTextWithShadow(textRenderer, title.string, backgroundWidth / 2, 6, 0xFFE000)
        context.drawText(textRenderer, playerInventoryTitle, 8, backgroundHeight - 96 + 4, 0xFFE000, false)
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.drawTexture(texture, x, y, 0, 0, 176, 131)
        if(handler.inventory.getStack(1)?.isEmpty == true)
            context.drawItem(ItemStack(DRAWBRIDGE.asItem()), x+134, y+18)
    }
}