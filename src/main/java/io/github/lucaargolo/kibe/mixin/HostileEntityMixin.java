package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.utils.SpikeHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("HEAD"), method = "shouldDropLoot", cancellable = true)
    private void shouldDropLoot(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = ((LivingEntity) ((Object) this));
        if(SpikeHelper.INSTANCE.shouldCancelLootDrop(livingEntity)) {
            cir.setReturnValue(false);
        }
    }

}
