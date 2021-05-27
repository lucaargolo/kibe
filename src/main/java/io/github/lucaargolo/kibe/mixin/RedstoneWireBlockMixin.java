package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.BlockCompendiumKt;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {

    @Inject(method = "canRunOnTop", at = @At("HEAD"), cancellable = true)
    private void allowWireOnFluidHoppers(BlockView world, BlockPos pos, BlockState floor, CallbackInfoReturnable<Boolean> ci) {
        if (floor.isOf(BlockCompendiumKt.getFLUID_HOPPER())) {
            ci.setReturnValue(true);
        }
    }

}
