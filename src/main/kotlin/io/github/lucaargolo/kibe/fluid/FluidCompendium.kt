package io.github.lucaargolo.kibe.fluid

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.fluid.miscellaneous.LiquidXpFluid
import io.github.lucaargolo.kibe.fluid.miscellaneous.ModdedFluid
import io.github.lucaargolo.kibe.item.ItemCompendium
import io.github.lucaargolo.kibe.utils.ModIdentifier
import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.Sprite
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions
import net.minecraftforge.common.SoundActions
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.ObjectHolderDelegate
import thedarkcolour.kotlinforforge.forge.registerObject
import java.util.function.Consumer


object FluidCompendium: RegistryCompendium<Fluid>(ForgeRegistries.FLUIDS) {

    private val FORGE_FLUID: DeferredRegister<FluidType> = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, KibeMod.MOD_ID)

    val LIQUID_XP_TYPE by FORGE_FLUID.registerObject("liquid_xp") {
        object: FluidType(Properties.create()
            .descriptionId("block.kibe.liquid_xp")
            .fallDistanceModifier(0F)
            .canExtinguish(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.ITEM_BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.ITEM_BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.BLOCK_FIRE_EXTINGUISH)
        ) {
            override fun initializeClient(consumer: Consumer<IClientFluidTypeExtensions>) {
                consumer.accept(object : IClientFluidTypeExtensions {
                    val stillSpriteId = ModIdentifier("block/liquid_xp_still")
                    val flowingSpriteId = ModIdentifier("block/liquid_xp_flow")

                    override fun getStillTexture() = stillSpriteId
                    override fun getFlowingTexture() = flowingSpriteId
                })
            }
        }
    }

    val LIQUID_XP by registerStill("liquid_xp", { LiquidXpFluid.Still() })
    val LIQUID_XP_FLOWING by registerFlowing("flowing_liquid_xp", { LiquidXpFluid.Flowing() })

    fun <E : FlowableFluid> registerStill(string: String, entry: () -> E): ObjectHolderDelegate<E> {
        val delegate = super.register(string, entry)
        BlockCompendium.registerFluidBlock(string, delegate)
        ItemCompendium.registerBucketItem(string, delegate)
        return delegate
    }

    fun <E : FlowableFluid> registerFlowing(string: String, entry: () -> E): ObjectHolderDelegate<E> {
        return super.register(string, entry)
    }

    override fun initialize() {
        super.initialize()
        FORGE_FLUID.register(MOD_BUS)
    }

}