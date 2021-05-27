package io.github.lucaargolo.kibe.blocks.vacuum

import alexiil.mc.lib.attributes.ListenerRemovalToken
import alexiil.mc.lib.attributes.ListenerToken
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FixedFluidInv
import alexiil.mc.lib.attributes.fluid.FluidInvTankChangeListener
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.fluids.LIQUID_XP
import io.github.lucaargolo.kibe.mixin.ExperienceOrbEntityAccessor
import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_TYPE
import io.github.lucaargolo.kibe.recipes.vacuum.VacuumHopperRecipe
import io.github.lucaargolo.kibe.utils.FluidTank
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.PropertyDelegate
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.Tickable
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class VacuumHopperEntity(private val vacuumHopper: VacuumHopper, pos: BlockPos, state: BlockState): LockableContainerBlockEntity(getEntityType(vacuumHopper), pos, state), FixedFluidInv, BlockEntityClientSerializable {

    private var processingRecipe: Identifier? = null
    private var processingTicks = 0
    private var totalProcessingTicks = 0

    val propertyDelegate = object: PropertyDelegate {
        override fun get(index: Int): Int {
            return when(index) {
                0 -> processingTicks
                1 -> totalProcessingTicks
                else -> 0
            }
        }

        override fun set(index: Int, value: Int) {
            when(index) {
                0 -> processingTicks = value
                1 -> totalProcessingTicks = value
            }
        }

        override fun size() = 2
    }

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(11, ItemStack.EMPTY)
    val tanks = listOf(FluidTank(FluidAmount.ofWhole(16)))

    override fun getTankCount() = tanks.size

    override fun isFluidValidForTank(tank: Int, fluidKey: FluidKey?) = tanks[tank].volume.fluidKey == fluidKey || (tanks[tank].volume.fluidKey.isEmpty && fluidKey == LIQUID_XP)

    override fun getMaxAmount_F(tank: Int) = tanks[tank].capacity

    override fun getInvFluid(tank: Int) = tanks[tank].volume

    override fun setInvFluid(tank: Int, to: FluidVolume, simulation: Simulation?): Boolean {
        return if (isFluidValidForTank(tank, to.fluidKey)) {
            if (simulation?.isAction == true)
                tanks[tank].volume = to
            markDirty()
            true
        } else false
    }

    override fun addListener(p0: FluidInvTankChangeListener?, p1: ListenerRemovalToken?) = ListenerToken {}

    private fun addLiquidXp(qnt: Int): Boolean {
        val currentAmount = tanks[0].volume.amount()
        val newAmount = FluidAmount.of(currentAmount.asLong(1000) + qnt, 1000)
        if (newAmount > tanks[0].capacity)
            tanks[0].volume = LIQUID_XP.key.withAmount(tanks[0].capacity)
        else
            tanks[0].volume = LIQUID_XP.key.withAmount(newAmount)
        markDirty()
        return true
    }

    override fun markDirty() {
        super.markDirty()
        if(world?.isClient == false) sync()
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        val tanksTag = NbtCompound()
        tanks.forEachIndexed { index, tank ->
            val tankTag = NbtCompound()
            tankTag.put("fluids", tank.volume.toTag())
            tanksTag.put(index.toString(), tankTag)
        }
        tag.put("tanks", tanksTag)
        Inventories.writeNbt(tag, inventory)
        return super.writeNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound) = writeNbt(tag)

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        val tanksTag = tag.getCompound("tanks")
        tanksTag.keys.forEachIndexed { idx, key ->
            val tankTag = tanksTag.getCompound(key)
            val volume = FluidVolume.fromTag(tankTag.getCompound("fluids"))
            tanks[idx].volume = volume
        }
        //Backwards compatibility
        if(tag.contains("fluid")) {
            val liquidXp = tag.getInt("fluid")
            tanks[0].volume = LIQUID_XP.key.withAmount(FluidAmount.of(liquidXp.toLong(), 1000))
        }
        Inventories.readNbt(tag, inventory)
    }

    override fun fromClientTag(tag: NbtCompound) = readNbt(tag)

    fun addStack(stack: ItemStack): ItemStack {
        var modifiableStack = stack
        inventory.forEachIndexed { id, stk ->
            if(modifiableStack == ItemStack.EMPTY) return@forEachIndexed
            if(stk.isEmpty) {
                inventory[id] = modifiableStack
                modifiableStack = ItemStack.EMPTY
            }else{
                if(ItemStack.areItemsEqual(stk, modifiableStack) && ItemStack.areTagsEqual(stk, modifiableStack)) {
                    if(stk.count+modifiableStack.count > stk.maxCount) {
                        val aux = stk.maxCount-stk.count
                        stk.count = stk.maxCount
                        modifiableStack.count -= aux
                    }else if(stk.count+modifiableStack.count == stk.maxCount){
                        stk.count = stk.maxCount
                        modifiableStack = ItemStack.EMPTY
                    }else{
                        stk.count += modifiableStack.count
                        modifiableStack = ItemStack.EMPTY
                    }
                }
                if(modifiableStack.count <= 0) {
                    modifiableStack = ItemStack.EMPTY
                }
            }
        }
        markDirty()
        return modifiableStack
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

    override fun getAvailableSlots(side: Direction?) = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = slot == 9

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = slot != 9

    override fun tick() {

        var actualProcessingRecipe: VacuumHopperRecipe? = null
        (world as? ServerWorld)?.let { serverWorld ->
            if(processingRecipe == null) {
                if (!getStack(9).isEmpty) {
                    actualProcessingRecipe = serverWorld.server.recipeManager.getFirstMatch(VACUUM_HOPPER_RECIPE_TYPE, this, world).orElseGet { null }
                }
            }else{
                serverWorld.server.recipeManager.get(processingRecipe).ifPresent {
                    (it as? VacuumHopperRecipe)?.let { vacuumHopperRecipe -> actualProcessingRecipe = vacuumHopperRecipe }
                }
            }
            processingRecipe = actualProcessingRecipe?.id
            actualProcessingRecipe?.let { recipe ->
                if(recipe.matches(this, serverWorld)) {
                    totalProcessingTicks = recipe.ticks
                    if(processingTicks++ >= recipe.ticks) {
                        recipe.craft(this)
                        processingRecipe = null
                        processingTicks = 0
                        totalProcessingTicks = 0
                    }
                }else{
                    processingRecipe = null
                    processingTicks = 0
                    totalProcessingTicks = 0
                }
            }
            if(actualProcessingRecipe == null) {
                processingRecipe = null
                processingTicks = 0
                totalProcessingTicks = 0
            }
        }

        if(!cachedState[Properties.ENABLED]) return
        val pos1 = BlockPos(pos.x - 8, pos.y - 8, pos.z - 8)
        val pos2 = BlockPos(pos.x + 8, pos.y + 8, pos.z + 8)
        val vecPos = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        val validEntities = world?.getEntitiesByType<Entity>(null, Box(pos1, pos2)) { it is ItemEntity || it is ExperienceOrbEntity }
        validEntities?.forEach {
            val distance: Double = it.pos.distanceTo(vecPos)
            if (distance < 1.0) {
                if(it is ExperienceOrbEntity) {
                    addLiquidXp((it as ExperienceOrbEntityAccessor).amount * 10)
                    it.remove()
                }
                if(it is ItemEntity) {
                    it.stack = addStack(it.stack)
                }
            }
            val vel = it.pos.reverseSubtract(vecPos).normalize().multiply(0.1)
            it.addVelocity(vel.x, vel.y, vel.z)
        }
    }

}