package io.github.lucaargolo.kibe.blocks.trashcan

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class TrashCanScreen(screenHandler: TrashCanScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<TrashCanScreenHandler>(screenHandler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/trash_can.png")

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
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        drawCenteredString(matrices, textRenderer, title.string,backgroundWidth/2, 6, 0xFFFFFF)
        textRenderer.draw(matrices, playerInventory.displayName, 8f, backgroundHeight - 96 + 4f, 0xFFFFFF)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, startX, startY, 0, 0, 176, 166)
    }

}
