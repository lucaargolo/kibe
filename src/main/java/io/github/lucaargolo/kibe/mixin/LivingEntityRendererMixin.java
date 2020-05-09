package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.KibeModKt;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {

    @Inject(at = @At("HEAD"), method = "getRenderLayer", cancellable = true)
    private void getRenderLayer(T entity, boolean showBody, boolean translucent, CallbackInfoReturnable<RenderLayer> info) {
        if(entity.hasStatusEffect(KibeModKt.getCursedEffect())) {
            info.setReturnValue(RenderLayer.getEndPortal(1));
        }
    }

}
