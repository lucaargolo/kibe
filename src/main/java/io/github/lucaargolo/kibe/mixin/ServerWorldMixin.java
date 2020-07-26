package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderState;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Shadow @Final private MinecraftServer server;

    @SuppressWarnings("ConstantConditions")
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getForcedChunks()Lit/unimi/dsi/fastutil/longs/LongSet;"), method = "tick")
    public LongSet redirectGetForcedChunksOnTick(ServerWorld serverWorld) {
        ChunkLoaderState chunkLoaderState = this.server.getOverworld().getPersistentStateManager().getOrCreate(() -> new ChunkLoaderState(this.server, "kibe_chunk_loaders"),"kibe_chunk_loaders");
        LongSet longSet = serverWorld.getForcedChunks();
        if(longSet.isEmpty() && chunkLoaderState.getLoadedChunkMap().get(((ServerWorld) ((Object) this)).getRegistryKey()) != null && !chunkLoaderState.getLoadedChunkMap().get(((ServerWorld) ((Object) this)).getRegistryKey()).isEmpty())
            return new LongOpenHashSet(new long[]{0L});
        else
            return longSet;
    }

}
