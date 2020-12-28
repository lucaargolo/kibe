package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.entities.miscellaneous.ThrownTorchEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World


class TorchSling(settings: Settings): Item(settings) {

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
        val player = (entity as? PlayerEntity) ?: return
        val tickStrength = ((this.getMaxUseTime(stack) - remainingUseTicks) / 20f).coerceAtLeast(0.5f).coerceAtMost(1.5f)

        val torchStack = player.inventory.main.firstOrNull { it.item == Items.TORCH || it.item == Items.SOUL_TORCH || it.item == Items.REDSTONE_TORCH }

        torchStack?.let {
            world.playSound(null, entity.x, entity.y, entity.z, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (RANDOM.nextFloat() * 0.4f + 0.8f))
            if (!world.isClient) {
                val thrownTorchEntity = ThrownTorchEntity(world, entity)
                thrownTorchEntity.setItem(ItemStack(it.item))
                thrownTorchEntity.setProperties(entity, entity.pitch, entity.yaw, 0f, tickStrength, 0f)
                world.spawnEntity(thrownTorchEntity)
                it.decrement(1)
            }
        }

    }

}