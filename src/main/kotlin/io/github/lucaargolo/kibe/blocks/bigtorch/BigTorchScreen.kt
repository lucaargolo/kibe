package io.github.lucaargolo.kibe.blocks.bigtorch

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory

import net.minecraft.text.Text

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

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        drawMouseoverTooltip(context, mouseX, mouseY)
        if(mouseX in (x+8..x+168) && mouseY in (y+19..y+33)) {
            val list = listOf(
                Text.translatable("tooltip.kibe.torch_percentage").append(Text.literal("${(handler.entity.torchPercentage*100).toInt()}%").formatted(Formatting.GRAY)),
                Text.translatable("tooltip.kibe.chunk_radius").append(Text.literal("${handler.entity.chunkRadius}").formatted(Formatting.GRAY))
                //Text.translatable("tooltip.kibe.suppressed_spawns").append(Text.literal("${handler.entity.suppressedSpawns}").formatted(Formatting.GRAY))
            )
            context.drawTooltip(textRenderer, list, mouseX, mouseY)
        }
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.drawText(textRenderer, title, 8, 6, 4210752, false)
        context.drawText(textRenderer, playerInventoryTitle, 8, backgroundHeight - 96 + 4, 4210752, false)
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShaderTexture(0, texture)
        context.drawTexture(texture, x, y, 0, 0, 176, 150)
        context.drawTexture(texture, x+8, y+19, 0, 150, (159*handler.entity.torchPercentage).toInt(), 14)
    }

}