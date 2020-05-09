package io.github.lucaargolo.kibe.utils

import com.mojang.authlib.GameProfile
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World
import java.util.*

val FAKE_PLAYER_UUID: UUID = UUID.randomUUID()

class FakePlayerEntity(world: World): PlayerEntity(world, GameProfile(FAKE_PLAYER_UUID, "fake")) {

    override fun isSpectator() = true
    override fun isCreative() = true

}