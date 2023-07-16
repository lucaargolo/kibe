package io.github.lucaargolo.kibe.items.cooler

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.tooltip.BundleTooltipComponent
import net.minecraft.client.item.BundleTooltipData

class CoolerTooltipComponent(data: BundleTooltipData) : BundleTooltipComponent(data) {

    override fun drawSlot(x: Int, y: Int, index: Int, shouldBlock: Boolean, context: DrawContext, textRenderer: TextRenderer) {
        val itemStack = inventory[index]
        draw(context, x, y, Sprite.SLOT)
        context.drawItem(itemStack, x + 1, y + 1, index)
        context.drawItemInSlot(textRenderer, itemStack, x + 1, y + 1)
    }

    override fun getColumns() = 1

    override fun getRows() = 1

}