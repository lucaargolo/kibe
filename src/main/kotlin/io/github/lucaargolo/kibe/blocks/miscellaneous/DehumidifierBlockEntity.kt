package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World

class DehumidifierBlockEntity(dehumidifier: Dehumidifier, pos: BlockPos, state: BlockState): BlockEntity(getEntityType(dehumidifier), pos, state) {

    override fun setWorld(world: World) {
        super.setWorld(world)
        if(world.isClient) {
            setupDehumidifier(ChunkPos(pos), this)
        }
    }

    companion object {

        private val activeDehumidifiers = linkedMapOf<ChunkPos, LinkedHashSet<DehumidifierBlockEntity>>()

        fun setupDehumidifier(center: ChunkPos, dehumidifier: DehumidifierBlockEntity) {
            activeDehumidifiers.getOrPut(center) { linkedSetOf() }.add(dehumidifier)
            activeDehumidifiers.getOrPut(ChunkPos(center.x-1, center.z)) { linkedSetOf() }.add(dehumidifier)
            activeDehumidifiers.getOrPut(ChunkPos(center.x+1, center.z)) { linkedSetOf() }.add(dehumidifier)
            activeDehumidifiers.getOrPut(ChunkPos(center.x-1, center.z-1)) { linkedSetOf() }.add(dehumidifier)
            activeDehumidifiers.getOrPut(ChunkPos(center.x+1, center.z-1)) { linkedSetOf() }.add(dehumidifier)
            activeDehumidifiers.getOrPut(ChunkPos(center.x-1, center.z+1)) { linkedSetOf() }.add(dehumidifier)
            activeDehumidifiers.getOrPut(ChunkPos(center.x+1, center.z+1)) { linkedSetOf() }.add(dehumidifier)
            activeDehumidifiers.getOrPut(ChunkPos(center.x, center.z-1)) { linkedSetOf() }.add(dehumidifier)
            activeDehumidifiers.getOrPut(ChunkPos(center.x, center.z+1)) { linkedSetOf() }.add(dehumidifier)
        }

        fun isBeingDehumidified(chunkPos: ChunkPos): Boolean {
            val activeDehumidifiersIterator = activeDehumidifiers[chunkPos]?.iterator() ?: return false
            while(activeDehumidifiersIterator.hasNext()) {
                val dehumidifier = activeDehumidifiersIterator.next()
                if(!dehumidifier.isRemoved) {
                    if(dehumidifier.cachedState[Properties.ENABLED]) {
                        return true
                    }
                }else{
                    activeDehumidifiersIterator.remove()
                }
            }
            return false
        }

    }

}