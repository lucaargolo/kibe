package io.github.lucaargolo.kibe.items.entangled

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier

class EntangledBagScreen(container: EntangledBagContainer, inventory: PlayerInventory, title: Text): AbstractInventoryScreen<EntangledBagContainer>(container, inventory, title) {

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
        (1..8).forEach {
            val color = DyeColor.byName(container.tag.getString("rune$it"), DyeColor.WHITE)
            itemRenderer.renderGuiItem(ItemStack(Rune.getRuneByColor(color)), startX+87+(it-1)*10, startY+2)
        }
    }

    override fun drawForeground(mouseX: Int, mouseY: Int) {
        font.draw(title.asFormattedString(), 8.0f, 6.0f, 0xFFFFFF)
        font.draw(playerInventory.displayName.asFormattedString(), 8.0f, (containerHeight - 96 + 4).toFloat(), 0xFFFFFF)
    }

    override fun drawBackground(delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft!!.textureManager.bindTexture(texture)
        blit(startX,startY, 0, 0, 176, 166)
    }

    fun hasSameColors(map: MutableMap<Int, DyeColor>): Boolean {
        map.forEach { (key, value) ->
            if(value != DyeColor.byName(container.tag.getString("rune$key"), DyeColor.WHITE)) return false
        }
        return true
    }


}