package io.github.lucaargolo.kibe.utils

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity


object GliderHelper {

    private val gliderSet = linkedSetOf<PlayerEntity>()
    private val playerRenderPopSet = linkedSetOf<PlayerEntity>()

    fun isPlayerGliding(playerEntity: PlayerEntity) = gliderSet.contains(playerEntity)

    fun setPlayerGliding(playerEntity: PlayerEntity, boolean: Boolean) {
        if(boolean) gliderSet.add(playerEntity) else gliderSet.remove(playerEntity)
    }

    fun needsPlayerRenderPopping(playerEntity: PlayerEntity) = playerRenderPopSet.contains(playerEntity)

    fun setPlayerRenderPoppingNeeds(playerEntity: PlayerEntity, boolean: Boolean) {
        if(boolean) playerRenderPopSet.add(playerEntity) else playerRenderPopSet.remove(playerEntity)
    }


}