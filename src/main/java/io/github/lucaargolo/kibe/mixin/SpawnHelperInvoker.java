package io.github.lucaargolo.kibe.mixin;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(SpawnHelper.class)
public interface SpawnHelperInvoker {

    @Invoker
    static boolean invokeCanSpawn(
            ServerWorld world,
            SpawnGroup group,
            StructureAccessor structureAccessor,
            ChunkGenerator chunkGenerator,
            SpawnSettings.SpawnEntry spawnEntry,
            BlockPos.Mutable pos,
            double squaredDistance
    ) {
        throw new AssertionError();
    }

    @Invoker
    static Optional<SpawnSettings.SpawnEntry> invokePickRandomSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, Random random, BlockPos pos) {
        throw new AssertionError();
    }

}
