package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.entities.EntityCompendiumKt;
import io.github.lucaargolo.kibe.entities.miscellaneous.ThrownTorchEntity;
import io.github.lucaargolo.kibe.utils.SyncableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow private ClientWorld world;

    @Inject(at = @At("TAIL"), method = "onEntitySpawn")
    public void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        double x = packet.getX(), y = packet.getY(), z = packet.getZ();
        EntityType<?> entityType = packet.getEntityTypeId();
        Entity entity = null;

        if(entityType == EntityCompendiumKt.getTHROWN_TORCH()) {
            entity = new ThrownTorchEntity(this.world, x, y, z);
        }

        if (entity != null) {
            int i = packet.getId();
            entity.updateTrackedPosition(x, y, z);
            entity.refreshPositionAfterTeleport(x, y, z);
            entity.setPitch((packet.getPitch() * 360) / 256.0F);
            entity.setYaw((packet.getYaw() * 360) / 256.0F);
            entity.setId(i);
            entity.setUuid(packet.getUuid());
            this.world.addEntity(i, entity);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V"), method = "method_38542", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, BlockEntity blockEntity, CallbackInfo info, NbtCompound nbtCompound) {
        if(blockEntity instanceof SyncableBlockEntity) {
            ((SyncableBlockEntity) blockEntity).readClientNbt(nbtCompound);
            info.cancel();
        }
    }



}
