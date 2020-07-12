@file:Suppress("unused")

package io.github.lucaargolo.kibe

import io.github.lucaargolo.kibe.blocks.VACUUM_HOPPER
import io.github.lucaargolo.kibe.blocks.chunkloader.ChunkLoaderState
import io.github.lucaargolo.kibe.blocks.initBlocks
import io.github.lucaargolo.kibe.blocks.initBlocksClient
import io.github.lucaargolo.kibe.blocks.vacuum.VacuumHopperScreen
import io.github.lucaargolo.kibe.effects.CURSED_EFFECT
import io.github.lucaargolo.kibe.effects.initEffects
import io.github.lucaargolo.kibe.fluids.initFluids
import io.github.lucaargolo.kibe.fluids.initFluidsClient
import io.github.lucaargolo.kibe.items.CURSED_DROPLETS
import io.github.lucaargolo.kibe.items.initItems
import io.github.lucaargolo.kibe.items.initItemsClient
import io.github.lucaargolo.kibe.items.miscellaneous.Lasso
import io.github.lucaargolo.kibe.recipes.VACUUM_HOPPER_RECIPE_SERIALIZER
import io.github.lucaargolo.kibe.recipes.initRecipeSerializers
import io.github.lucaargolo.kibe.recipes.initRecipeTypes
import io.github.lucaargolo.kibe.utils.initCreativeTab
import io.github.lucaargolo.kibe.utils.initTooltip
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.event.server.ServerStartCallback
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.loader.launch.common.FabricLauncherBase
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.entity.EntityType
import net.minecraft.loot.ConstantLootTableRange
import net.minecraft.loot.UniformLootTableRange
import net.minecraft.loot.condition.EntityPropertiesLootCondition
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.LootingEnchantLootFunction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.predicate.entity.EntityEffectPredicate
import net.minecraft.predicate.entity.EntityPredicate
import net.minecraft.resource.ResourceManager
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.TranslatableText
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import java.util.*
import java.util.function.Consumer

const val MOD_ID = "kibe"
val FAKE_PLAYER_UUID: UUID = UUID.randomUUID()
val USE_LASSO = Identifier(MOD_ID, "use_lasso")
val SYNCHRONIZE_LAST_RECIPE_PACKET = Identifier(MOD_ID, "synchronize_last_recipe")
val CLIENT = FabricLauncherBase.getLauncher().environmentType == EnvType.CLIENT

fun init() {
    initRecipeSerializers()
    initRecipeTypes()
    initTooltip()
    initBlocks()
    initItems()
    initEffects()
    initLootTables()
    initFluids()
    initCreativeTab()
    initPackets()
    initExtras()
}

fun initClient() {
    initBlocksClient()
    initItemsClient()
    initFluidsClient()
    initExtrasClient()
    initPacketsClient()
}

fun initPackets() {
    ServerSidePacketRegistry.INSTANCE.register(USE_LASSO) { packetContext: PacketContext, attachedData: PacketByteBuf ->
        val hand = attachedData.readEnumConstant(Hand::class.java)
        val type = attachedData.readInt()
        val entityUUID = if(type == 1) attachedData.readUuid() else null
        val blockHit = if(type == 2) attachedData.readBlockHitResult() else null
        packetContext.taskQueue.execute {
            val player = packetContext.player as ServerPlayerEntity
            val world = player.world as ServerWorld
            val entity = world.getEntity(entityUUID)

            val stack = player.getStackInHand(hand)
            val stackTag = stack.orCreateTag

            val lasso = stack.item
            if(lasso !is Lasso) return@execute

            if(entity != null && !stackTag.contains("Entity")) {
                if(lasso.canStoreEntity(entity.type)) {
                    val tag = CompoundTag()
                    entity.saveSelfToTag(tag)
                    stackTag.put("Entity", tag)
                    stack.tag = stackTag
                    entity.remove()
                }
            }else if(blockHit != null && stackTag.contains("Entity")) {
                val pos = blockHit.blockPos

                val targetPos = when(blockHit.side) {
                    Direction.DOWN -> pos.down(2)
                    Direction.UP -> pos.up()
                    Direction.EAST -> pos.east()
                    Direction.NORTH -> pos.north()
                    Direction.WEST -> pos.east()
                    Direction.SOUTH -> pos.south()
                    else -> pos
                }

                val newTag = lasso.addToTag(stackTag["Entity"] as CompoundTag)
                val newEntity = EntityType.loadEntityWithPassengers(newTag, world) {
                    println(targetPos)
                    it.refreshPositionAndAngles(targetPos.x+.5, targetPos.y+.0, targetPos.z+.5, it.yaw, it.pitch)
                    if (!world.tryLoadEntity(it)) {
                        player.sendMessage(TranslatableText("chat.kibe.lasso.cannot_spawn"), true)
                        null
                    }
                    else it
                }

                if(newEntity != null) {
                    stackTag.remove("Entity")
                    stack.tag = stackTag
                }
            }

        }
    }
}

