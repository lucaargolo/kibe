package io.github.lucaargolo.kibe.items.entangled

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.kibe.items.miscellaneous.Rune
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier

class EntangledBagScreen(container: EntangledBagContainer, inventory: PlayerInventory, title: Text): AbstractInventoryScreen<EntangledBagContainer>(container, inventory, title) {

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
        (1..8).forEach {
            val color = DyeColor.byName(container.tag.getString("rune$it"), DyeColor.WHITE)
            itemRenderer.renderGuiItem(ItemStack(Rune.getRuneByColor(color)), START_X+87+(it-1)*10, START_Y+2)
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

    fun hasSameColors(map: MutableMap<Int, DyeColor>): Boolean {
        map.forEach { (key, value) ->
            if(value != DyeColor.byName(container.tag.getString("rune$key"), DyeColor.WHITE)) return false
        }
        return true
    }


}