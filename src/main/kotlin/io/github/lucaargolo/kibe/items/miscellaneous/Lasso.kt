package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.effects.CURSED_EFFECT
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

abstract class Lasso(settings: Settings): Item(settings) {

    override fun hasGlint(stack: ItemStack): Boolean {
        return stack.orCreateTag.contains("Entity")
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val d = 4.5
        val hitResult = player.rayTrace(d, 1.0F, false)
        val vec3d = player.getCameraPosVec(1.0f)
        val vec3d2 = player.getRotationVec(1.0f)
        val vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d)
        val box: Box = player.boundingBox.stretch(vec3d2.multiply(d)).expand(1.0, 1.0, 1.0)
        val entityHitResult = ProjectileUtil.rayTrace(player, vec3d, vec3d3, box, { entityx ->
            !entityx.isSpectator && entityx.collides()
        }, d)
        val stack = player.getStackInHand(hand)
        val stackTag = stack.orCreateTag
        if(entityHitResult != null && !stackTag.contains("Entity")) {
            val entity = entityHitResult.entity
            return if(canStoreEntity(entity.type)) {
                val tag = CompoundTag()
                entity.saveSelfToTag(tag)
                stackTag.put("Entity", tag)
                stack.tag = stackTag
                entity.remove()
                TypedActionResult.success(stack)
            }else{
                TypedActionResult.fail(stack)
            }
        }
        if(hitResult.type == HitResult.Type.BLOCK && stackTag.contains("Entity")) {
            if(!world.isClient) {
                val pos = (hitResult as BlockHitResult).blockPos
                val newTag = addToTag(stackTag["Entity"] as CompoundTag)
                val entity = EntityType.loadEntityWithPassengers(newTag, world) {
                    it.refreshPositionAndAngles(pos.x+.0, pos.y+1.0, pos.z+.0, it.yaw, it.pitch)
                    if (!(world as ServerWorld).tryLoadEntity(it)) null else it
                }
                if(entity != null) {
                    stackTag.remove("Entity")
                    stack.tag = stackTag
                }
                if(entity is MobEntity) {
                    entity.initialize(world, world.getLocalDifficulty(BlockPos(entity.pos)), SpawnReason.NATURAL, null, null)
                }
                return TypedActionResult.success(stack)
            }else{
                return TypedActionResult.consume(stack)
            }
        }
        return TypedActionResult.pass(player.getStackInHand(hand))
    }

    abstract fun addToTag(tag: CompoundTag): CompoundTag
    abstract fun canStoreEntity(entityType: EntityType<*>): Boolean

    class GoldenLasso(settings: Settings): Lasso(settings) {
        override fun addToTag(tag: CompoundTag): CompoundTag = tag
        override fun canStoreEntity(entityType: EntityType<*>): Boolean = entityType.spawnGroup.isAnimal
    }

    class CursedLasso(settings: Settings): Lasso(settings) {
        override fun addToTag(tag: CompoundTag): CompoundTag {
            val activeEffect = CompoundTag()
            activeEffect.putInt("Id", Registry.STATUS_EFFECT.getRawId(CURSED_EFFECT))
            activeEffect.putInt("Amplifier", 1)
            activeEffect.putInt("Duration", 999999)
            val activeEffects = if(tag.contains("ActiveEffects")) tag.get("ActiveEffects") as ListTag else ListTag()
            activeEffects.add(activeEffect)
            tag.put("ActiveEffects", activeEffects)
            return tag
        }
        override fun canStoreEntity(entityType: EntityType<*>): Boolean = entityType.spawnGroup == SpawnGroup.MONSTER
    }

    class DiamondLasso(settings: Settings): Lasso(settings) {
        override fun addToTag(tag: CompoundTag): CompoundTag = tag
        override fun canStoreEntity(entityType: EntityType<*>): Boolean = entityType.spawnGroup.isAnimal || entityType.spawnGroup == SpawnGroup.MONSTER
    }

}