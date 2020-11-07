package io.github.lucaargolo.kibe.blocks.entangledtank

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import io.github.lucaargolo.kibe.MARK_ENTANGLED_TANK_DIRTY_S2C
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState

class EntangledTankState(val world: ServerWorld, val key: String): PersistentState(key) {

    private var fluidInvMap = mutableMapOf<String, SimpleFixedFluidInv>()

    private fun createInventory(colorCode: String): SimpleFixedFluidInv {
        val fluidInv = SimpleFixedFluidInv(1, FluidAmount(16))
        fluidInvMap[colorCode] = fluidInv
        return fluidInv
    }

    fun markDirty(colorCode: String) {
        val server = world.server
        server.playerManager.playerList.forEach {
            val passedData = PacketByteBuf(Unpooled.buffer())
            passedData.writeString(key)
            passedData.writeString(colorCode)
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(it, MARK_ENTANGLED_TANK_DIRTY_S2C, passedData)
        }
        server.worlds.forEach {
            println("conserte isso depois gayyyy")
//            it.blockEntities.forEach { blockEntity ->
//                (blockEntity as? EntangledTankEntity)?.let { entangledTankEntity ->
//                    if(entangledTankEntity.colorCode == colorCode)
//                        entangledTankEntity.sync()
//                }
//            }
        }
        super.markDirty()
    }

    fun getOrCreateInventory(colorCode: String): SimpleFixedFluidInv {
        return fluidInvMap[colorCode] ?: createInventory(colorCode)
    }

    override fun fromTag(tag: CompoundTag) {
        tag.keys.forEach {
            val tempFluidInv = SimpleFixedFluidInv(1, FluidAmount(16))
            tempFluidInv.fromTag(tag.getCompound(it))
            fluidInvMap[it] = tempFluidInv
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        fluidInvMap.forEach { (colorCode, fluidInv) ->
            tag.put(colorCode, fluidInv.toTag())
        }
        return tag
    }

}