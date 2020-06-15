package io.github.lucaargolo.kibe.blocks.trashcan

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.ingame.ContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class TrashCanScreen(container: TrashCanContainer, inventory: PlayerInventory, title: Text): ContainerScreen<TrashCanContainer>(container, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/trash_can.png")

    private var startX = 0
    private var startY = 0

    override fun init() {
        super.init()
        startX = width/2-containerWidth/2
        startY = height/2-containerHeight/2
    }

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground()
        super.render(mouseX, mouseY, delta)
        drawMouseoverTooltip(mouseX, mouseY)
    }

    override fun drawForeground(mouseX: Int, mouseY: Int) {
        drawCenteredString(font, title.string,containerWidth/2, 6, 0xFFFFFF)
        font.draw(playerInventory.displayName.string, 8f, containerHeight - 96 + 4f, 0xFFFFFF)
    }

    override fun drawBackground(delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft!!.textureManager.bindTexture(texture)
        blit(startX, startY, 0, 0, 176, 166)
    }

}
