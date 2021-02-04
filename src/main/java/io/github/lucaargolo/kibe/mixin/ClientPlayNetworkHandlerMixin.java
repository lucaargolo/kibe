package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.entities.EntityCompendiumKt;
import io.github.lucaargolo.kibe.entities.miscellaneous.ThrownTorchEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
            entity.pitch = (packet.getPitch() * 360) / 256.0F;
            entity.yaw = (packet.getYaw() * 360) / 256.0F;
            entity.setEntityId(i);
            entity.setUuid(packet.getUuid());
            this.world.addEntity(i, entity);
        }
    }

}
