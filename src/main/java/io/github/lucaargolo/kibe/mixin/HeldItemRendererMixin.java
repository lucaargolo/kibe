package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.items.miscellaneous.Glider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Shadow private ItemStack offHand;
    @Shadow private ItemStack mainHand;

    @Shadow private float equipProgressMainHand;
    @Shadow private float equipProgressOffHand;
    @Shadow private float prevEquipProgressOffHand;
    @Shadow private float prevEquipProgressMainHand;

    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("TAIL"), method = "updateHeldItems")
    private void updateHeldItems(CallbackInfo info) {
        assert client.player != null;
        ItemStack mainHandStack = client.player.getMainHandStack();
        ItemStack offHandStack = client.player.getOffHandStack();
        if(offHandStack.getItem() instanceof Glider) {
            offHand = offHandStack;
            equipProgressOffHand = 1F;
            prevEquipProgressOffHand = 1F;
        }
        if(client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof Glider) {
            mainHand = mainHandStack;
            equipProgressMainHand = 1F;
            prevEquipProgressMainHand= 1F;
        }
    }

    @Inject(at = @At("HEAD"), method = "renderFirstPersonItem")
    private void renderFirstPersonItemPre(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        if(player.getStackInHand(hand).getItem() instanceof Glider && Glider.Companion.isEnabled(player.getStackInHand(hand))) {
            matrices.push();
            matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(360 - player.getPitch(tickDelta)));
        }
    }

    @Inject(at = @At("TAIL"), method = "renderFirstPersonItem")
    private void renderFirstPersonItemPost(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        if(player.getStackInHand(hand).getItem() instanceof Glider && Glider.Companion.isEnabled(player.getStackInHand(hand))) {
            matrices.pop();
        }
    }

}
