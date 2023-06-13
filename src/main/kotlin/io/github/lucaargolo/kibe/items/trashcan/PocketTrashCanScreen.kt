package io.github.lucaargolo.kibe.items.trashcan

import com.mojang.blaze3d.systems.RenderSystem
import joptsimple.internal.Strings
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class PocketTrashCanScreen(screenHandler: PocketTrashCanScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<PocketTrashCanScreenHandler>(screenHandler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/trash_can.png")

    private var startX = 0
    private var startY = 0

    override fun init() {
        super.init()
        startX = width/2-backgroundWidth/2
        startY = height/2-backgroundHeight/2
    }


    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.drawTexture(texture, startX, startY, 0, 0, 176, 166)
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        val stringArray = title.string.split(" ").toMutableList()
        context.drawCenteredTextWithShadow(textRenderer, stringArray[0],backgroundWidth/2, 6, 0xFFFFFF)
        stringArray.removeAt(0)
        context.drawCenteredTextWithShadow(textRenderer, Strings.join(stringArray, " "),backgroundWidth/2, 17, 0xFFFFFF)
        context.drawText(textRenderer, playerInventoryTitle, 8, backgroundHeight - 96 + 4, 0xFFFFFF, false)
    }

}