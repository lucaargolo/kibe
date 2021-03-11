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
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3i
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

class DrawbridgeBlockEntity(drawbridge: Drawbridge, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(drawbridge), pos, state), BlockEntityClientSerializable, SidedInventory {

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

    override fun writeNbt(tag: CompoundTag): CompoundTag {
        tag.putString("state", state.name)
        tag.putString("extendedBlock", extendedBlock?.let { Registry.BLOCK.getId(it).toString() } ?: "yeet")
        tag.putInt("extendedBlocks", extendedBlocks)
        val listTag = ListTag()
        for (i in inventory.indices) {
            val itemStack = inventory[i]
            val compoundTag = CompoundTag()
            compoundTag.putByte("Slot", i.toByte())
            itemStack.writeNbt(compoundTag)
            listTag.add(compoundTag)
        }
        tag.put("Items", listTag)
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: CompoundTag) {
        super.readNbt(tag)
        this.state = try {
            State.valueOf(tag.getString("state"))
        }catch (e: IllegalArgumentException) {
            State.CONTRACTED
        }
        extendedBlock = Registry.BLOCK.get(Identifier(tag.getString("extendedBlock")))
        extendedBlocks = tag.getInt("extendedBlocks")
        Inventories.readNbt(tag, inventory)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        return writeNbt(tag)
    }

    override fun fromClientTag(tag: CompoundTag) {
        readNbt(tag)
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

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: DrawbridgeBlockEntity) {
            if(world.isClient || world.time%8 != 0L) {
                return
            }

            val direction = state[Properties.FACING]

            val hasSpace = blockEntity.inventory[0].isEmpty || blockEntity.inventory[0].count < blockEntity.inventory[0].maxCount
            val storedBlock = (blockEntity.inventory[0].item as? BlockItem)?.block

            when(blockEntity.state) {
                State.EXTENDED -> {
                    if(!world.isReceivingRedstonePower(pos)) {
                        blockEntity.state = State.CONTRACTING
                    }
                }
                State.EXTENDING -> {
                    if(!world.isReceivingRedstonePower(pos)) {
                        blockEntity.state = State.CONTRACTING
                    }
                    if(storedBlock != null) {
                        for(it in 1..64) {
                            val itPos = pos.add(Vec3i( direction.vector.x*it, direction.vector.y*it, direction.vector.z*it))
                            val itBlock = world.getBlockState(itPos).block
                            if(itBlock is AirBlock) {
                                if(it != blockEntity.extendedBlocks+1) {
                                    blockEntity.state = State.EXTENDED
                                    break
                                }
                                blockEntity.inventory[0].decrement(1)
                                world.setBlockState(itPos, storedBlock.defaultState)
                                world.playSound(null, itPos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.25f + 0.6f)
                                if(blockEntity.extendedBlock != storedBlock) {
                                    blockEntity.extendedBlock = storedBlock
                                    blockEntity.extendedBlocks = 0
                                }
                                blockEntity.extendedBlocks++
                                break
                            }else if(itBlock != storedBlock) {
                                blockEntity.state = State.EXTENDED
                                break
                            }
                            if(it == 64) {
                                blockEntity.state = State.EXTENDED
                            }
                        }
                    }else{
                        blockEntity.state = State.EXTENDED
                    }

                }
                State.CONTRACTED -> {
                    if(world.isReceivingRedstonePower(pos)) {
                        blockEntity.state = State.EXTENDING
                    }
                }
                State.CONTRACTING -> {
                    if(world.isReceivingRedstonePower(pos)) {
                        blockEntity.state = State.EXTENDING
                    }
                    if(hasSpace) {
                        var furthestPos = pos
                        var furthestBlock = Blocks.AIR

                        var selectedBlock = storedBlock ?: Blocks.AIR

                        for(it in 1..64) {
                            val itPos = pos.add(Vec3i(direction.vector.x*it, direction.vector.y*it, direction.vector.z*it))
                            val itBlock = world.getBlockState(itPos).block
                            if(selectedBlock is AirBlock) {
                                selectedBlock = itBlock
                            }
                            if(blockEntity.extendedBlock != selectedBlock || it > blockEntity.extendedBlocks) {
                                break
                            }
                            if(itBlock !is AirBlock && itBlock == selectedBlock) {
                                furthestPos = itPos
                                furthestBlock = itBlock
                            } else {
                                if(it == 1) {
                                    blockEntity.state = State.CONTRACTED
                                }
                                break
                            }
                        }
                        if(furthestPos != pos) {
                            if(blockEntity.inventory[0].isEmpty) {
                                blockEntity.inventory[0] = ItemStack(furthestBlock.asItem())
                            }else{
                                blockEntity.inventory[0].increment(1)
                            }
                            blockEntity.extendedBlocks--
                            world.setBlockState(furthestPos, Blocks.AIR.defaultState)
                            world.playSound(null, furthestPos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.25f + 0.6f)
                        }
                    }
                }
            }

        }
    }


}