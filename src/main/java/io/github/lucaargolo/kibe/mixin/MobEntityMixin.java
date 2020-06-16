package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.effects.EffectCompendiumKt;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntityMixin {

    @Inject(at = @At("HEAD"), method = "cannotDespawn", cancellable = true)
    public void cannotDespawn(CallbackInfoReturnable<Boolean> info) {
        boolean isCursed = ((MobEntity) ((Object) this)).hasStatusEffect(EffectCompendiumKt.getCURSED_EFFECT());
        if(isCursed) info.setReturnValue(true);
    }

}
