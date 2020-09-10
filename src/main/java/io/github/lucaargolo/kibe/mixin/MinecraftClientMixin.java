package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.utils.EntangledTankCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(at = @At("HEAD"), method = "joinWorld")
    public void joinWorld(ClientWorld world, CallbackInfo info) {
        EntangledTankCache.INSTANCE.reset();
    }

}
