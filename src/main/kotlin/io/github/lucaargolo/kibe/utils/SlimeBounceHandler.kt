package io.github.lucaargolo.kibe.utils

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class SlimeBounceHandler(entityLiving: LivingEntity, var bounce: Double) {

    var timer = 0
    var wasInAir = false
    var bounceTick = if (bounce != 0.0) entityLiving.age else 0

    init {
        if(entityLiving is ServerPlayerEntity) serverBouncingEntities[entityLiving] = this
        if(entityLiving is ClientPlayerEntity) clientBouncingEntities[entityLiving] = this
    }

    var lastMovX: Double = 0.0
    var lastMovZ: Double = 0.0

    companion object {

        var serverBouncingEntities: IdentityHashMap<Entity, SlimeBounceHandler> = IdentityHashMap<Entity, SlimeBounceHandler>()
        var clientBouncingEntities: IdentityHashMap<Entity, SlimeBounceHandler> = IdentityHashMap<Entity, SlimeBounceHandler>()

        fun addBounceHandler(entity: LivingEntity, bounce: Double) {
            if (entity !is PlayerEntity) {
                return
            }
            val sHandler = serverBouncingEntities[entity]
            val cHandler = clientBouncingEntities[entity]
            if (sHandler == null || cHandler == null) {
                SlimeBounceHandler(entity, bounce)
            } else if (bounce != 0.0) {
                sHandler.bounce = bounce
                sHandler.bounceTick = entity.age
                cHandler.bounce = bounce
                cHandler.bounceTick = entity.age
            }
        }

    }

}