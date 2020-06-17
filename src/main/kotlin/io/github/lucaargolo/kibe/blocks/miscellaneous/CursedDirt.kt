package io.github.lucaargolo.kibe.blocks.miscellaneous

import io.github.lucaargolo.kibe.effects.CURSED_EFFECT
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.*
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.EmptyFluid
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.WeightedPicker
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.world.*
import net.minecraft.world.biome.Biome
import net.minecraft.world.chunk.light.ChunkLightProvider
import java.util.*

class CursedDirt: GrassBlock(FabricBlockSettings.of(Material.SOLID_ORGANIC).ticksRandomly().strength(0.6F).sounds(BlockSoundGroup.GRASS)) {

    init {
        defaultState = stateManager.defaultState.with(Properties.LEVEL_15, 15).with(Properties.SNOWY, false)
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block, BlockState>) {
        stateManager.add(Properties.LEVEL_15)
        super.appendProperties(stateManager)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (player.isSneaking && !world.isClient && hand === Hand.MAIN_HAND) {
            val entries = (world as ServerWorld).chunkManager.chunkGenerator.getEntitySpawnList(EntityCategory.MONSTER, pos.up())
            if (entries.isEmpty()) {
                player.sendMessage(LiteralText("Nothing can spawn"))
                return ActionResult.SUCCESS
            } else {
                var names = TranslatableText("chat.kibe.cursed_dirt.spawn")
                entries.forEachIndexed { index, entry ->
                    names.append(entry.type.name)
                    if(index < entries.size-1) names.append(", ")
                }
                player.sendMessage(names)
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
                    val randomPos = pos.add(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1)
                    if (canSpread(state, world, randomPos)) {
                        world.setBlockState(randomPos, defaultState.with(SNOWY, world.getBlockState(randomPos.up()).block == Blocks.SNOW).with(Properties.LEVEL_15, state[Properties.LEVEL_15]-1))
                    }
                }
            }
        }

        //I dont really know what this does
        world.blockTickScheduler.schedule(pos, state.block, random.nextInt(200))

        //Dont spawn mobs in peaceful, in non water liquids or when doMobSpawning is set to false
        if ((world.getFluidState(pos.up()).fluid !is EmptyFluid && world.getFluidState(pos.up()).fluid != Fluids.WATER && world.getFluidState(pos.up()).fluid != Fluids.FLOWING_WATER) || world.difficulty == Difficulty.PEACEFUL || !world.gameRules[GameRules.DO_MOB_SPAWNING].get()) return

        //Chunk mob cap for avoiding L A G
        val chunkPos = world.getChunk(pos).pos
        val entityList = world.getEntities(null, Box(chunkPos.startX.toDouble(), 0.0, chunkPos.startZ.toDouble(), chunkPos.endX.toDouble(), 256.0, chunkPos.endZ.toDouble())) {it is MobEntity}
        if (entityList.size > 25) return

        //Yay, you've passed all the required conditions, now you only need to decide what mob to spawn and do it
        val mob = getSpawnableMonster(world, pos.up(), random)
        if (mob != null) {
            val location = if(world.getFluidState(pos.up()).fluid is EmptyFluid) SpawnRestriction.Location.ON_GROUND else SpawnRestriction.Location.IN_WATER
            if(SpawnHelper.canSpawn(location, world, pos.add(0.0, 1.0, 0.0), mob)) {
                val tag = getSpawnTag()
                tag.putString("id", Registry.ENTITY_TYPE.getId(mob).toString())
                val entity = EntityType.loadEntityWithPassengers(tag, world) {
                    it.refreshPositionAndAngles(pos.x+.0, pos.y+1.0, pos.z+.0, it.yaw, it.pitch)
                    if (!world.tryLoadEntity(it)) null else it
                }
                if(entity is MobEntity) {
                    entity.initialize(world, world.getLocalDifficulty(BlockPos(entity.pos)), SpawnType.NATURAL, null, null)
                }
                //mob.spawn(world, tag, null, null, pos.add(0.0, 1.0, 0.0), SpawnType.NATURAL, false, false)
            }
        }
    }

    private fun getSpawnTag(): CompoundTag {
        val activeEffect = CompoundTag()
        activeEffect.putInt("Id", Registry.STATUS_EFFECT.getRawId(CURSED_EFFECT))
        activeEffect.putInt("Amplifier", 1)
        activeEffect.putInt("Duration", 300)
        val activeEffects = ListTag()
        activeEffects.add(activeEffect)
        val tag = CompoundTag()
        tag.put("ActiveEffects", activeEffects)
        return tag
    }

    private fun canSpread(state: BlockState, world: ServerWorld, pos: BlockPos): Boolean {
        return (world.getBlockState(pos).block == Blocks.DIRT || world.getBlockState(pos).block == Blocks.GRASS_BLOCK) && canSurvive(world.getBlockState(pos), world, pos) && state[Properties.LEVEL_15] > 0
    }

    fun canSurvive(state: BlockState, worldView: WorldView, pos: BlockPos): Boolean {
        val blockPos = pos.up()
        val blockState = worldView.getBlockState(blockPos)
        return if (blockState.block === Blocks.SNOW && blockState.get(SnowBlock.LAYERS) as Int == 1) {
            true
        } else {
            ChunkLightProvider.getRealisticOpacity(worldView, state, pos, blockState, blockPos, Direction.UP, blockState.getOpacity(worldView, blockPos)) < worldView.maxLightLevel
        }
    }

    private fun getSpawnableMonster(world: ServerWorld, pos: BlockPos, random: Random): EntityType<*>? {
        val spawnList = world.chunkManager.chunkGenerator.getEntitySpawnList(EntityCategory.MONSTER, pos)
        if (spawnList.size == 0) return null
        val entry: Biome.SpawnEntry = WeightedPicker.getRandom(random, spawnList)
        if (!SpawnRestriction.canSpawn(entry.type, world, SpawnType.NATURAL, pos, world.random)) return null
        return entry.type
    }
}