package io.github.lucaargolo.kibe.blocks.drawbridge

import io.github.lucaargolo.kibe.blocks.DRAWBRIDGE
import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.AirBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.Tickable
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry

class DrawbridgeBlockEntity(drawbridge: Drawbridge): BlockEntity(getEntityType(drawbridge)), BlockEntityClientSerializable, SidedInventory, Tickable {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(2, ItemStack.EMPTY)

    var extendedBlock: Block? = null
    var extendedBlocks = 0
    var state = State.CONTRACTED

    var lastCoverBlock = DRAWBRIDGE

    enum class State {
        CONTRACTED,
        CONTRACTING,
        EXTENDING,
        EXTENDED
    }

    override fun tick() {
        val world = world ?: return
        if(world.isClient || world.time%8 != 0L) {
            return
        }

        val direction = cachedState[Properties.FACING]

        val hasSpace = inventory[0].isEmpty || inventory[0].count < inventory[0].maxCount
        val storedBlock = (inventory[0].item as? BlockItem)?.block

        when(state) {
            State.EXTENDED -> {
                if(!world.isReceivingRedstonePower(pos)) {
                    state = State.CONTRACTING
                }
            }
            State.EXTENDING -> {
                if(!world.isReceivingRedstonePower(pos)) {
                    state = State.CONTRACTING
                }
                if(storedBlock != null) {
                    for(it in 1..64) {
                        val itPos = pos.add(direction.vector.x*it, direction.vector.y*it, direction.vector.z*it)
                        val itBlock = world.getBlockState(itPos).block
                        if(itBlock is AirBlock) {
                            if(it != extendedBlocks+1) {
                                state = State.EXTENDED
                                break
                            }
                            inventory[0].decrement(1)
                            world.setBlockState(itPos, storedBlock.defaultState)
                            world.playSound(null, itPos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.25f + 0.6f)
                            if(extendedBlock != storedBlock) {
                                extendedBlock = storedBlock
                                extendedBlocks = 0
                            }
                            extendedBlocks++
                            break
                        }else if(itBlock != storedBlock) {
                            state = State.EXTENDED
                            break
                        }
                        if(it == 64) {
                            state = State.EXTENDED
                        }
                    }
                }else{
                    state = State.EXTENDED
                }

            }
            State.CONTRACTED -> {
                if(world.isReceivingRedstonePower(pos)) {
                    state = State.EXTENDING
                }
            }
            State.CONTRACTING -> {
                if(world.isReceivingRedstonePower(pos)) {
                    state = State.EXTENDING
                }
                if(hasSpace) {
                    var furthestPos = pos
                    var furthestBlock = Blocks.AIR

                    var selectedBlock = storedBlock ?: Blocks.AIR

                    for(it in 1..64) {
                        val itPos = pos.add(direction.vector.x*it, direction.vector.y*it, direction.vector.z*it)
                        val itBlock = world.getBlockState(itPos).block
                        if(selectedBlock is AirBlock) {
                            selectedBlock = itBlock
                        }
                        if(extendedBlock != selectedBlock || it > extendedBlocks) {
                            break
                        }
                        if(itBlock !is AirBlock && itBlock == selectedBlock) {
                            furthestPos = itPos
                            furthestBlock = itBlock
                        } else {
                            if(it == 1) {
                                state = State.CONTRACTED
                            }
                            break
                        }
                    }
                    if(furthestPos != pos) {
                        if(inventory[0].isEmpty) {
                            inventory[0] = ItemStack(furthestBlock.asItem())
                        }else{
                            inventory[0].increment(1)
                        }
                        extendedBlocks--
                        world.setBlockState(furthestPos, Blocks.AIR.defaultState)
                        world.playSound(null, furthestPos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.25f + 0.6f)
                    }
                }
            }
        }

    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putString("state", state.name)
        tag.putString("extendedBlock", extendedBlock?.let { Registry.BLOCK.getId(it).toString() } ?: "yeet")
        tag.putInt("extendedBlocks", extendedBlocks)
        val listTag = ListTag()
        for (i in inventory.indices) {
            val itemStack = inventory[i]
            val compoundTag = CompoundTag()
            compoundTag.putByte("Slot", i.toByte())
            itemStack.toTag(compoundTag)
            listTag.add(compoundTag)
        }
        tag.put("Items", listTag)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        this.state = try {
            State.valueOf(tag.getString("state"))
        }catch (e: IllegalArgumentException) {
            State.CONTRACTED
        }
        extendedBlock = Registry.BLOCK.get(Identifier(tag.getString("extendedBlock")))
        extendedBlocks = tag.getInt("extendedBlocks")
        Inventories.fromTag(tag, inventory)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        return toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag) {
        fromTag(DRAWBRIDGE.defaultState, tag)
        MinecraftClient.getInstance().worldRenderer.updateBlock(world, pos, cachedState, cachedState, 0)
    }

    override fun size() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int) = inventory[slot]

    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(inventory, slot, amount)

    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(this.inventory, slot)

    override fun setStack(slot: Int, stack: ItemStack?) {
        inventory[slot] = stack
        if (stack!!.count > maxCountPerStack) {
            stack.count = maxCountPerStack
        }
    }

    override fun clear()  = inventory.clear()

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }

    override fun getAvailableSlots(side: Direction?) = intArrayOf(0)

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = stack.item is BlockItem && (stack.item as BlockItem).block.defaultState.isFullCube(world, pos)

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = true


}