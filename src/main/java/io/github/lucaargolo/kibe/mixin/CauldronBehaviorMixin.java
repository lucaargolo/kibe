package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.items.ItemCompendiumKt;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(CauldronBehavior.class)
public interface CauldronBehaviorMixin {

    @Inject(at = @At("TAIL"), method = "registerBucketBehavior")
    private static void registerWoodenBucketBehavior(Map<Item, CauldronBehavior> behavior, CallbackInfo ci) {
        behavior.put(ItemCompendiumKt.getWATER_WOODEN_BUCKET(), (state, world, pos, player, hand, stack) ->
           fillCauldronWithWoodenBucket(world, pos, player, hand, stack, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3), SoundEvents.ITEM_BUCKET_EMPTY)
        );
    }

    @Inject(at = @At("TAIL"), method = "registerBehavior")
    private static void registerWoodenBucketBehavior(CallbackInfo ci) {
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(ItemCompendiumKt.getWOODEN_BUCKET(), (state, world, pos, player, hand, stack) ->
            CauldronBehavior.emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(ItemCompendiumKt.getWATER_WOODEN_BUCKET()), (statex) -> statex.get(LeveledCauldronBlock.LEVEL) == 3, SoundEvents.ITEM_BUCKET_FILL)
        );
    }

    private static ActionResult fillCauldronWithWoodenBucket(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, BlockState state, SoundEvent soundEvent) {
        if (!world.isClient) {
            Item item = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(ItemCompendiumKt.getWOODEN_BUCKET())));
            player.incrementStat(Stats.FILL_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            world.setBlockState(pos, state);
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        }

        return ActionResult.success(world.isClient);
    }

}