fun initPacketsClient() {
    ClientSidePacketRegistry.INSTANCE.register(SYNCHRONIZE_LAST_RECIPE_PACKET) { packetContext: PacketContext, attachedData: PacketByteBuf ->
        val id = attachedData.readIdentifier()
        val recipe = VACUUM_HOPPER_RECIPE_SERIALIZER.read(id, attachedData)
        packetContext.taskQueue.execute {
            if(MinecraftClient.getInstance().currentScreen is VacuumHopperScreen) {
                val screen = MinecraftClient.getInstance().currentScreen as VacuumHopperScreen
                screen.screenHandler.lastRecipe = recipe
            }
        }
    }
}

fun initExtras() {
    ServerStartCallback.EVENT.register(ServerStartCallback {  server ->
        server.worlds.firstOrNull()?.let {world ->
            @Suppress("TYPE_MISMATCH")
            //Why is it even triggering a type mismatch here????
            world.persistentStateManager.getOrCreate({ ChunkLoaderState(world.server, "kibe:chunk_loaders") }, "kibe:chunk_loaders")
        }
    })
}

fun initExtrasClient() {
    @Suppress("deprecated")
    ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(ClientSpriteRegistryCallback { _, registry ->
        registry.register(Identifier(MOD_ID, "block/entangled_chest_runes"))
        (0..15).forEach{
            registry.register(Identifier(MOD_ID, "block/redstone_timer_$it"))
        }
    })
    ModelLoadingRegistry.INSTANCE.registerAppender { _: ResourceManager?, out: Consumer<ModelIdentifier?> ->
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_background"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_ring"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_gold_core"), "inventory"))
        out.accept(ModelIdentifier(Identifier(MOD_ID, "entangled_bag_diamond_core"), "inventory"))
    }
    BlockRenderLayerMap.INSTANCE.putBlock(VACUUM_HOPPER, RenderLayer.getTranslucent())
}

fun initLootTables() {
    //Add cursed droplets drop to mobs with the cursed effect
    LootTableLoadingCallback.EVENT.register(LootTableLoadingCallback { _, _, id: Identifier, supplier: FabricLootSupplierBuilder, _ ->
        if (id.toString().startsWith("minecraft:entities")) {
            val poolBuilder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(CURSED_DROPLETS))
                .conditionally(
                    EntityPropertiesLootCondition.builder(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.create().effects(EntityEffectPredicate.create().withEffect(CURSED_EFFECT))
                    ))
                .conditionally(RandomChanceLootCondition.builder(0.05F))
                .withFunction(LootingEnchantLootFunction.builder(UniformLootTableRange.between(0f,1.5f)).build())
            supplier.pool(poolBuilder)
        }
    })
    //Add cursed droplets to wither skeletons
    LootTableLoadingCallback.EVENT.register(LootTableLoadingCallback { _, _, id: Identifier, supplier: FabricLootSupplierBuilder, _ ->
        if (id.toString() == "minecraft:entities/wither_skeleton") {
            val poolBuilder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(CURSED_DROPLETS))
                .conditionally(RandomChanceLootCondition.builder(0.1F))
                .withFunction(LootingEnchantLootFunction.builder(UniformLootTableRange.between(0f,1.5f)).build())
            supplier.pool(poolBuilder)
        }
    })
}
