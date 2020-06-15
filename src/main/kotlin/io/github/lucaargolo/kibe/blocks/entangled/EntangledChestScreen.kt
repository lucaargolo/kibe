package io.github.lucaargolo.kibe.blocks.entangled

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.ContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class EntangledChestScreen(container: EntangledChestContainer, inventory: PlayerInventory, title: Text): ContainerScreen<EntangledChestContainer>(container, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/entangled_chest.png")

    private var startX = 0
    private var startY = 0

    override fun init() {
        super.init()
        startX = width/2-containerWidth/2
        startY = height/2-containerHeight/2
    }

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground()
        drawRunes()
        super.render(mouseX, mouseY, delta)
        drawMouseoverTooltip(mouseX, mouseY)
    }

    private fun drawRunes() {
        container.entity.runeColors.forEach { (n, color) ->
            itemRenderer.renderGuiItemIcon(ItemStack(Rune.getRuneByColor(color)), startX+87+(n-1)*10, startY+2)
        }
    }

    override fun drawForeground(mouseX: Int, mouseY: Int) {
        font.draw(title.string, 8.0f, 6.0f, 0xFFFFFF)
        font.draw(playerInventory.displayName.string, 8.0f, (containerHeight - 96 + 4).toFloat(), 0xFFFFFF)
    }

    override fun drawBackground(delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft!!.textureManager.bindTexture(texture)
        blit(startX, startY, 0, 0, 176, 166)
    }

}