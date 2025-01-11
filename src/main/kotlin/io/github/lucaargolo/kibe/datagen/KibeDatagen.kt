package io.github.lucaargolo.kibe.datagen

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object KibeDatagen: DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
        val myPack: FabricDataGenerator.Pack = generator.createPack()
        myPack.addProvider(::KibeBlockLootProvider)
    }

}