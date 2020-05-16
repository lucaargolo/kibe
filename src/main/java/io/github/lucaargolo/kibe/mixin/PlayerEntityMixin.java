package io.github.lucaargolo.kibe.mixin;

import io.github.ladysnake.pal.VanillaAbilities;
import io.github.lucaargolo.kibe.items.ItemCompendiumKt;
import io.github.lucaargolo.kibe.items.miscellaneous.SleepingBag;
import io.github.lucaargolo.kibe.utils.RingAbilitySourceKt;
import io.github.lucaargolo.kibe.utils.SlimeBounceHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Iterator;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "setPlayerSpawn", cancellable = true)
    private void setPlayerSpawn(CallbackInfo info) {
        if(SleepingBag.Companion.getPlayersSleeping().contains(this)) {
            info.cancel();
        }
    }

    @Inject(at = @At("TAIL"), method = "wakeUp()V")
    private void wakeUpV(CallbackInfo info) {
        SleepingBag.Companion.getPlayersSleeping().remove(this);
    }

    @Inject(at = @At("TAIL"), method = "wakeUp(ZZ)V")
    private void wakeUpZ(CallbackInfo info) {
        SleepingBag.Companion.getPlayersSleeping().remove(this);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {
        PlayerEntity player = (PlayerEntity) ((Object) this);
        //Angel Ring Logic
        if(!player.isCreative()) {
            HashSet<Item> itemSet = new HashSet<>();
            itemSet.add(ItemCompendiumKt.getANGEL_RING());
            if(player.inventory.containsAnyInInv(itemSet)) {
                RingAbilitySourceKt.getAngelRingSource().grants(player, VanillaAbilities.ALLOW_FLYING);
            }else {
                RingAbilitySourceKt.getAngelRingSource().revokeFrom(player, VanillaAbilities.ALLOW_FLYING);
            }
        }
        //Slime Boots Logic
        Iterator<Entity> keyIt;
        if(world.isClient) keyIt = SlimeBounceHandler.Companion.getClientBouncingEntityes().keySet().iterator();
        else keyIt = SlimeBounceHandler.Companion.getServerBouncingEntityes().keySet().iterator();
        while(keyIt.hasNext()) {
            Entity entity = keyIt.next();
            SlimeBounceHandler bounce;
            if(world.isClient) bounce = SlimeBounceHandler.Companion.getClientBouncingEntityes().get(entity);
            else bounce = SlimeBounceHandler.Companion.getServerBouncingEntityes().get(entity);
            if(player.equals(entity) && !player.isFallFlying()) {
                System.out.println("cu");

                if(player.age == bounce.getBounceTick()) {
                    Vec3d velocity = getVelocity();
                    this.setVelocity(velocity.x, bounce.getBounce(), velocity.z);
                    bounce.setBounceTick(0);
                }

                if(!player.onGround && player.age != bounce.getBounceTick()) {
                    if(bounce.getLastMovX() != getVelocity().x || bounce.getLastMovZ() != getVelocity().z) {
                        double f = 0.91d + 0.025d;
                        Vec3d velocity = getVelocity();
                        System.out.println(velocity);
                        this.setVelocity(velocity.x/f, velocity.y, velocity.z/f);
                        System.out.println(getVelocity());
                        this.velocityDirty = true;
                        bounce.setLastMovX(getVelocity().x);
                        bounce.setLastMovZ(getVelocity().z);
                    }
                }

                if(bounce.getWasInAir() && player.onGround) {
                    if(bounce.getTimer() == 0) {
                        bounce.setTimer(player.age);
                    }else if(player.age - bounce.getTimer() > 5){
                        keyIt.remove();
                        SlimeBounceHandler.Companion.getServerBouncingEntityes().remove(entity);
                    }
                }else{
                    bounce.setTimer(0);
                    bounce.setWasInAir(true);
                }
            }
        }
    }

}
