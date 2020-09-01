package io.github.lucaargolo.kibe.blocks.vacuum

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

class VacuumHopperScreen(screenHandler: VacuumHopperScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<VacuumHopperScreenHandler>(screenHandler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/vacuum_hopper.png")

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
        if(mouseX in (startX+100..startX+112) && mouseY in (startY+18..startY+70)) {
            val tank = handler.entity.tanks.first()
            val stored = tank.volume.amount()
            val capacity = tank.capacity
            renderTooltip(matrices, listOf(tank.volume.name, LiteralText("${stored.asInt(1000)} / ${capacity.asInt(1000)} mB").formatted(Formatting.GRAY)), mouseX, mouseY)
        }
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        drawCenteredString(matrices, textRenderer, title.string,backgroundWidth/2, 6, 0xFFFFFF)
        textRenderer.draw(matrices, playerInventory.displayName, 8f, backgroundHeight - 96 + 4f, 0xFFFFFF)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, startX, startY, 0, 0, 176, 166)

        val tank = handler.entity.tanks.first()
        val percentage = tank.volume.amount().asInt(1000).toDouble()/tank.capacity.asInt(1000).toDouble()
        tank.volume.renderGuiRect(startX+100.0, startY+70.0-(52.0*percentage), startX+112.0, startY+70.0)

        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, startX+100, startY+18, 172, 0, 12, 52)

    }

}
