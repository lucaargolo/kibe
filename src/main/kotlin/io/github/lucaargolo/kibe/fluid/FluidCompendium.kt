package io.github.lucaargolo.kibe.fluid

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.fluid.miscellaneous.LiquidXpFluid
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.utils.ModIdentifier
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent
import net.neoforged.neoforge.common.SoundActions
import net.neoforged.neoforge.fluids.FluidType
import net.neoforged.neoforge.fluids.FluidType.Properties
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

object FluidCompendium: RegistryCompendium<Fluid>(Registries.FLUID) {

    private val FORGE_FLUID: DeferredRegister<FluidType> = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, KibeMod.MOD_ID)

    val LIQUID_XP_TYPE by FORGE_FLUID.register("liquid_xp", Supplier {
        FluidType(Properties.create()
            .descriptionId("block.kibe.liquid_xp")
            .fallDistanceModifier(0F)
            .canExtinguish(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.ITEM_BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.ITEM_BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.BLOCK_FIRE_EXTINGUISH)
        )
    })

    val LIQUID_XP by registerStill("liquid_xp") { LiquidXpFluid.Still() }
    val LIQUID_XP_FLOWING by registerFlowing("flowing_liquid_xp") { LiquidXpFluid.Flowing() }
    val EXPERIENCE by registerTag(Identifier.of("c", "experience"), "liquid_xp")

    fun <E : FlowableFluid> registerStill(string: String, entry: () -> E): DeferredHolder<Fluid, E> {
        val delegate = super.register(string, entry)
        BlockCompendium.registerFluidBlock(string, delegate)
        ItemCompendium.registerBucketItem(string, delegate)
        return delegate
    }

    fun <E : FlowableFluid> registerFlowing(string: String, entry: () -> E): DeferredHolder<Fluid, E> {
        return super.register(string, entry)
    }

    override fun initialize() {
        super.initialize()
        FORGE_FLUID.register(MOD_BUS)
    }

    override fun initializeClient() {
        super.initializeClient()
        MOD_BUS.addListener(::onClientExtensions)
    }

    private fun onClientExtensions(event: RegisterClientExtensionsEvent) {
        event.registerFluidType(object : IClientFluidTypeExtensions {
            val stillSpriteId = ModIdentifier.of("block/liquid_xp_still")
            val flowingSpriteId = ModIdentifier.of("block/liquid_xp_flow")

            override fun getStillTexture() = stillSpriteId
            override fun getFlowingTexture() = flowingSpriteId
        }, LIQUID_XP_TYPE)
    }

}