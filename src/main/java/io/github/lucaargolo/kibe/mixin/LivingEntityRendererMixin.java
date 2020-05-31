package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.KibeModKt;
import io.github.lucaargolo.kibe.blocks.entangled.EntangledChestEntityRenderer;
import io.github.lucaargolo.kibe.effects.EffectCompendiumKt;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow
    public abstract M getModel();

    @Inject(at = @At("HEAD"), method = "getRenderLayer", cancellable = true)
    private void getRenderLayer(T entity, boolean showBody, boolean translucent, boolean bl, CallbackInfoReturnable<RenderLayer> info) {
        if(entity.hasStatusEffect(EffectCompendiumKt.getCURSED_EFFECT())) {
            Identifier texture = new Identifier("textures/block/coal_block.png");
            info.setReturnValue(this.getModel().getLayer(texture));
        }
    }

}
