package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.items.miscellaneous.Lasso;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {

    @Inject(at = @At("HEAD"), method = "interactMob", cancellable = true)
    public void interactMob(PlayerEntity playerEntity, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        if(playerEntity.getStackInHand(hand).getItem() instanceof Lasso) {
            info.setReturnValue(ActionResult.PASS);
        }
    }

}
