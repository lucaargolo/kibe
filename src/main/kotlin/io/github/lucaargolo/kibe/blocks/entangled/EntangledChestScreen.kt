package io.github.lucaargolo.kibe.blocks.entangled

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class EntangledChestScreen(container: EntangledChestContainer, inventory: PlayerInventory, title: Text): HandledScreen<EntangledChestContainer>(container, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/entangled_chest.png")

    private var startX = 0
    private var startY = 0

    override fun init() {
        super.init()
        startX = width/2-backgroundWidth/2
        startY = height/2-backgroundHeight/2
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        drawRunes()
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    private fun drawRunes() {
        handler.entity.runeColors.forEach { (n, color) ->
            itemRenderer.renderGuiItem(ItemStack(Rune.getRuneByColor(color)), startX+87+(n-1)*10, startY+2)
        }
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        textRenderer.draw(matrices, title, 8.0f, 6.0f, 0xFFFFFF)
        textRenderer.draw(matrices, playerInventory.displayName, 8.0f, (backgroundHeight - 96 + 4).toFloat(), 0xFFFFFF)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, startX, startY, 0, 0, 176, 166)
    }

}