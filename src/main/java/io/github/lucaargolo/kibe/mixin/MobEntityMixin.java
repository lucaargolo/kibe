package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.effects.EffectCompendiumKt;
import io.github.lucaargolo.kibe.items.miscellaneous.Lasso;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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

    @Inject(at = @At("HEAD"), method = "interactWithItem", cancellable = true)
    public void method_29506(PlayerEntity playerEntity, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        ItemStack stack = playerEntity.getStackInHand(hand);
        Item item = stack.getItem();
        if(item instanceof Lasso lasso) {
            NbtCompound stackTag = stack.getTag();
            MobEntity entity = (MobEntity) ((Object) this);
            if(stackTag != null && !stackTag.contains("Entity")) {
                if (lasso.canStoreEntity(entity.getType())) {
                    if(entity.isLeashed()) entity.detachLeash(true, true);
                    entity.fallDistance = 0;
                    NbtCompound tag = new NbtCompound();
                    entity.saveSelfNbt(tag);
                    stackTag.put("Entity", tag);
                    stack.setTag(stackTag);
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    info.setReturnValue(ActionResult.SUCCESS);
                }
            }
        }
    }

}
