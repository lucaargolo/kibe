package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.effects.EffectCompendiumKt;
import io.github.lucaargolo.kibe.items.ItemCompendiumKt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "cannotDespawn", cancellable = true)
    public void cannotDespawn(CallbackInfoReturnable<Boolean> info) {
        boolean isCursed = hasStatusEffect(EffectCompendiumKt.getCURSED_EFFECT());
        if(isCursed) info.setReturnValue(true);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 0), method = "interactWithItem", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void method_29506(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir, ItemStack itemStack) {
        if(itemStack.isOf(ItemCompendiumKt.getGOLDEN_LASSO()) || itemStack.isOf(ItemCompendiumKt.getCURSED_LASSO()) || itemStack.isOf(ItemCompendiumKt.getDIAMOND_LASSO())) {
            ActionResult actionResult = itemStack.useOnEntity(player, this, hand);
            if (actionResult.isAccepted()) {
                cir.setReturnValue(actionResult);
            }
        }
    }

}
