package io.github.lucaargolo.kibe.item

import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.compat.TrinketMagnet
import io.github.lucaargolo.kibe.utils.ModIdentifier
import net.minecraft.entity.Entity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

open class Magnet(settings: Settings) : BooleanItem(settings) {

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        val player = entity as? PlayerEntity ?: return
        if (!isEnabled(stack) || world.isClient) return
        val pos = player.blockPos
        val target = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        val areaOfEffect = Box.from(target).expand(KibeMod.CONFIG.miscellaneousModule.magnetRange)

        @Suppress("DEPRECATION")
        if (world.getStatesInBox(areaOfEffect).anyMatch { it.block.registryEntry.isIn(MAGNET_INHIBITOR_TAG) }) return

        world.getOtherEntities(player, areaOfEffect) { ((it is ItemEntity && !it.cannotPickup()) || it is ExperienceOrbEntity) }
            .forEach {
                val vel = it.pos.relativize(target).normalize().multiply(0.1)
                it.addVelocity(vel.x, vel.y, vel.z)
            }
    }

    companion object {

        val MAGNET_INHIBITOR_TAG = TagKey.of(RegistryKeys.BLOCK, ModIdentifier.of("magnet_inhibitor"))
        fun create(settings: Settings): Magnet = if (KibeMod.TRINKET) TrinketMagnet(settings) else Magnet(settings)
    }
}