package io.github.lucaargolo.kibe.entity

import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object EntityCompendium : RegistryCompendium<EntityType<*>>(Registries.ENTITY_TYPE) {

    val THROWN_TORCH by register("thrown_torch", {
        EntityType.Builder.create(::ThrownTorchEntity, SpawnGroup.MISC)
            .dimensions(0.25f, 0.25f)
            .build("thrown_torch")
    })

    override fun initializeClient() {
        MOD_BUS.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        EntityRendererRegistry.register(THROWN_TORCH) { context ->
            FlyingItemEntityRenderer(context)
        }
    }

}