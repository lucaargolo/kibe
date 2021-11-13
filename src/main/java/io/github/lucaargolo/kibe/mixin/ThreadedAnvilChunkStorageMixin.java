package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {

    @Shadow @Final ServerWorld world;

    @Inject(at = @At("HEAD"), method = "canTickChunk", cancellable = true)
    public void isTooFarFromPlayersToSpawnMobs(ServerPlayerEntity player, ChunkPos pos, CallbackInfoReturnable<Boolean> info) {
        ChunkLoaderState state = world.getServer().getOverworld().getPersistentStateManager().getOrCreate( tag -> ChunkLoaderState.Companion.createFromTag(tag, world.getServer()), () -> new ChunkLoaderState(world.getServer()) , "kibe_chunk_loaders");
        boolean bl = state.isItBeingChunkLoaded(world, pos);
        if(bl) info.setReturnValue(false);
    }


}
