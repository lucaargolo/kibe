package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.effect.EffectCompendium
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.Direction

abstract class Lasso(settings: Settings): Item(settings) {

    override fun hasGlint(stack: ItemStack): Boolean {
        return stack.contains(DataComponentTypes.ENTITY_DATA)
    }

    override fun useOnEntity(stack: ItemStack, user: PlayerEntity, entity: LivingEntity, hand: Hand): ActionResult {
        if (!stack.contains(DataComponentTypes.ENTITY_DATA)) {
            if (entity is MobEntity && canStoreEntity(entity.type) && !KibeMod.CONFIG.miscellaneousModule.lassoDenyList.contains(Registries.ENTITY_TYPE.getId(entity.type).toString())) {
                if(!user.world.isClient) {
                    if (entity.isLeashed) entity.detachLeash(true, true)
                    entity.fallDistance = 0f
                    val tag = NbtCompound()
                    entity.saveSelfNbt(tag)
                    stack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(tag))
                    entity.remove(Entity.RemovalReason.DISCARDED)
                }
                return ActionResult.SUCCESS
            }
        }
        return ActionResult.PASS
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if(context.stack.contains(DataComponentTypes.ENTITY_DATA)) {
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

                val newTag = this.addToTag(context.stack.get(DataComponentTypes.ENTITY_DATA)!!.copyNbt())
                if(newTag.contains("APX")) {
                    newTag.putInt("APX", targetPos.x)
                    newTag.putInt("APY", targetPos.y)
                    newTag.putInt("APZ", targetPos.z)
                }
                val newEntity = EntityType.loadEntityWithPassengers(newTag, context.world) {
                    it.refreshPositionAndAngles(targetPos.x+.5, targetPos.y+.0, targetPos.z+.5, it.yaw, it.pitch)
                    if (!(context.world as ServerWorld).tryLoadEntity(it)) {
                        context.player?.sendMessage(Text.translatable("chat.kibe.lasso.cannot_spawn"), true)
                        null
                    }
                    else it
                }

                if(newEntity != null) {
                    context.stack.remove(DataComponentTypes.ENTITY_DATA)
                }
            }
            return ActionResult.SUCCESS
        }
        return super.useOnBlock(context)
    }

    override fun appendTooltip(stack: ItemStack, context: TooltipContext?, tooltip: MutableList<Text>, type: TooltipType?) {
        super.appendTooltip(stack, context, tooltip, type)
        val entity = stack.get(DataComponentTypes.ENTITY_DATA)?.copyNbt()?.getString("id")?.replace(":", ".") ?: return
        tooltip.add(Text.translatable("tooltip.kibe.stored").append(Text.translatable("entity.$entity")))
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
            activeEffect.putInt("Id", Registries.STATUS_EFFECT.getRawId(EffectCompendium.CURSED.value()))
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