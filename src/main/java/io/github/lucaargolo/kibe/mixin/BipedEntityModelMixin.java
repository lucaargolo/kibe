package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.items.miscellaneous.Glider;
import io.github.lucaargolo.kibe.utils.GliderHelper;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {

    @Final @Shadow public ModelPart rightArm;
    @Final @Shadow public ModelPart leftArm;

    @Final @Shadow public ModelPart rightLeg;
    @Final @Shadow public ModelPart leftLeg;

    @Inject(at = @At("TAIL"), method = "setAngles")
    private void setAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo info) {
        if(livingEntity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) livingEntity;
            ItemStack stack1 = livingEntity.getStackInHand(Hand.MAIN_HAND);
            ItemStack stack2 = livingEntity.getStackInHand(Hand.OFF_HAND);
            if((stack1.getItem() instanceof Glider && Glider.Companion.isEnabled(stack1)) || (stack2.getItem() instanceof Glider && Glider.Companion.isEnabled(stack2))) {
                this.rightArm.pitch = -0.35F;
                this.rightArm.roll = 0F;
                this.rightArm.yaw = 0F;
                this.leftArm.pitch = -0.35F;
                this.leftArm.roll = 0F;
                this.leftArm.yaw = 0F;
            }
            boolean isGliding = GliderHelper.INSTANCE.isPlayerGliding(player);
            if(isGliding) {
                this.rightLeg.pitch = 0F;
                this.rightLeg.roll = 0F;
                this.rightLeg.yaw = 0F;
                this.leftLeg.pitch = 0F;
                this.leftLeg.roll = 0F;
                this.leftArm.yaw = 0F;
            }
        }
    }




}
