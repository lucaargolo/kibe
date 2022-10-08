
package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRenderView.class)
public interface BlockRenderViewMixin {

    @Inject(at = @At("HEAD"), method = "getLightLevel", cancellable = true)
    default void getLight(LightType type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if(type == LightType.BLOCK && BigTorchBlockEntity.Companion.isTesting() && Thread.currentThread() == BigTorchBlockEntity.Companion.getTestingThread()) {
            cir.setReturnValue(15);
        }
    }

    @Inject(at = @At("HEAD"), method = "getBaseLightLevel", cancellable = true)
    default void getLight(BlockPos pos, int ambientDarkness, CallbackInfoReturnable<Integer> cir) {
        if(BigTorchBlockEntity.Companion.isTesting() && Thread.currentThread() == BigTorchBlockEntity.Companion.getTestingThread()) {
            cir.setReturnValue(15);
        }
    }

}
