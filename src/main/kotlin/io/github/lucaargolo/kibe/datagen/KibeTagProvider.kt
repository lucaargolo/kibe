package io.github.lucaargolo.kibe.datagen

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.fluid.FluidCompendium
import io.github.lucaargolo.kibe.item.ItemCompendium
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Block
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import kotlin.jvm.optionals.getOrNull

object KibeTagProvider {

    fun init(pack: FabricDataGenerator.Pack) {
        pack.addProvider { output, lookup ->
            object: FabricTagProvider<Item>(output, RegistryKeys.ITEM, lookup) {
                override fun configure(registryLookup: RegistryWrapper.WrapperLookup) {
                    ItemCompendium.tags.forEach { (key, entry) ->
                        getOrCreateTagBuilder(key).add(*entry.values.mapNotNull { holder -> holder.keyOrValue.left().getOrNull() }.toTypedArray()).also {
                            entry.children.forEach(it::addTag)
                        }.replace(false)
                    }
                }
            }
        }
        pack.addProvider { output, lookup ->
            object: FabricTagProvider<Block>(output, RegistryKeys.BLOCK, lookup) {
                override fun configure(registryLookup: RegistryWrapper.WrapperLookup) {
                    BlockCompendium.tags.forEach { (key, entry) ->
                        getOrCreateTagBuilder(key).add(*entry.values.mapNotNull { holder -> holder.keyOrValue.left().getOrNull() }.toTypedArray()).also {
                            entry.children.forEach(it::addTag)
                        }.replace(false)
                    }
                }
            }
        }
        pack.addProvider { output, lookup ->
            object: FabricTagProvider<Fluid>(output, RegistryKeys.FLUID, lookup) {
                override fun configure(registryLookup: RegistryWrapper.WrapperLookup) {
                    FluidCompendium.tags.forEach { (key, entry) ->
                        getOrCreateTagBuilder(key).add(*entry.values.mapNotNull { holder -> holder.keyOrValue.left().getOrNull() }.toTypedArray()).also {
                            entry.children.forEach(it::addTag)
                        }.replace(false)
                    }
                }
            }
        }
    }


}