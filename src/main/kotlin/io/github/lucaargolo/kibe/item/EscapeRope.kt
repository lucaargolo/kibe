package io.github.lucaargolo.kibe.item

import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.ToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.Heightmap
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
        if(entity !is PlayerEntity || 72000 - remainingUseTicks < 20) return
        if(!world.isClient && !world.dimension.hasCeiling) {
            val topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, entity.blockPos.x, entity.blockPos.z)
            val pos = entity.blockPos.withY(topY)
            if(pos.getSquaredDistance(entity.blockPos) > 2) {
                stack.damage(pos.y - entity.blockPos.y, entity, if (entity.activeHand == Hand.MAIN_HAND) EquipmentSlot.MAINHAND else EquipmentSlot.OFFHAND)
                entity.requestTeleport(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5)
            }
        }
    }

}