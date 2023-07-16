package io.github.lucaargolo.kibe.items.cooler

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.tooltip.BundleTooltipComponent
import net.minecraft.client.item.BundleTooltipData
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack

class CoolerTooltipComponent(data: BundleTooltipData) : BundleTooltipComponent(data) {

    override fun drawSlot(x: Int, y: Int, index: Int, shouldBlock: Boolean, textRenderer: TextRenderer, matrices: MatrixStack, itemRenderer: ItemRenderer, z: Int) {
        val itemStack = inventory[index]
        draw(matrices, x, y, z, Sprite.SLOT)
        itemRenderer.renderInGuiWithOverrides(itemStack, x + 1, y + 1, index)
        itemRenderer.renderGuiItemOverlay(textRenderer, itemStack, x + 1, y + 1)
    }

    override fun getColumns() = 1

    override fun getRows() = 1

}