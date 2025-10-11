package io.github.lucaargolo.kibe.item

import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.ToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.particle.ParticleTypes
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.Heightmap
import net.minecraft.world.TeleportTarget
import net.minecraft.world.World

class EscapeRope(settings: Settings): ToolItem(object: ToolMaterial {
    override fun getDurability() = 256
    override fun getMiningSpeedMultiplier() = 0F
    override fun getAttackDamage() = 0F
    override fun getInverseTag() = BlockTags.INCORRECT_FOR_NETHERITE_TOOL
    override fun getEnchantability() = 0
    override fun getRepairIngredient() = Ingredient.ofItems(Items.STRING)
}, settings) {

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = player.getStackInHand(hand)
        player.setCurrentHand(hand)
        return TypedActionResult.success(stack)
    }

    override fun getUseAction(stack: ItemStack): UseAction {
        return UseAction.BOW
    }

    override fun getMaxUseTime(stack: ItemStack, user: LivingEntity): Int {
        return 72000
    }

    override fun onStoppedUsing(stack: ItemStack, world: World, entity: LivingEntity, remainingUseTicks: Int) {
        if(entity !is ServerPlayerEntity || 72000 - remainingUseTicks < 20) return
        if(!world.dimension.hasCeiling) {
            val topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, entity.blockPos.x, entity.blockPos.z)
            val pos = entity.pos.withAxis(Direction.Axis.Y, topY+0.5)
            if(pos.distanceTo(entity.pos) > 2) {
                if(world is ServerWorld) {
                    world.spawnParticles(ParticleTypes.WHITE_SMOKE, pos.x, pos.y, pos.z, 32, 0.0, 1.0, 0.0, 0.01)
                    entity.teleportTo(TeleportTarget(world, pos, entity.getVelocity(), entity.getYaw(), entity.getPitch(), TeleportTarget.NO_OP))
                    entity.onLanding()
                    entity.clearCurrentExplosion()
                    stack.damage(MathHelper.floor(pos.y) - entity.blockPos.y, entity, if (entity.activeHand == Hand.MAIN_HAND) EquipmentSlot.MAINHAND else EquipmentSlot.OFFHAND)
                    world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.PLAYERS)
                }

            }
        }
    }

}