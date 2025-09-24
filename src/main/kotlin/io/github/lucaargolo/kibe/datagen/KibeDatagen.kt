package io.github.lucaargolo.kibe.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.registry.RegistryBuilder
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import net.neoforged.neoforge.data.event.GatherDataEvent


object KibeDatagen {

    fun onInitializeDataGenerator(event: GatherDataEvent) {
        val generator = event.getGenerator()
        val output = generator.getPackOutput()
        val fabricOutput = FabricDataOutput(FabricLoader.getInstance().getModContainer("kibe").get(), output.path, true)
        val builtinProvider = DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), RegistryBuilder(), setOf<String>())
        generator.addProvider(event.includeServer(), KibeBlockLootProvider(fabricOutput, builtinProvider.registryProvider))
    }

}