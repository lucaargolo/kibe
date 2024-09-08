package io.github.lucaargolo.kibe.particle

import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.client.particle.FlameParticle
import net.minecraft.particle.ParticleType
import net.minecraft.registry.Registries
import net.minecraftforge.registries.ForgeRegistries

object ParticleCompendium: RegistryCompendium<ParticleType<*>>(ForgeRegistries.PARTICLE_TYPES) {

    val WATER_DROPS by register("water_drops", { FabricParticleTypes.simple() })

    override fun initializeClient() {
        ParticleFactoryRegistry.getInstance().register(WATER_DROPS) { sprite -> FlameParticle.Factory(sprite) }

    }

}