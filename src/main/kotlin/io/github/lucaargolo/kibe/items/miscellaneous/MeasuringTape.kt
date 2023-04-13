package io.github.lucaargolo.kibe.items.miscellaneous

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.abs

class MeasuringTape(settings: Settings) : Item(settings) {
    companion object {
        private const val MEASURING_FROM_LEVEL = "MeasuringFromLevel"
        private const val MEASURING_FROM_X = "MeasuringFromX"
        private const val MEASURING_FROM_Y = "MeasuringFromY"
        private const val MEASURING_FROM_Z = "MeasuringFromZ"
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val player = context.player ?: return super.useOnBlock(context)
        val world = context.world
        val pos = context.blockPos.toImmutable()

        val measuringFrom = measuringFrom(context.stack)
        if (measuringFrom == null) {
            startMeasuring(context.stack, world, pos)
            if (!world.isClient) {
                player.sendMessage(Text.translatable("chat.kibe.measuring_tape.start", pos.x, pos.y, pos.z).formatted(Formatting.GREEN, Formatting.ITALIC), true)
            }
        } else {
            finishMeasuring(measuringFrom, player, pos, context.stack)
        }

        return ActionResult.success(world.isClient)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val measuringFrom = measuringFrom(stack)
        if (measuringFrom == null) {
            tooltip += Text.translatable("tooltip.kibe.lore.measuring_tape.start").formatted(Formatting.GRAY, Formatting.ITALIC)
        } else {
            tooltip += Text.translatable("tooltip.kibe.lore.measuring_tape.measuring_1", measuringFrom.second.x, measuringFrom.second.y, measuringFrom.second.z).formatted(Formatting.GRAY, Formatting.ITALIC)
            tooltip += Text.translatable("tooltip.kibe.lore.measuring_tape.measuring_2").formatted(Formatting.GRAY, Formatting.ITALIC)
            tooltip += Text.translatable("tooltip.kibe.lore.measuring_tape.measuring_3").formatted(Formatting.GRAY, Formatting.ITALIC)
        }
        if (context.isAdvanced && measuringFrom != null) {
            tooltip += Text.of(" ${measuringFrom.first}")
        }
    }

    private fun finishMeasuring(
        measuringFrom: Pair<Identifier, BlockPos>,
        player: PlayerEntity,
        clicked: BlockPos,
        stack: ItemStack
    ) {
        val (fromLevel, fromPos) = measuringFrom
        if (fromLevel != player.world.dimensionKey.value && !player.world.isClient) {
            player.sendMessage(Text.translatable("chat.kibe.measuring_tape.measuring_between_dimensions").formatted(Formatting.YELLOW))
        }
        val distX = abs(fromPos.x - clicked.x) + 1
        val distY = abs(fromPos.y - clicked.y) + 1
        val distZ = abs(fromPos.z - clicked.z) + 1
        if (!player.world.isClient) {
            player.sendMessage(Text.translatable("chat.kibe.measuring_tape.result", distX, distY, distZ).formatted(Formatting.GREEN))
        }
        if (!player.isSneaking) {
            stack.nbt?.remove(MEASURING_FROM_LEVEL)
            stack.nbt?.remove(MEASURING_FROM_X)
            stack.nbt?.remove(MEASURING_FROM_Y)
            stack.nbt?.remove(MEASURING_FROM_Z)
        }
    }

    private fun measuringFrom(stack: ItemStack): Pair<Identifier, BlockPos>? {
        val nbt = stack.nbt ?: return null
        if (MEASURING_FROM_LEVEL !in nbt || MEASURING_FROM_X !in nbt || MEASURING_FROM_Y !in nbt || MEASURING_FROM_Z !in nbt) {
            return null
        }
        val x = nbt.getInt(MEASURING_FROM_X)
        val y = nbt.getInt(MEASURING_FROM_Y)
        val z = nbt.getInt(MEASURING_FROM_Z)
        val level = nbt.getString(MEASURING_FROM_LEVEL)

        return Identifier(level) to BlockPos(x, y, z).toImmutable()
    }

    private fun startMeasuring(stack: ItemStack, world: World, pos: BlockPos) {
        val nbt = stack.orCreateNbt
        nbt.putInt(MEASURING_FROM_X, pos.x)
        nbt.putInt(MEASURING_FROM_Y, pos.y)
        nbt.putInt(MEASURING_FROM_Z, pos.z)
        nbt.putString(MEASURING_FROM_LEVEL, world.dimensionKey.value.toString())
    }
}
