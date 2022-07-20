
package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightingProvider.class)
public class LightingProviderMixin {

    @Inject(at = @At("HEAD"), method = "getLight", cancellable = true)
    public void getLight(BlockPos pos, int ambientDarkness, CallbackInfoReturnable<Integer> cir) {
        if(BigTorchBlockEntity.Companion.isTesting() && Thread.currentThread() == BigTorchBlockEntity.Companion.getTestingThread()) {
            cir.setReturnValue(15);
        }
    }

}
