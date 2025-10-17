package io.github.lucaargolo.kibe.client.model

import io.github.lucaargolo.kibe.client.KibeModClient
import io.github.lucaargolo.kibe.enchantment.EnchantmentCompendium
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.client.render.model.BakedModel
import net.minecraft.item.ItemStack
import net.minecraft.util.math.random.Random
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull

class SlimyBootsModel(wrapped: BakedModel): ForwardingBakedModel(wrapped) {

    private val slimyOverlay by lazy {
        KibeModClient.bakedModel(ModIdentifier.of("item/slimy_boots_overlay"))
    }

    override fun isVanillaAdapter(): Boolean {
        return false
    }

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>, context: RenderContext) {
        super.emitItemQuads(stack, randomSupplier, context)
        val isSlimy = stack.enchantments.enchantments.firstOrNull { enchantment -> enchantment.key.getOrNull() == EnchantmentCompendium.SLIMY } != null
        if(isSlimy) {
            slimyOverlay?.emitItemQuads(stack, randomSupplier, context)
        }
    }

}