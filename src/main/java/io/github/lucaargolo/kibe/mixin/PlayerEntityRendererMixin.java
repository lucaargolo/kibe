package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.utils.GliderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(at = @At("HEAD"), method = "render")
    private void renderPre(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        if(GliderHelper.INSTANCE.isPlayerGliding(abstractClientPlayerEntity)) {
            float partialTicks = MinecraftClient.getInstance().getTickDelta();
            float interpolatedYaw = abstractClientPlayerEntity.prevHeadYaw + (abstractClientPlayerEntity.headYaw - abstractClientPlayerEntity.prevHeadYaw) * partialTicks;

            double x = abstractClientPlayerEntity.getX();
            double y = abstractClientPlayerEntity.getY();
            double z = abstractClientPlayerEntity.getZ();

            matrixStack.push();
            matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-interpolatedYaw));
            matrixStack.translate(0f, abstractClientPlayerEntity.getHeight() / 2f, 0f);
            matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90f));
            matrixStack.translate(0f, -abstractClientPlayerEntity.getHeight() / 2f, 0f);
            matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(interpolatedYaw));

            GliderHelper.INSTANCE.setPlayerRenderPoppingNeeds(abstractClientPlayerEntity, true);
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void renderPost(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        if(GliderHelper.INSTANCE.needsPlayerRenderPopping(abstractClientPlayerEntity)) {
            matrixStack.pop();
            GliderHelper.INSTANCE.setPlayerRenderPoppingNeeds(abstractClientPlayerEntity, false);
        }
    }
}
