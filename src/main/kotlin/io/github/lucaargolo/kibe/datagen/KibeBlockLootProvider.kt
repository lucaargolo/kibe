package io.github.lucaargolo.kibe.datagen

import io.github.lucaargolo.kibe.block.BlockCompendium
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.block.Block
import net.minecraft.component.DataComponentTypes
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.condition.SurvivesExplosionLootCondition
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.CopyComponentsLootFunction
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class KibeBlockLootProvider(dataOutput: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>?) : FabricBlockLootTableProvider(dataOutput, registryLookup) {

    override fun generate() {
        val list = mutableListOf<Block>()
        list.addAll(BlockCompendium.map.values)
        addBlockEntity(list, BlockCompendium.ENTANGLED_CHEST)
        addBlockEntity(list, BlockCompendium.ENTANGLED_TANK)
        addBlockEntity(list, BlockCompendium.TANK)
        addBlockEntity(list, BlockCompendium.COOLER)
        list.forEach(this::addDrop)
    }

    private fun addBlockEntity(list: MutableList<Block>, block: Block) {
        this.addDrop(block, LootTable.builder()
            .pool(LootPool.builder()
                .conditionally(SurvivesExplosionLootCondition.builder())
                .with(ItemEntry.builder(block))
                .apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY).include(DataComponentTypes.BLOCK_ENTITY_DATA))
                .rolls(ConstantLootNumberProvider.create(1.0F))
            )
        )
        list.remove(block)
    }

}