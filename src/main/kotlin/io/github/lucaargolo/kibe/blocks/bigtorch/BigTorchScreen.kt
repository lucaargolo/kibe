package io.github.lucaargolo.kibe.blocks.bigtorch

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

class BigTorchScreen(handler: BigTorchScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<BigTorchScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/big_torch.png")

    override fun init() {
        super.init()
        backgroundHeight = 150
        x = width/2-backgroundWidth/2
        y = height/2-backgroundHeight/2
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
        if(mouseX in (x+8..x+168) && mouseY in (y+19..y+33)) {
            val list = listOf(
                TranslatableText("tooltip.kibe.torch_percentage").append(LiteralText("${(handler.entity.torchPercentage*100).toInt()}%").formatted(Formatting.GRAY)),
                TranslatableText("tooltip.kibe.chunk_radius").append(LiteralText("${handler.entity.chunkRadius}").formatted(Formatting.GRAY))
                //TranslatableText("tooltip.kibe.suppressed_spawns").append(LiteralText("${handler.entity.suppressedSpawns}").formatted(Formatting.GRAY))
            )
            renderTooltip(matrices, list, mouseX, mouseY)
        }
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        textRenderer.draw(matrices, title, 8f, 6f, 4210752)
        textRenderer.draw(matrices, playerInventoryTitle, 8f, backgroundHeight - 96 + 4f, 4210752)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShaderTexture(0, texture)
        drawTexture(matrices, x, y, 0, 0, 176, 150)
        drawTexture(matrices, x+8, y+19, 0, 150, (159*handler.entity.torchPercentage).toInt(), 14)
    }

}