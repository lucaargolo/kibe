package io.github.lucaargolo.kibe.blocks.trashcan

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.kibe.blocks.entangled.EntangledChestContainer
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class TrashCanScreen(container: TrashCanContainer, inventory: PlayerInventory, title: Text): AbstractInventoryScreen<TrashCanContainer>(container, inventory, title) {

    private val TEXTURE = Identifier("kibe:textures/gui/trash_can.png")

    var START_X = 0
    var START_Y = 0

    override fun init() {
        super.init()
        START_X = width/2-containerWidth/2
        START_Y = height/2-containerHeight/2
    }

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground()
        super.render(mouseX, mouseY, delta)
        drawMouseoverTooltip(mouseX, mouseY)
    }

    override fun drawForeground(mouseX: Int, mouseY: Int) {
        drawCenteredString(font, title.asFormattedString(),containerWidth/2, 6, 0xFFFFFF)
        drawString(font, playerInventory.displayName.asFormattedString(), 8, containerHeight - 96 + 4, 0xFFFFFF)
    }

    override fun drawBackground(delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft!!.textureManager.bindTexture(TEXTURE)
        blit(START_X,START_Y, 0, 0, 176, 166)
    }

}
