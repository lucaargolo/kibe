package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderState;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Shadow @Final private MinecraftServer server;

    private ChunkLoaderState cachedChunkLoaderState = null;

    private ChunkLoaderState getCachedChunkLoaderState() {
        if(cachedChunkLoaderState == null) {
            cachedChunkLoaderState = this.server.getOverworld().getPersistentStateManager().getOrCreate(tag -> ChunkLoaderState.Companion.createFromTag(tag, this.server), () -> new ChunkLoaderState(this.server),"kibe_chunk_loaders");
        }
        return cachedChunkLoaderState;
    }

    @SuppressWarnings("ConstantConditions")
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getForcedChunks()Lit/unimi/dsi/fastutil/longs/LongSet;"), method = "tick")
    public LongSet redirectGetForcedChunksOnTick(ServerWorld serverWorld) {
        ChunkLoaderState chunkLoaderState = getCachedChunkLoaderState();
        LongSet longSet = serverWorld.getForcedChunks();
        if(longSet.isEmpty() && chunkLoaderState.getLoadedChunkMap().get(((ServerWorld) ((Object) this)).getRegistryKey()) != null && !chunkLoaderState.getLoadedChunkMap().get(((ServerWorld) ((Object) this)).getRegistryKey()).isEmpty())
            return new LongOpenHashSet(new long[]{0L});
        else
            return longSet;
    }


    @Inject(at = @At("HEAD"), method = "isChunkLoaded", cancellable = true)
    public void isChunkLoaded(long chunkPos, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld serverWorld = (ServerWorld) (Object) this;
        ChunkLoaderState chunkLoaderState = getCachedChunkLoaderState();
        if(chunkLoaderState.isItBeingChunkLoaded(serverWorld, new ChunkPos(chunkPos))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "shouldTick(Lnet/minecraft/util/math/BlockPos;)Z", cancellable = true)
    public void shouldTick(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld serverWorld = (ServerWorld) (Object) this;
        ChunkLoaderState chunkLoaderState = getCachedChunkLoaderState();
        if(chunkLoaderState.isItBeingChunkLoaded(serverWorld, new ChunkPos(pos))) {
            cir.setReturnValue(true);
        }
    }


    @Inject(at = @At("HEAD"), method = "shouldTick(Lnet/minecraft/util/math/ChunkPos;)Z", cancellable = true)
    public void shouldTick(ChunkPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld serverWorld = (ServerWorld) (Object) this;
        ChunkLoaderState chunkLoaderState = getCachedChunkLoaderState();
        if(chunkLoaderState.isItBeingChunkLoaded(serverWorld, pos)) {
            cir.setReturnValue(true);
        }
    }

}
