package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.utils.SlimeBounceHandler
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RaycastContext
import net.minecraft.world.World

/*
    Code adapted from Tinkers Construct
    Available at: https://github.com/SlimeKnights/TinkersConstruct/blob/c01173c0408352c50a2e8c5017552323ce42f5b4/src/main/java/slimeknights/tconstruct/gadgets/item/ItemSlimeSling.java
    Licensed under the MIT license available at: https://tldrlegal.com/license/mit-license
*/
class SlimeSling(settings: Settings): Item(settings) {

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = player.getStackInHand(hand)
        player.setCurrentHand(hand)
        return TypedActionResult.success(stack)
    }

    override fun getUseAction(stack: ItemStack): UseAction {
        return UseAction.BOW
    }

    override fun getMaxUseTime(stack: ItemStack): Int {
        return 72000
    }

    override fun onStoppedUsing(stack: ItemStack, world: World, entity: LivingEntity, remainingUseTicks: Int) {
        if(entity !is PlayerEntity) return

        if(!entity.isOnGround) {
            return
        }

        val i: Int = this.getMaxUseTime(stack) - remainingUseTicks
        var f = i / 20.0f
        f = (f * f + f * 2.0f) / 3.0f
        f *= 4f

        if (f > 6f) {
            f = 6f
        }

        val mop = raycast(world, entity, RaycastContext.FluidHandling.NONE)

        if(mop != null && mop.type == HitResult.Type.BLOCK) {

            val vec = entity.getRotationVec(1.0f).normalize()

            entity.addVelocity(vec.x * -f, vec.y * -f/3f, vec.z * -f)

            if(entity is ServerPlayerEntity) {
               entity.networkHandler.sendPacket(EntityVelocityUpdateS2CPacket(entity))
            }

            SlimeBounceHandler.addBounceHandler(entity, 0.0)
        }


    }

}