package io.github.lucaargolo.kibe.items.cooler

import net.minecraft.client.item.BundleTooltipData
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

class CoolerTooltipData(inventory: DefaultedList<ItemStack>) : BundleTooltipData(inventory, 0)