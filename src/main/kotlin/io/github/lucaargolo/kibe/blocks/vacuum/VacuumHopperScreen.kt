@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.kibe.blocks.vacuum

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.kibe.utils.getMb
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

class VacuumHopperScreen(screenHandler: VacuumHopperScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<VacuumHopperScreenHandler>(screenHandler, inventory, title) {

    private val texture = Identifier("kibe:textures/gui/vacuum_hopper.png")

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
        val p = ((handler.processingTicks/handler.totalProcessingTicks.toFloat())*14).toInt()
        context.drawTexture(texture, startX+120, startY+37, 184, 0, 8, p)
        drawMouseoverTooltip(context, mouseX, mouseY)
        if(mouseX in (startX+100..startX+112) && mouseY in (startY+18..startY+70)) {
            val tank = handler.entity.tank
            val stored = tank.amount
            val capacity = tank.capacity
            context.drawTooltip(textRenderer, listOf(if(tank.isResourceBlank) Text.translatable("tooltip.kibe.empty") else FluidVariantAttributes.getName(tank.variant), Text.literal("${getMb(stored)} / ${capacity/81} mB").formatted(Formatting.GRAY)), mouseX, mouseY)
        }
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.drawCenteredTextWithShadow(textRenderer, title.string,backgroundWidth/2, 6, 0xFFFFFF)
        context.drawText(textRenderer, playerInventoryTitle, 8, backgroundHeight - 96 + 4, 0xFFFFFF, false)
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.drawTexture(texture, startX, startY, 0, 0, 176, 166)

        val tank = handler.entity.tank
        val oldShader = RenderSystem.getShader()

        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram)
        FluidRenderHandlerRegistry.INSTANCE.get(tank.resource.fluid)?.let { fluidRenderHandler ->
            val fluidColor = fluidRenderHandler.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player!!.blockPos, tank.resource.fluid.defaultState)
            val sprite = fluidRenderHandler.getFluidSprites(MinecraftClient.getInstance().world, BlockPos.ORIGIN, tank.resource.fluid.defaultState)[0]
            val r = (fluidColor shr 16 and 255)/255f
            val g = (fluidColor shr 8 and 255)/255f
            val b = (fluidColor and 255)/255f
            val tess = Tessellator.getInstance()
            val bb = tess.buffer
            val matrix = context.matrices.peek().positionMatrix

            var percentage = (tank.amount/tank.capacity.toFloat())*52f

            (0..(percentage/16).toInt()).forEach { index ->
                val p = if(percentage > 16f) 16f else percentage
                bb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)
                bb.vertex(matrix, startX+100f, startY+70f-(index*16f), 0f).color(r, g, b, 1f).texture(sprite.maxU, sprite.minV).next()
                bb.vertex(matrix, startX+112f, startY+70f-(index*16f), 0f).color(r, g, b, 1f).texture(sprite.minU, sprite.minV).next()
                val atlasHeight = sprite.contents.height/(sprite.maxV - sprite.minV)
                bb.vertex(matrix, startX+112f, startY+70f-p-(index*16f), 0f).color(r, g, b, 1f).texture(sprite.minU, (sprite.maxV-((sprite.contents.height-p)/atlasHeight))).next()
                bb.vertex(matrix, startX+100f, startY+70f-p-(index*16f), 0f).color(r, g, b, 1f).texture(sprite.maxU, (sprite.maxV-((sprite.contents.height-p)/atlasHeight))).next()
                tess.draw()
                percentage -= p
            }
        }

        RenderSystem.setShader { oldShader }
        context.drawTexture(texture, startX+100, startY+18, 172, 0, 12, 52)

    }

}
