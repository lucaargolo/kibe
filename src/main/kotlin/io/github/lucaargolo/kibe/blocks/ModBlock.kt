package io.github.lucaargolo.kibe.blocks

import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

open class ModBlock(block: Block) {

    constructor(block: Block, boolean: Boolean) : this(block) {
        this.boolean = boolean
    }

    var boolean: Boolean = true

    var block: Block = block
        private set

    open fun init(identifier: Identifier) {
        Registry.register(Registry.BLOCK, identifier, block)
        if(boolean) Registry.register(Registry.ITEM, identifier, BlockItem(block, Item.Settings()))
    }

    open fun initClient(identifier: Identifier) {}
}