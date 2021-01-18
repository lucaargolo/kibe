package io.github.lucaargolo.kibe.utils

import com.mojang.authlib.GameProfile
import io.github.lucaargolo.kibe.FAKE_PLAYER_UUID
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FakePlayerEntity(world: World): PlayerEntity(world, BlockPos.ORIGIN, 0f, GameProfile(FAKE_PLAYER_UUID, "fake")) {

    override fun isSpectator() = true
    override fun isCreative() = true

    override fun damage(source: DamageSource?, amount: Float) = false

}