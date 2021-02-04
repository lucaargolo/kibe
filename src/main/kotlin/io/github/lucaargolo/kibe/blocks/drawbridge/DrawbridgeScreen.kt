package io.github.lucaargolo.kibe.blocks.drawbridge

import io.github.lucaargolo.kibe.blocks.DRAWBRIDGE
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

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        drawCenteredString(matrices, textRenderer, title.string, backgroundWidth / 2, 6, 0xFFE000)
        textRenderer.draw(matrices, playerInventory.displayName, 8f, backgroundHeight - 96 + 4f, 0xFFE000)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, x, y, 0, 0, 176, 131)
        if(handler.inventory.getStack(1)?.isEmpty == true)
            itemRenderer.renderInGuiWithOverrides(ItemStack(DRAWBRIDGE.asItem()), x+134, y+18)
    }
}