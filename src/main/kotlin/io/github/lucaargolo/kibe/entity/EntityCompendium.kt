package io.github.lucaargolo.kibe.entity

import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraftforge.registries.ForgeRegistries

object EntityCompendium : RegistryCompendium<EntityType<*>>(ForgeRegistries.ENTITY_TYPES) {

    val THROWN_TORCH by register("thrown_torch", {
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::ThrownTorchEntity)
            .dimensions(EntityDimensions.changing(0.25f, 0.25f))
            .build()
    })

    override fun initializeClient() {
        EntityRendererRegistry.register(THROWN_TORCH) { context ->
            FlyingItemEntityRenderer(context)
        }
    }

}