package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.effects.CURSED_EFFECT
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

abstract class Lasso(settings: Settings): Item(settings) {

    override fun hasGlint(stack: ItemStack): Boolean {
        return stack.orCreateNbt.contains("Entity")
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val stackTag = context.stack.orCreateNbt
        if(stackTag.contains("Entity")) {
            if(context.world is ServerWorld) {
                val pos = context.blockPos

                val targetPos = when(context.side) {
                    Direction.DOWN -> pos.down(2)
                    Direction.UP -> pos.up()
                    Direction.EAST -> pos.east()
                    Direction.NORTH -> pos.north()
                    Direction.WEST -> pos.west()
                    Direction.SOUTH -> pos.south()
                    else -> pos
                }

                val newTag = this.addToTag(stackTag["Entity"] as NbtCompound)
                if(newTag.contains("APX")) {
                    newTag.putInt("APX", targetPos.x)
                    newTag.putInt("APY", targetPos.y)
                    newTag.putInt("APZ", targetPos.z)
                }
                val newEntity = EntityType.loadEntityWithPassengers(newTag, context.world) {
                    it.refreshPositionAndAngles(targetPos.x+.5, targetPos.y+.0, targetPos.z+.5, it.yaw, it.pitch)
                    if (!(context.world as ServerWorld).tryLoadEntity(it)) {
                        context.player?.sendMessage(TranslatableText("chat.kibe.lasso.cannot_spawn"), true)
                        null
                    }
                    else it
                }

                if(newEntity != null) {
                    stackTag.remove("Entity")
                    context.stack.nbt = stackTag
                }
            }
            return ActionResult.SUCCESS
        }
        return super.useOnBlock(context)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        if(stack.orCreateNbt.contains("Entity"))
            tooltip.add(TranslatableText("tooltip.kibe.stored").append(TranslatableText("entity."+stack.nbt!!.getCompound("Entity").getString("id").replace(":", "."))))
        super.appendTooltip(stack, world, tooltip, context)
    }

    abstract fun addToTag(tag: NbtCompound): NbtCompound
    abstract fun canStoreEntity(entityType: EntityType<*>): Boolean

    class GoldenLasso(settings: Settings): Lasso(settings) {
        override fun addToTag(tag: NbtCompound): NbtCompound = tag
        override fun canStoreEntity(entityType: EntityType<*>): Boolean = entityType.spawnGroup != SpawnGroup.MONSTER && entityType.spawnGroup != SpawnGroup.MISC
    }

    class CursedLasso(settings: Settings): Lasso(settings) {
        override fun addToTag(tag: NbtCompound): NbtCompound {
            val activeEffect = NbtCompound()
            activeEffect.putInt("Id", Registry.STATUS_EFFECT.getRawId(CURSED_EFFECT))
            activeEffect.putInt("Amplifier", 1)
            activeEffect.putInt("Duration", 999999)
            val activeEffects = if(tag.contains("ActiveEffects")) tag.get("ActiveEffects") as NbtList else NbtList()
            activeEffects.add(activeEffect)
            tag.put("ActiveEffects", activeEffects)
            return tag
        }
        override fun canStoreEntity(entityType: EntityType<*>): Boolean = entityType.spawnGroup == SpawnGroup.MONSTER && entityType != EntityType.ENDER_DRAGON && entityType != EntityType.WITHER
    }

    class DiamondLasso(settings: Settings): Lasso(settings) {
        override fun addToTag(tag: NbtCompound): NbtCompound = tag
        override fun canStoreEntity(entityType: EntityType<*>): Boolean = entityType == EntityType.VILLAGER || (entityType.spawnGroup != SpawnGroup.MISC && entityType != EntityType.ENDER_DRAGON && entityType != EntityType.WITHER)
    }

}