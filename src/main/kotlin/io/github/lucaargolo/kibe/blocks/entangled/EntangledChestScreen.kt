package io.github.lucaargolo.kibe.blocks.entangled

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class EntangledChestScreen(container: EntangledChestContainer, inventory: PlayerInventory, title: Text): AbstractInventoryScreen<EntangledChestContainer>(container, inventory, title) {

    private val TEXTURE = Identifier("kibe:textures/gui/entangled_chest.png")

    var START_X = 0
    var START_Y = 0

    override fun init() {
        super.init()
        START_X = width/2-containerWidth/2
        START_Y = height/2-containerHeight/2
    }

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground()
        drawRunes(mouseX, mouseY, delta)
        super.render(mouseX, mouseY, delta)
        drawMouseoverTooltip(mouseX, mouseY)
    }

    fun drawRunes(mouseX: Int, mouseY: Int, delta: Float) {
        container.entity.runeColors.forEach { n, color ->
            itemRenderer.renderGuiItem(ItemStack(Rune.getRuneByColor(color)), START_X+87+(n-1)*10, START_Y+2)
        }
    }

    override fun drawForeground(mouseX: Int, mouseY: Int) {
        font.draw(title.asFormattedString(), 8.0f, 6.0f, 0)
        font.draw(playerInventory.displayName.asFormattedString(), 8.0f, (containerHeight - 96 + 4).toFloat(), 0)
    }

    override fun drawBackground(delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft!!.textureManager.bindTexture(TEXTURE)
        blit(START_X,START_Y, 0, 0, 176, 166)
    }

}