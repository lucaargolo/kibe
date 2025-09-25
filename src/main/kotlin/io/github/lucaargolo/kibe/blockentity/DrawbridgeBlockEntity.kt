package io.github.lucaargolo.kibe.blockentity

import io.github.lucaargolo.kibe.block.BlockCompendium
import io.github.lucaargolo.kibe.utils.SyncableBlockEntity
import net.fabricmc.fabric.api.entity.FakePlayer
import net.minecraft.block.AirBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.property.Properties
import net.minecraft.util.Hand
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World

class DrawbridgeBlockEntity(pos: BlockPos, state: BlockState): SyncableBlockEntity(BlockEntityCompendium.DRAWBRIDGE, pos, state), SidedInventory {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(2, ItemStack.EMPTY)

    var extendedStack: ItemStack = ItemStack.EMPTY
    var extendedStacks: Int = 0
    var state = State.CONTRACTED

    var lastCoverBlock: Block = BlockCompendium.DRAWBRIDGE

    enum class State {
        CONTRACTED,
        CONTRACTING,
        EXTENDING,
        EXTENDED
    }

    override fun writeNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.writeNbt(tag, registryLookup)
        tag.putString("state", state.name)
        tag.put("extendedStack", extendedStack.encodeAllowEmpty(registryLookup))
        tag.putInt("extendedStacks", extendedStacks)
        val nbtList = NbtList()
        for (i in inventory.indices) {
            val itemStack = inventory[i]
            val nbtCompound = NbtCompound()
            nbtCompound.putByte("Slot", i.toByte())
            if(!itemStack.isEmpty) {
                nbtList.add(itemStack.encode(registryLookup, nbtCompound))
            }else{
                nbtList.add(nbtCompound)
            }
        }
        tag.put("Items", nbtList)
    }

    override fun readNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        super.readNbt(tag, registryLookup)
        this.state = try {
            State.valueOf(tag.getString("state"))
        }catch (e: IllegalArgumentException) {
            State.CONTRACTED
        }
        extendedStack = ItemStack.fromNbtOrEmpty(registryLookup, tag.getCompound("extendedStack"))
        extendedStacks = tag.getInt("extendedBlocks")
        val nbtList: NbtList = tag.getList("Items", NbtElement.COMPOUND_TYPE.toInt())

        for (i in nbtList.indices) {
            val nbtCompound = nbtList.getCompound(i)
            val j = nbtCompound.getByte("Slot").toInt() and 255
            if (j < inventory.size) {
                if(nbtCompound.contains("id")) {
                    inventory[j] = ItemStack.fromNbt(registryLookup, nbtCompound).orElse(ItemStack.EMPTY)
                }else{
                    inventory[j] = ItemStack.EMPTY
                }
            }
        }
    }

    override fun writeClientNbt(tag: NbtCompound, registryLookup: WrapperLookup): NbtCompound {
        return tag.also { writeNbt(it, registryLookup) }
    }

    override fun readClientNbt(tag: NbtCompound, registryLookup: WrapperLookup) {
        readNbt(tag, registryLookup)
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

            val facing = state[Properties.FACING]

            val stack = blockEntity.inventory[0]
            val hasSpace = stack.isEmpty || stack.count < stack.maxCount

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
                    if(!stack.isEmpty) {
                        for(it in 1..64) {
                            val itPos = pos.add(Vec3i( facing.vector.x*it, facing.vector.y*it, facing.vector.z*it))
                            val itState = world.getBlockState(itPos)
                            val itBlock = itState.block
                            val itStack = stackFromLootTable(world as ServerWorld, itPos, itState)
                            if(itBlock is AirBlock) {
                                if(it != blockEntity.extendedStacks+1) {
                                    blockEntity.state = State.EXTENDED
                                    break
                                }

                                if(!ItemStack.areItemsAndComponentsEqual(blockEntity.extendedStack, stack)) {
                                    blockEntity.extendedStack = stack.copy()
                                    blockEntity.extendedStacks = 0
                                }
                                blockEntity.extendedStacks++

                                val fakePlayer = FakePlayer.get(world)
                                fakePlayer.setStackInHand(Hand.MAIN_HAND, stack)
                                val fakeHitPos = Vec3d(itPos.x + 0.5, itPos.y + 0.0, itPos.z + 0.5)
                                (stack.item as? BlockItem)?.useOnBlock(ItemUsageContext(fakePlayer, Hand.MAIN_HAND, BlockHitResult(fakeHitPos, facing.opposite, itPos, false)))
                                world.playSound(null, itPos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 1f, world.random.nextFloat() * 0.25f + 0.6f)

                                break
                            }else if(!ItemStack.areItemsAndComponentsEqual(itStack, stack)) {
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
                        var furthestStack = ItemStack.EMPTY

                        var selectedStack = stack

                        for(it in 1..64) {
                            val itPos = pos.add(Vec3i(facing.vector.x*it, facing.vector.y*it, facing.vector.z*it))
                            val itStack = stackFromLootTable(world as ServerWorld, itPos)
                            if(selectedStack.isEmpty) {
                                selectedStack = itStack
                            }
                            if(!ItemStack.areItemsAndComponentsEqual(blockEntity.extendedStack, selectedStack) || it > blockEntity.extendedStacks) {
                                break
                            }
                            if(!itStack.isEmpty && ItemStack.areItemsAndComponentsEqual(selectedStack, itStack)) {
                                furthestPos = itPos
                                furthestStack = itStack
                            } else {
                                if(it == 1) {
                                    blockEntity.state = State.CONTRACTED
                                }
                                break
                            }
                        }
                        if(furthestPos != pos) {
                            if(blockEntity.inventory[0].isEmpty) {
                                blockEntity.inventory[0] = furthestStack
                            }else{
                                blockEntity.inventory[0].increment(1)
                            }
                            blockEntity.extendedStacks--

                            world.breakBlock(furthestPos, false)
                            world.playSound(null, furthestPos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 1f, world.random.nextFloat() * 0.25f + 0.6f)
                        }
                    }
                }
            }
        }

        private fun stackFromLootTable(world: ServerWorld, pos: BlockPos): ItemStack {
            val state = world.getBlockState(pos)
            return stackFromLootTable(world, pos, state)
        }

        private fun stackFromLootTable(world: ServerWorld, pos: BlockPos, state: BlockState): ItemStack {
            val entity = world.getBlockEntity(pos)
            val silkTouch = Items.STICK.defaultStack.also {
                it.addEnchantment(world.registryManager.get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SILK_TOUCH).get(), 1)
            }
            val list = Block.getDroppedStacks(state, world, pos, entity, FakePlayer.get(world), silkTouch)
            if(list.size == 1) {
                val stack = list.first()
                if(stack.count == 1 && stack.item is BlockItem) {
                    return stack
                }
            }
            return ItemStack.EMPTY
        }
    }


}