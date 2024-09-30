package io.github.lucaargolo.kibe.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.ClampedModelPredicateProvider
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.network.codec.PacketCodec
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.abs
import kotlin.math.roundToInt

class MeasuringTape(settings: Settings) : Item(settings) {

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if(user.mainHandStack == stack || user.mainHandStack.isEmpty) {
            val reach = if (user.isCreative) 5.0 else 4.5
            val pos = (user.raycast(reach, 1f, true) as? BlockHitResult)?.blockPos ?: return TypedActionResult.pass(stack)
            if(!world.isClient) {
                val measuringFrom = measuringFrom(stack)
                val measuringTo = measuringTo(stack)
                if (measuringFrom == null) {
                    startMeasuring(stack, world, pos)
                } else {
                    finishMeasuring(measuringFrom, measuringTo, user, pos, stack)
                }
                return TypedActionResult.success(stack)
            }
        }
        return TypedActionResult.pass(stack)
    }

    override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
        super.appendTooltip(stack, context, tooltip, type)
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
        if (type == TooltipType.ADVANCED && measuringFrom != null) {
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

    companion object {

        fun measuringFrom(stack: ItemStack): Pair<Identifier, BlockPos>? {
            val data = stack.get(ComponentTypeCompendium.MEASURING_FROM) ?: return null
            return data.measuringLevel to data.measuring.toImmutable()
        }

        fun measuringTo(stack: ItemStack): Pair<Identifier, BlockPos>? {
            val data = stack.get(ComponentTypeCompendium.MEASURING_TO) ?: return null
            return data.measuringLevel to data.measuring.toImmutable()
        }

        fun startMeasuring(stack: ItemStack, world: World, pos: BlockPos) {
            stack.remove(ComponentTypeCompendium.MEASURING_TO)
            stack.set(ComponentTypeCompendium.MEASURING_FROM, MeasuringData(world.registryKey.value, pos))
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
                if (fromLevel != player.world.registryKey.value) {
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
                    stack?.remove(ComponentTypeCompendium.MEASURING_FROM)
                    stack?.remove(ComponentTypeCompendium.MEASURING_TO)
                } else {
                    if (toPos != null) {
                        stack?.remove(ComponentTypeCompendium.MEASURING_TO)
                    } else {
                        stack?.set(ComponentTypeCompendium.MEASURING_TO, MeasuringData(fromLevel, lookPos))
                    }
                }
            }
        }

        private fun measureResult(fromPos: BlockPos, toPos: BlockPos): String {
            val unit = Text.translatable("chat.kibe.measuring_tape.unit").string
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

    data class MeasuringData(val measuringLevel: Identifier, val measuring: BlockPos) {

        companion object {

            val CODEC: Codec<MeasuringData> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Identifier.CODEC.fieldOf("measuringLevel").forGetter(MeasuringData::measuringLevel),
                    BlockPos.CODEC.fieldOf("measuring").forGetter(MeasuringData::measuring),
                ).apply(instance, ::MeasuringData)
            }

            val PACKET_CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, MeasuringData::measuringLevel,
                BlockPos.PACKET_CODEC, MeasuringData::measuring,
            ::MeasuringData)
        }

    }

    class PredicateProvider: ClampedModelPredicateProvider {

        override fun unclampedCall(stack: ItemStack, world: ClientWorld?, entity: LivingEntity?, seed: Int): Float {
            if (stack.contains(ComponentTypeCompendium.MEASURING_FROM)) {
                return 1f
            }
            return 0f
        }

    }

}
