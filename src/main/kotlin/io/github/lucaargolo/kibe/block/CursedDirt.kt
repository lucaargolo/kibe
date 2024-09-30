package io.github.lucaargolo.kibe.block


import com.mojang.serialization.MapCodec
import io.github.lucaargolo.kibe.KibeMod
import io.github.lucaargolo.kibe.blockentity.BigTorchBlockEntity
import io.github.lucaargolo.kibe.effect.EffectCompendium
import io.github.lucaargolo.kibe.mixin.SpawnHelperInvoker
import net.minecraft.block.*
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.SpawnRestriction
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.EmptyFluid
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3i
import net.minecraft.util.math.random.Random
import net.minecraft.world.Difficulty
import net.minecraft.world.GameRules
import net.minecraft.world.World
import net.minecraft.world.WorldView
import net.minecraft.world.biome.SpawnSettings.SpawnEntry
import net.minecraft.world.chunk.light.ChunkLightProvider
import java.util.*

class CursedDirt(settings: Settings): GrassBlock(settings) {

    init {
        defaultState = stateManager.defaultState.with(Properties.LEVEL_15, 15).with(Properties.SNOWY, false)
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block, BlockState>) {
        stateManager.add(Properties.LEVEL_15)
        super.appendProperties(stateManager)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hit: BlockHitResult): ActionResult {
        if (player.isSneaking && !world.isClient) {
            val entries = (world as ServerWorld).chunkManager.chunkGenerator.getEntitySpawnList(world.getBiome(pos), world.structureAccessor, SpawnGroup.MONSTER, pos.up()).entries
            if (entries.isEmpty()) {
                player.sendMessage(Text.literal("Nothing can spawn"), false)
                return ActionResult.SUCCESS
            } else {
                val names = Text.translatable("chat.kibe.cursed_dirt.spawn")
                entries.forEachIndexed { index, entry ->
                    names.append(entry.type.name)
                    if(index < entries.size-1) names.append(", ")
                }
                player.sendMessage(names, false)
            }
            return ActionResult.SUCCESS
        }
        return ActionResult.FAIL
    }

    @Suppress("DEPRECATION")
    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        if (!world.isChunkLoaded(pos)) return

        //Checks if its exposed to sunlight and spreads itself
        if (world.isDay && world.getBrightness(pos.up()) > 0.5F && world.isSkyVisible(pos.up())) {
            world.setBlockState(pos, Blocks.DIRT.defaultState)
            if(world.getBlockState(pos.up()).isAir) world.setBlockState(pos.up(), Blocks.FIRE.defaultState)
        } else {
            if (world.getLightLevel(pos.up()) <= 7) {
                repeat((0..3).count()) {
                    val randomPos = pos.add(Vec3i(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1))
                    if (canSpread(state, world, randomPos)) {
                        world.setBlockState(randomPos, defaultState.with(SNOWY, world.getBlockState(randomPos.up()).block == Blocks.SNOW).with(Properties.LEVEL_15, state[Properties.LEVEL_15]-1))
                    }
                }
            }
        }

        //I dont really know what this does
        world.scheduleBlockTick(pos, state.block, random.nextInt(200))

        //Dont spawn mobs in peaceful, in non water liquids or when doMobSpawning is set to false
        if ((world.getFluidState(pos.up()).fluid !is EmptyFluid && world.getFluidState(pos.up()).fluid != Fluids.WATER && world.getFluidState(pos.up()).fluid != Fluids.FLOWING_WATER) || world.difficulty == Difficulty.PEACEFUL || !world.gameRules[GameRules.DO_MOB_SPAWNING].get()) return

        //Chunk mob cap for avoiding L A G
        val chunkPos = world.getChunk(pos).pos
        val entityList = world.getOtherEntities(null, Box(chunkPos.startX.toDouble(), 0.0, chunkPos.startZ.toDouble(), chunkPos.endX.toDouble(), 256.0, chunkPos.endZ.toDouble())) {it is MobEntity}
        if (entityList.size > KibeMod.CONFIG.miscellaneousModule.cursedDirtMobCap) return

        val entry = getSpawnableMonster(world, pos.up(), random)
        if (entry != null) {
            val mob = entry.type
            if(SpawnHelperInvoker.invokeCanSpawn(world, mob.spawnGroup, world.structureAccessor, world.chunkManager.chunkGenerator, entry, pos.up().mutableCopy(), 0.0)) {
                val tag = getSpawnTag()
                tag.putString("id", Registries.ENTITY_TYPE.getId(mob).toString())
                val entity = EntityType.loadEntityWithPassengers(tag, world) {
                    it.refreshPositionAndAngles(pos.x+.5, pos.y+1.0, pos.z+.5, it.yaw, it.pitch)
                    if(it.isInsideWall) null else
                    if (!world.tryLoadEntity(it)) null else it
                }
                if(entity is MobEntity) {
                    entity.initialize(world, world.getLocalDifficulty(BlockPos.ofFloored(entity.pos)), SpawnReason.NATURAL, null)
                }
            }
        }
    }

    private fun getSpawnTag(): NbtCompound {
        val activeEffect = NbtCompound()
        activeEffect.putInt("Id", Registries.STATUS_EFFECT.getRawId(EffectCompendium.CURSED.value()))
        activeEffect.putInt("Amplifier", 1)
        activeEffect.putInt("Duration", 300)
        val activeEffects = NbtList()
        activeEffects.add(activeEffect)
        val tag = NbtCompound()
        tag.put("ActiveEffects", activeEffects)
        return tag
    }

    private fun canSpread(state: BlockState, world: ServerWorld, pos: BlockPos): Boolean {
        return (world.getBlockState(pos).block == Blocks.DIRT || world.getBlockState(pos).block == Blocks.GRASS_BLOCK) && canSurvive(world.getBlockState(pos), world, pos) && state[Properties.LEVEL_15] > 0
    }

    private fun canSurvive(state: BlockState, worldView: WorldView, pos: BlockPos): Boolean {
        val blockPos = pos.up()
        val blockState = worldView.getBlockState(blockPos)
        return if (blockState.block === Blocks.SNOW && blockState.get(SnowBlock.LAYERS) as Int == 1) {
            true
        } else {
            ChunkLightProvider.getRealisticOpacity(worldView, state, pos, blockState, blockPos, Direction.UP, blockState.getOpacity(worldView, blockPos)) < worldView.maxLightLevel
        }
    }

    private fun getSpawnableMonster(world: ServerWorld, pos: BlockPos, random: Random): SpawnEntry? {
        val optionalEntry: Optional<SpawnEntry> = SpawnHelperInvoker.invokePickRandomSpawnEntry(world, world.structureAccessor, world.chunkManager.chunkGenerator, SpawnGroup.MONSTER, random, pos)
        val entry = if(optionalEntry.isPresent) optionalEntry.get() else null ?: return null
        if(KibeMod.CONFIG.miscellaneousModule.cursedDirtDenyList.contains(Registries.ENTITY_TYPE.getId(entry.type).toString())) return null
        BigTorchBlockEntity.setException(true)
        SpawnRestriction.canSpawn(entry.type, world, SpawnReason.NATURAL, pos, world.random).let {
            BigTorchBlockEntity.setException(false)
            return if(it) entry else null
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getCodec() = CODEC as MapCodec<GrassBlock>

    companion object {
        private val CODEC: MapCodec<CursedDirt> = createCodec(::CursedDirt)
    }

}