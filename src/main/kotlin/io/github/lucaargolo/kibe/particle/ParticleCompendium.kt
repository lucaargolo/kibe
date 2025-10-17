package io.github.lucaargolo.kibe.particle

import io.github.lucaargolo.kibe.utils.RegistryCompendium
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.client.particle.FlameParticle
import net.minecraft.particle.ParticleType
import net.minecraft.registry.Registries
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object ParticleCompendium: RegistryCompendium<ParticleType<*>>(Registries.PARTICLE_TYPE) {

    val WATER_DROPS by register("water_drops") { FabricParticleTypes.simple() }

    override fun initializeClient() {
        MOD_BUS.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        ParticleFactoryRegistry.getInstance().register(WATER_DROPS) { sprite -> FlameParticle.Factory(sprite) }
    }

}