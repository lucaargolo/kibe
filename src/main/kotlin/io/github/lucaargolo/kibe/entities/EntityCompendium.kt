package io.github.lucaargolo.kibe.entities

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.entities.miscellaneous.ThrownTorchEntity
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.entity.EntityDimensions
import net.minecraft.world.World

val entityRegistry = mutableMapOf<Identifier, EntityType<*>>()

val THROWN_TORCH = register (
    Identifier(MOD_ID, "thrown_torch"),
    FabricEntityTypeBuilder.create(SpawnGroup.MISC) { type: EntityType<ThrownTorchEntity>, world: World -> ThrownTorchEntity(type, world) }.dimensions(EntityDimensions.changing(0.25f, 0.25f)).build()
)

private fun <T: Entity> register(identifier: Identifier, entityType: EntityType<T>): EntityType<T> {
    entityRegistry[identifier] = entityType
    return entityType
}

fun initEntities() {
    entityRegistry.forEach{ Registry.register(Registry.ENTITY_TYPE, it.key, it.value) }
}

fun initEntitiesClient() {
    EntityRendererRegistry.INSTANCE.register(THROWN_TORCH) { dispatcher, context ->
        FlyingItemEntityRenderer(dispatcher, context.itemRenderer)
    }
}