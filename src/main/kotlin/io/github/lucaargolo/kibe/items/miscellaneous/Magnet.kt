package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.MOD_CONFIG
import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.TRINKET
import io.github.lucaargolo.kibe.compat.trinkets.TrinketMagnet
import net.minecraft.entity.Entity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

open class Magnet(settings: Settings) : BooleanItem(settings) {

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        val player = entity as? PlayerEntity ?: return
        if (!isEnabled(stack) || world.isClient) return
        val pos = player.blockPos
        val target = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        val areaOfEffect = Box.from(target).expand(MOD_CONFIG.miscellaneousModule.magnetRange)

        if (world.getStatesInBox(areaOfEffect).anyMatch { it.block.registryEntry.isIn(MAGNET_INHIBITOR_TAG) }) return

        world.getOtherEntities(player, areaOfEffect) { ((it is ItemEntity && !it.cannotPickup()) || it is ExperienceOrbEntity) }
            .forEach {
                val vel = it.pos.relativize(target).normalize().multiply(0.1)
                it.addVelocity(vel.x, vel.y, vel.z)
            }
    }

    companion object {

        val MAGNET_INHIBITOR_TAG = TagKey.of(Registry.BLOCK_KEY, Identifier(MOD_ID, "magnet_inhibitor"))
        fun create(settings: Settings): Magnet = if (TRINKET) TrinketMagnet(settings) else Magnet(settings)
    }
}