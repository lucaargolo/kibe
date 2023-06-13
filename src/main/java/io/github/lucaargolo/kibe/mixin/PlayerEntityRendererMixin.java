package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.utils.GliderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    /*
        Code adapted from Open Gliders
        Available at: https://github.com/gr8pefish/OpenGlider/blob/002ec43e3b22e9a6c2cc94c0a5fb3d49bcce7594/src/main/java/gr8pefish/openglider/client/event/ClientEventHandler.java#L37
        Licensed under the MIT license available at: https://github.com/gr8pefish/OpenGlider/blob/1.12/LICENSE
     */
    @Inject(at = @At("HEAD"), method = "render")
    private void renderPre(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        if(GliderHelper.INSTANCE.isPlayerGliding(abstractClientPlayerEntity)) {
            float partialTicks = MinecraftClient.getInstance().getTickDelta();
            float interpolatedYaw = abstractClientPlayerEntity.prevHeadYaw + (abstractClientPlayerEntity.headYaw - abstractClientPlayerEntity.prevHeadYaw) * partialTicks;

            matrixStack.push();
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-interpolatedYaw));
            matrixStack.translate(0f, abstractClientPlayerEntity.getHeight() / 2f, 0f);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f));
            matrixStack.translate(0f, -abstractClientPlayerEntity.getHeight() / 2f, 0f);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(interpolatedYaw));

            GliderHelper.INSTANCE.setPlayerRenderPoppingNeeds(abstractClientPlayerEntity, true);
        }
    }

    /*
        Code adapted from Open Gliders
        Available at: https://github.com/gr8pefish/OpenGlider/blob/002ec43e3b22e9a6c2cc94c0a5fb3d49bcce7594/src/main/java/gr8pefish/openglider/client/event/ClientEventHandler.java#L81
        Licensed under the MIT license available at: https://github.com/gr8pefish/OpenGlider/blob/1.12/LICENSE
     */
    @Inject(at = @At("TAIL"), method = "render")
    private void renderPost(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        if(GliderHelper.INSTANCE.needsPlayerRenderPopping(abstractClientPlayerEntity)) {
            matrixStack.pop();
            GliderHelper.INSTANCE.setPlayerRenderPoppingNeeds(abstractClientPlayerEntity, false);
        }
    }
}
