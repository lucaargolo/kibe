package io.github.lucaargolo.kibe.blocks.vacuum

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import io.github.lucaargolo.kibe.fluids.getFluidStill
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos.getZ
import java.awt.Color
import java.awt.Rectangle


class VacuumHopperScreen(container: VacuumHopperContainer, inventory: PlayerInventory, title: Text): AbstractInventoryScreen<VacuumHopperContainer>(container, inventory, title) {

    private val TEXTURE = Identifier("kibe:textures/gui/vacuum_hopper.png")

    var START_X = 0
    var START_Y = 0

    override fun init() {
        super.init()
        START_X = width/2-containerWidth/2
        START_Y = height/2-containerHeight/2
    }

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground()
        super.render(mouseX, mouseY, delta)
        drawMouseoverTooltip(mouseX, mouseY)
    }

    override fun drawForeground(mouseX: Int, mouseY: Int) {
        drawCenteredString(font, title.asFormattedString(),containerWidth/2, 6, 0xFFFFFF)
        drawString(font, playerInventory.displayName.asFormattedString(), 8, containerHeight - 96 + 4, 0xFFFFFF)
    }

    override fun drawBackground(delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft!!.textureManager.bindTexture(TEXTURE)
        blit(START_X,START_Y, 0, 0, 176, 166)

        val fluid = getFluidStill(LIQUID_XP)!!
        val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid)
        val fluidColor = fluidRenderHandler.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player!!.blockPos, fluid.defaultState)
        val sprite = fluidRenderHandler.getFluidSprites(MinecraftClient.getInstance().world, BlockPos.ORIGIN, fluid.defaultState)[0]
        val color  = Color((fluidColor shr 16 and 255), (fluidColor shr 8 and 255), (fluidColor and 255))
        MinecraftClient.getInstance().textureManager.bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
        val tess = Tessellator.getInstance()
        val bb = tess.buffer

        var percentage = (container.entity.liquidXp/16000.0)*52.0
        var index = 0;

        (0..(percentage/16).toInt()).forEach { index ->
            val p = if(percentage > 16.0) 16.0 else percentage;
            bb.begin(7, VertexFormats.POSITION_TEXTURE_COLOR)
            bb.vertex(START_X+112.0, START_Y+70.0-(index*16.0), 50.0).texture(sprite.maxU, sprite.minV).color(color.red/255f, color.green/255f, color.blue/255f, 1f).next()
            bb.vertex(START_X+100.0, START_Y+70.0-(index*16.0), 50.0).texture(sprite.minU, sprite.minV).color(color.red/255f, color.green/255f, color.blue/255f, 1f).next()
            bb.vertex(START_X+100.0, START_Y+70.0-p-(index*16.0), 50.0).texture(sprite.minU, (sprite.maxV-((16f-p)/1024f)).toFloat()).color(color.red/255f, color.green/255f, color.blue/255f, 1f).next()
            bb.vertex(START_X+112.0, START_Y+70.0-p-(index*16.0), 50.0).texture(sprite.maxU, (sprite.maxV-((16f-p)/1024f)).toFloat()).color(color.red/255f, color.green/255f, color.blue/255f, 1f).next()
            tess.draw()
            percentage -= p;
        }

        minecraft!!.textureManager.bindTexture(TEXTURE)
        blit(START_X+100, START_Y+18, 172, 0, 12, 52)

    }

}
