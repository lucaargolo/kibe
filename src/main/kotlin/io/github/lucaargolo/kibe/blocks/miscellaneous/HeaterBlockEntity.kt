package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.IceBlock
import net.minecraft.block.SnowBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

class HeaterBlockEntity(heater: Heater): BlockEntity(getEntityType(heater)), Tickable {

    override fun tick() {
        if(cachedState[Properties.ENABLED]) {
            val center = ChunkPos(pos)
            repeat(32) {
                val x = (center.startX-16..center.endX+16).random()
                val y = (pos.y-6..pos.y+6).random()
                val z = (center.startZ-16..center.endZ+16).random()
                val meltPos = BlockPos(x, y, z)
                world?.let { world ->
                    val meltBlockState = world.getBlockState(meltPos)
                    val meltBlock = meltBlockState.block
                    (meltBlock as? IceBlock)?.let {
                        if (world.dimension.isUltrawarm) {
                            world.removeBlock(meltPos, false)
                        } else {
                            world.setBlockState(meltPos, Blocks.WATER.defaultState)
                            world.updateNeighbor(meltPos, Blocks.WATER, meltPos)
                        }
                    }
                    (meltBlock as? SnowBlock)?.let {
                        Block.dropStacks(meltBlockState, world, meltPos)
                        world.removeBlock(meltPos, false)
                    }
                }
            }
        }
    }

    override fun setLocation(world: World, pos: BlockPos) {
        super.setLocation(world, pos)
        (world as? ServerWorld)?.let {
            setupHeater(it, ChunkPos(pos), this)
        }
    }

    companion object {

        private val globalActiveHeater = linkedMapOf<RegistryKey<World>, LinkedHashMap<ChunkPos, LinkedHashSet<HeaterBlockEntity>>>()

        fun setupHeater(world: ServerWorld, center: ChunkPos, lamp: HeaterBlockEntity) {
            val activeLamps = globalActiveHeater.getOrPut(world.registryKey) { linkedMapOf() }
            activeLamps.getOrPut(center) { linkedSetOf() }.add(lamp)
            activeLamps.getOrPut(ChunkPos(center.x-1, center.z)) { linkedSetOf() }.add(lamp)
            activeLamps.getOrPut(ChunkPos(center.x+1, center.z)) { linkedSetOf() }.add(lamp)
            activeLamps.getOrPut(ChunkPos(center.x-1, center.z-1)) { linkedSetOf() }.add(lamp)
            activeLamps.getOrPut(ChunkPos(center.x+1, center.z-1)) { linkedSetOf() }.add(lamp)
            activeLamps.getOrPut(ChunkPos(center.x-1, center.z+1)) { linkedSetOf() }.add(lamp)
            activeLamps.getOrPut(ChunkPos(center.x+1, center.z+1)) { linkedSetOf() }.add(lamp)
            activeLamps.getOrPut(ChunkPos(center.x, center.z-1)) { linkedSetOf() }.add(lamp)
            activeLamps.getOrPut(ChunkPos(center.x, center.z+1)) { linkedSetOf() }.add(lamp)
        }

        fun isBeingHeated(world: ServerWorld, chunkPos: ChunkPos): Boolean {
            val activeLamps = globalActiveHeater.getOrPut(world.registryKey) { linkedMapOf() }
            val lampSet = activeLamps[chunkPos] ?: return false
            val lampSetIterator = lampSet.iterator()
            while(lampSetIterator.hasNext()) {
                val lamp = lampSetIterator.next()
                if(!lamp.isRemoved) {
                    if(lamp.cachedState[Properties.ENABLED]) {
                        return true
                    }
                }else{
                    lampSetIterator.remove()
                }
            }
            return false
        }

    }

}