
package io.github.lucaargolo.kibe.mixin;

import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockRenderView.class)
public interface BlockRenderViewMixin {

//    @Inject(at = @At("HEAD"), method = "getLightLevel", cancellable = true)
//    default void getLight(LightType type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
//        if(type == LightType.BLOCK && BigTorchBlockEntity.Companion.isTesting() && Thread.currentThread() == BigTorchBlockEntity.Companion.getTestingThread()) {
//            cir.setReturnValue(15);
//        }
//    }
//
//    @Inject(at = @At("HEAD"), method = "getBaseLightLevel", cancellable = true)
//    default void getLight(BlockPos pos, int ambientDarkness, CallbackInfoReturnable<Integer> cir) {
//        if(BigTorchBlockEntity.Companion.isTesting() && Thread.currentThread() == BigTorchBlockEntity.Companion.getTestingThread()) {
//            cir.setReturnValue(15);
//        }
//    }

}
