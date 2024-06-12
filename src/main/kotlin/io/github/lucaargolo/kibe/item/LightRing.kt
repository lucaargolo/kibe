package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.block.BlockCompendium
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult

class LightRing(settings: Settings): BlockItem(BlockCompendium.LIGHT_SOURCE, settings) {

    override fun useOnBlock(context: ItemUsageContext): ActionResult? {
        context.stack.increment(1)
        val actionResult = this.place(ItemPlacementContext(context))
        if(actionResult == ActionResult.FAIL) {
            context.stack.decrement(1)
        }
        return actionResult
    }

    override fun getTranslationKey() = this.orCreateTranslationKey

}