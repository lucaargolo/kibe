package io.github.lucaargolo.kibe.client.screen

import io.github.lucaargolo.kibe.menu.EntangledChestScreenHandler
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class EntangledChestScreen(screenHandler: EntangledChestScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<EntangledChestScreenHandler>(screenHandler, inventory, title) {

    private val texture = Identifier.of("kibe:textures/gui/entangled_chest.png")

    private var startX = 0
    private var startY = 0

    override fun init() {
        super.init()
        startX = width/2-backgroundWidth/2
        startY = height/2-backgroundHeight/2
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        drawRunes(context)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    private fun drawRunes(context: DrawContext) {
        handler.entity.runeColors.forEachIndexed { idx, col ->
            context.drawTexture(texture, startX + 91 + idx*10, startY + 5, col.id*8, 167, 8, 10)
        }
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.drawText(textRenderer, title, 8, 6, 0xFFFFFF, false)
        context.drawText(textRenderer, playerInventoryTitle, 8, backgroundHeight - 96 + 4, 0xFFFFFF, false)
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.drawTexture(texture, startX, startY, 0, 0, 176, 166)
    }

}