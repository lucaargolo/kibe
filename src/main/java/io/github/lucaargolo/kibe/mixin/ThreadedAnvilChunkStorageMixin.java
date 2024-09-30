package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.data.state.ChunkLoaderState;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkLoadingManager.class)
public class ThreadedAnvilChunkStorageMixin {

    @Shadow @Final ServerWorld world;

    @Inject(at = @At("HEAD"), method = "shouldTick", cancellable = true)
    public void shouldTick(ChunkPos pos, CallbackInfoReturnable<Boolean> info) {
        ChunkLoaderState state = ChunkLoaderState.Companion.getPersistentState(world.getServer());
        boolean bl = state.isItBeingChunkLoaded(world, pos);
        if(bl) info.setReturnValue(true);
    }


}
