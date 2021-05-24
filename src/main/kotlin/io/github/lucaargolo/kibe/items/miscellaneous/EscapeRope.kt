package io.github.lucaargolo.kibe.items.miscellaneous

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.ToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World

class EscapeRope(settings: Settings): ToolItem(object: ToolMaterial {
    override fun getDurability() = 256
    override fun getMiningSpeedMultiplier() = 0F
    override fun getAttackDamage() = 0F
    override fun getMiningLevel() = 0
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

    override fun getMaxUseTime(stack: ItemStack): Int {
        return 72000
    }

    override fun onStoppedUsing(stack: ItemStack, world: World, entity: LivingEntity, remainingUseTicks: Int) {
        if(entity !is PlayerEntity) return
        var pos = entity.blockPos
        while (!world.isSkyVisible(pos) && pos.y < world.dimension.logicalHeight-2) {
            pos = pos.up()
        }
        if(pos.y != entity.blockPos.y && world.getBlockState(pos.up()).isAir && world.getBlockState(pos.up().up()).isAir) {
            stack.damage(pos.y-entity.blockPos.y, entity) {
                it.sendToolBreakStatus(it.activeHand)
            }
            entity.teleport(pos.x+0.5, pos.y+0.0, pos.z+0.5)
        }
    }

}