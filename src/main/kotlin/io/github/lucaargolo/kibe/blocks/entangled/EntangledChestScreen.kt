package io.github.lucaargolo.kibe.blocks.entangled

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class EntangledChestScreen(container: EntangledChestContainer?, inventory: PlayerInventory?, title: Text?): AbstractInventoryScreen<EntangledChestContainer>(container, inventory, title) {

    private val TEXTURE = Identifier("kibe:textures/gui/entangled_chest.png")

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        //this.renderBackground()
        //super.render(mouseX, mouseY, delta)
        //drawMouseoverTooltip(mouseX, mouseY)
    }

    override fun drawForeground(mouseX: Int, mouseY: Int) {
        font.draw(title.asFormattedString(), 8.0f, 6.0f, 0)
        font.draw(playerInventory.displayName.asFormattedString(), 8.0f, (containerHeight - 96 + 4).toFloat(), 0)
    }

    override fun drawBackground(delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft!!.textureManager.bindTexture(TEXTURE)
        blit(width/2-containerWidth/2,height/2-containerHeight/2, 0, 0, 176, 166)
    }

}