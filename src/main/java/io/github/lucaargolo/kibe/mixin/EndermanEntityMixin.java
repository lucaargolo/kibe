package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.effects.EffectCompendiumKt;
import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndermanEntity.class)
public class EndermanEntityMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("HEAD"), method = "teleportRandomly", cancellable = true)
    public void teleportRandomly(CallbackInfoReturnable<Boolean> info) {
        EndermanEntity entity = (EndermanEntity) ((Object) this);
        if(entity.hasStatusEffect(EffectCompendiumKt.getCURSED_EFFECT())) {
            info.setReturnValue(false);
        }
    }

}
