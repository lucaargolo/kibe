package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.MOD_ID
import io.github.lucaargolo.kibe.items.IdentifiedModelPredicateProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.resource.language.I18n
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.Pair
import kotlin.math.abs
import kotlin.math.roundToInt

class MeasuringTape(settings: Settings) : Item(settings) {
    companion object {
        const val MEASURING_LEVEL = "MeasuringLevel"

        const val MEASURING_FROM_X = "MeasuringFromX"
        const val MEASURING_FROM_Y = "MeasuringFromY"
        const val MEASURING_FROM_Z = "MeasuringFromZ"

        const val MEASURING_TO_X = "MeasuringToX"
        const val MEASURING_TO_Y = "MeasuringToY"
        const val MEASURING_TO_Z = "MeasuringToZ"

        fun measuringFrom(stack: ItemStack): Pair<Identifier, BlockPos>? {
            val nbt = stack.nbt ?: return null
            if (MEASURING_LEVEL !in nbt || MEASURING_FROM_X !in nbt || MEASURING_FROM_Y !in nbt || MEASURING_FROM_Z !in nbt) {
                return null
            }
            val x = nbt.getInt(MEASURING_FROM_X)
            val y = nbt.getInt(MEASURING_FROM_Y)
            val z = nbt.getInt(MEASURING_FROM_Z)
            val level = nbt.getString(MEASURING_LEVEL)

            return Identifier(level) to BlockPos(x, y, z).toImmutable()
        }

        fun measuringTo(stack: ItemStack): Pair<Identifier, BlockPos>? {
            val nbt = stack.nbt ?: return null
            if (MEASURING_LEVEL !in nbt || MEASURING_TO_X !in nbt || MEASURING_TO_Y !in nbt || MEASURING_TO_Z !in nbt) {
                return null
            }
            val x = nbt.getInt(MEASURING_TO_X)
            val y = nbt.getInt(MEASURING_TO_Y)
            val z = nbt.getInt(MEASURING_TO_Z)
            val level = nbt.getString(MEASURING_LEVEL)

            return Identifier(level) to BlockPos(x, y, z).toImmutable()
        }

        fun startMeasuring(stack: ItemStack, world: World, pos: BlockPos) {
            val nbt = stack.orCreateNbt
            nbt.remove(MEASURING_TO_X)
            nbt.remove(MEASURING_TO_Y)
            nbt.remove(MEASURING_TO_Z)
            nbt.putInt(MEASURING_FROM_X, pos.x)
            nbt.putInt(MEASURING_FROM_Y, pos.y)
            nbt.putInt(MEASURING_FROM_Z, pos.z)
            nbt.putString(MEASURING_LEVEL, world.dimensionKey.value.toString())
        }

        fun finishMeasuring(
            measuringFrom: Pair<Identifier, BlockPos>,
            measuringTo: Pair<Identifier, BlockPos>?,
            player: PlayerEntity,
            lookPos: BlockPos,
            stack: ItemStack?
        ) {
            val (fromLevel, fromPos) = measuringFrom
            val toPos = measuringTo?.second
            if(!player.isSneaking) {
                if (fromLevel != player.world.dimensionKey.value) {
                    player.sendMessage(Text.translatable("chat.kibe.measuring_tape.measuring_between_dimensions").formatted(Formatting.RED), true)
                } else {
                    if (toPos != null) {
                        player.sendMessage(Text.translatable("chat.kibe.measuring_tape.result", measureResult(fromPos, toPos)).formatted(Formatting.YELLOW), true)
                    } else {
                        player.sendMessage(Text.translatable("chat.kibe.measuring_tape.result", measureResult(fromPos, lookPos)).formatted(Formatting.BLUE), true)
                    }
                }
            }
            if (!player.world.isClient) {
                if (player.isSneaking) {
                    player.sendMessage(Text.translatable("chat.kibe.measuring_tape.clear").formatted(Formatting.RED, Formatting.ITALIC), true)
                    stack?.nbt?.remove(MEASURING_LEVEL)
                    stack?.nbt?.remove(MEASURING_FROM_X)
                    stack?.nbt?.remove(MEASURING_FROM_Y)
                    stack?.nbt?.remove(MEASURING_FROM_Z)
                    stack?.nbt?.remove(MEASURING_TO_X)
                    stack?.nbt?.remove(MEASURING_TO_Y)
                    stack?.nbt?.remove(MEASURING_TO_Z)
                } else {
                    if (toPos != null) {
                        stack?.nbt?.remove(MEASURING_TO_X)
                        stack?.nbt?.remove(MEASURING_TO_Y)
                        stack?.nbt?.remove(MEASURING_TO_Z)
                    } else {
                        stack?.nbt?.putInt(MEASURING_TO_X, lookPos.x)
                        stack?.nbt?.putInt(MEASURING_TO_Y, lookPos.y)
                        stack?.nbt?.putInt(MEASURING_TO_Z, lookPos.z)
                    }
                }
            }
        }

        private fun measureResult(fromPos: BlockPos, toPos: BlockPos): String {
            val unit = I18n.translate("chat.kibe.measuring_tape.unit")
            val dist = (Vec3d.ofCenter(fromPos).distanceTo(Vec3d.ofCenter(toPos)) * 100.0).roundToInt() / 100.0
            val distX = abs(fromPos.x - toPos.x) + 1.0
            val distY = abs(fromPos.y - toPos.y) + 1.0
            val distZ = abs(fromPos.z - toPos.z) + 1.0
            var result = "${dist}m"
            if(distX > 1.0) {
                result += " (x: $distX $unit)"
            }
            if(distY > 1.0) {
                result += " (y: $distY $unit)"
            }
            if(distZ > 1.0) {
                result += " (z: $distZ $unit)"
            }
            return result
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if(!world.isClient && user.isSneaking && user.mainHandStack.isEmpty) {
            user.sendMessage(Text.translatable("chat.kibe.measuring_tape.clear").formatted(Formatting.RED, Formatting.ITALIC), true)
            stack.nbt?.remove(MEASURING_LEVEL)
            stack.nbt?.remove(MEASURING_FROM_X)
            stack.nbt?.remove(MEASURING_FROM_Y)
            stack.nbt?.remove(MEASURING_FROM_Z)
            stack?.nbt?.remove(MEASURING_TO_X)
            stack?.nbt?.remove(MEASURING_TO_Y)
            stack?.nbt?.remove(MEASURING_TO_Z)
            return TypedActionResult.success(stack)
        }
        return super.use(world, user, hand)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val player = context.player ?: return super.useOnBlock(context)
        val world = context.world
        val pos = context.blockPos.toImmutable()

        if (!world.isClient) {
            val measuringFrom = measuringFrom(context.stack)
            val measuringTo = measuringTo(context.stack)
            if (measuringFrom == null) {
                startMeasuring(context.stack, world, pos)
            } else {
                finishMeasuring(measuringFrom, measuringTo, player, pos, context.stack)
            }
        }

        return ActionResult.success(world.isClient)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val measuringFrom = measuringFrom(stack)
        val measuringTo = measuringTo(stack)
        if (measuringFrom == null) {
            tooltip += Text.translatable("tooltip.kibe.lore.measuring_tape.start").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC)
        } else {
            tooltip += if(measuringTo != null) {
                Text.translatable("tooltip.kibe.lore.measuring_tape.measuring_to", measuringFrom.second.x, measuringFrom.second.y, measuringFrom.second.z, measuringTo.second.x, measuringTo.second.y, measuringTo.second.z).formatted(Formatting.YELLOW, Formatting.ITALIC)
            }else{
                Text.translatable("tooltip.kibe.lore.measuring_tape.measuring_from", measuringFrom.second.x, measuringFrom.second.y, measuringFrom.second.z).formatted(Formatting.BLUE, Formatting.ITALIC)
            }
            tooltip += Text.translatable("tooltip.kibe.lore.measuring_tape.measuring_2").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC)
            tooltip += Text.translatable("tooltip.kibe.lore.measuring_tape.measuring_3").formatted(Formatting.RED, Formatting.ITALIC)
        }
        if (context.isAdvanced && measuringFrom != null) {
            tooltip += Text.of(" ${measuringFrom.first}")
        }
    }

    override fun hasGlint(stack: ItemStack): Boolean {
        return measuringTo(stack) != null
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if(world.isClient && selected) {
            val client = MinecraftClient.getInstance()
            val player = client.player ?: return
            val pos = (client.crosshairTarget as? BlockHitResult)?.blockPos ?: return
            val measuringFrom = measuringFrom(stack)
            val measuringTo = measuringTo(stack)
            if(measuringFrom != null) {
                finishMeasuring(measuringFrom, measuringTo, player, pos, null)
            }
        }
    }

    object ModelPredicateProvider : IdentifiedModelPredicateProvider {
        override fun unclampedCall(stack: ItemStack, world: ClientWorld?, entity: LivingEntity?, seed: Int): Float {
            val nbt = stack.nbt ?: return 0f
            if (MEASURING_LEVEL in nbt) {
                return 1f
            }
            return 0f
        }

        override val identifier: Identifier
            get() = Identifier(MOD_ID, "extended")
    }
}
