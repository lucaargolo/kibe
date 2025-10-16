package io.github.lucaargolo.kibe.datagen

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.component.ComponentType
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
        list.remove(BlockCompendium.CURSED_DIRT)
        this.addDrop(BlockCompendium.CURSED_DIRT) { block -> this.drops(Blocks.DIRT) }
        addBlockEntity(list, BlockCompendium.ENTANGLED_CHEST, ComponentTypeCompendium.RUNE_SET, ComponentTypeCompendium.ENTANGLED_KEY, ComponentTypeCompendium.OWNER)
        addBlockEntity(list, BlockCompendium.ENTANGLED_TANK, ComponentTypeCompendium.RUNE_SET, ComponentTypeCompendium.ENTANGLED_KEY, ComponentTypeCompendium.OWNER)
        addBlockEntity(list, BlockCompendium.TANK, DataComponentTypes.CUSTOM_DATA)
        addBlockEntity(list, BlockCompendium.COOLER, DataComponentTypes.CONTAINER)
        list.forEach(this::addDrop)
    }

    private fun addBlockEntity(list: MutableList<Block>, block: Block, vararg components: ComponentType<*>) {
        var copyFunction = CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY)
        for(component in components) {
            copyFunction = copyFunction.include(component)
        }
        this.addDrop(block, LootTable.builder()
            .pool(LootPool.builder()
                .conditionally(SurvivesExplosionLootCondition.builder())
                .with(ItemEntry.builder(block))
                .apply(copyFunction)
                .rolls(ConstantLootNumberProvider.create(1.0F))
            )
        )
        list.remove(block)
    }

}