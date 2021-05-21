package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity;
import net.minecraft.world.chunk.light.BlockLightStorage;
import net.minecraft.world.chunk.light.SkyLightStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SkyLightStorage.class, BlockLightStorage.class})
public class LightStorageMixin {

    @Inject(at = @At("HEAD"), method = "getLight", cancellable = true)
    public void getLight(long blockPos, CallbackInfoReturnable<Integer> cir) {
        if(BigTorchBlockEntity.Companion.isTesting() && Thread.currentThread() == BigTorchBlockEntity.Companion.getTestingThread()) {
            cir.setReturnValue(15);
        }
    }

}
