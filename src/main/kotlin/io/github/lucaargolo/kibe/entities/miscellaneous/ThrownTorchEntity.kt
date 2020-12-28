package io.github.lucaargolo.kibe.entities.miscellaneous

import io.github.lucaargolo.kibe.entities.THROWN_TORCH
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.*
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

class ThrownTorchEntity: ThrownItemEntity {

    constructor(entityType: EntityType<ThrownTorchEntity>, world: World): super(entityType, world)

    constructor(world: World, owner: LivingEntity): super(THROWN_TORCH, owner, world)

    constructor(world: World, x: Double, y: Double, z: Double): super(THROWN_TORCH, x, y, z, world)

    override fun getDefaultItem(): Item = Items.TORCH

    override fun onBlockHit(blockHitResult: BlockHitResult) {
        val playerEntity = (owner as? PlayerEntity) ?: return

        val hitPos = blockHitResult.blockPos
        val hitSide = blockHitResult.side
        val finalPos = hitPos.add(hitSide.vector)

        if((stack.item as? BlockItem)?.place(ItemPlacementContext(playerEntity, Hand.MAIN_HAND, stack, blockHitResult))?.isAccepted != true) {
            ItemScatterer.spawn(world, finalPos.x + 0.0, finalPos.y + 0.0, finalPos.z + 0.0, ItemStack(stack.item))
        }
        super.onBlockHit(blockHitResult)
    }

    override fun onCollision(hitResult: HitResult?) {
        super.onCollision(hitResult)
        if (!world.isClient) {
            this.remove()
        }
    }


}