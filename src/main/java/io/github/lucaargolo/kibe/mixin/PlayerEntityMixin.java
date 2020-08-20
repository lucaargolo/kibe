package io.github.lucaargolo.kibe.mixin;

import io.github.ladysnake.pal.PlayerAbility;
import io.github.lucaargolo.kibe.items.ItemCompendiumKt;
import io.github.lucaargolo.kibe.items.ModItem;
import io.github.lucaargolo.kibe.items.miscellaneous.AbilityRing;
import io.github.lucaargolo.kibe.items.miscellaneous.Glider;
import io.github.lucaargolo.kibe.items.miscellaneous.SleepingBag;
import io.github.lucaargolo.kibe.utils.GliderHelper;
import io.github.lucaargolo.kibe.utils.RingAbilitiesKt;
import io.github.lucaargolo.kibe.utils.SlimeBounceHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"SuspiciousMethodCalls"})
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("TAIL"), method = "eatFood")
    public void eatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> info) {
        if(stack.getItem().equals(ItemCompendiumKt.getCURSED_KIBE()) && !world.isClient) {
            int x = random.nextInt(64);
            if(x == 0) kill();
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
        @SuppressWarnings("ConstantConditions")
        PlayerEntity player = (PlayerEntity) ((Object) this);
        //Glider logic
        if(!isOnGround() && !isTouchingWater() && !isFallFlying() && getVelocity().y < 0.0) {
            ItemStack mainHandStack = player.getMainHandStack();
            ItemStack offHandStack = player.getOffHandStack();
            boolean isGliding = (mainHandStack.getItem() instanceof Glider && Glider.Companion.isEnabled(mainHandStack)) || (offHandStack.getItem() instanceof Glider && Glider.Companion.isEnabled(offHandStack));
            if(isGliding) {
                GliderHelper.INSTANCE.setPlayerGliding(player, true);

                float hSpeed = 0.05f;
                float vSpeed = 0.5f;

                if(isSneaking()) {
                    hSpeed *= 2.5;
                    vSpeed *= 1.5;
                }

                Vec3d v = getVelocity();
                player.setVelocity(v.x, v.y*vSpeed, v.z);
                v = getVelocity();

                double x = Math.cos(Math.toRadians(player.headYaw + 90)) * hSpeed;
                double z = Math.sin(Math.toRadians(player.headYaw + 90)) * hSpeed;
                player.setVelocity(v.x+x, v.y, v.z+z);

                fallDistance = 0f;
                velocityDirty = true;
            }else{
                GliderHelper.INSTANCE.setPlayerGliding(player, false);
            }
        }else{
            GliderHelper.INSTANCE.setPlayerGliding(player, false);
        }
        //Ring Logic
        if(!world.isClient) {
            for(PlayerAbility pa : RingAbilitiesKt.getPotionToAbilityMap().keySet()) {
                if(pa.isEnabledFor(player)) {
                    StatusEffect se = RingAbilitiesKt.getPotionToAbilityMap().get(pa);
                    StatusEffectInstance sei = new StatusEffectInstance(se, 100);
                    player.addStatusEffect(sei);
                }
            }

            List<ItemStack> enabledRings = new ArrayList<>();

            for(ModItem mdi : ItemCompendiumKt.getItemRegistry().values()) {
                if(mdi.getItem() instanceof AbilityRing) {
                    AbilityRing ring = ((AbilityRing) mdi.getItem());
                    if(player.inventory.contains(new ItemStack(mdi.getItem()))) {
                        List<DefaultedList<ItemStack>> combinedInventory = ((PlayerInventoryMixin) player.inventory).getCombinedInventory();
                        for (List<ItemStack> list : combinedInventory) {
                            for (ItemStack itemStack : list) {
                                if (itemStack.getItem().equals(ring)) {
                                    if(itemStack.hasTag() && itemStack.getTag().contains("enabled") && itemStack.getTag().getBoolean("enabled")) {
                                        enabledRings.add(itemStack);
                                    }else{
                                        //Is disabled in inventory
                                        if(RingAbilitiesKt.getRingAbilitySource().grants(player, ring.getAbility())) {
                                            RingAbilitiesKt.getRingAbilitySource().revokeFrom(player, ring.getAbility());
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        //Is not in inventory
                        if(RingAbilitiesKt.getRingAbilitySource().grants(player, ring.getAbility())) {
                            RingAbilitiesKt.getRingAbilitySource().revokeFrom(player, ring.getAbility());
                        }
                    }
                }
            }

            if(enabledRings.size() > 1) {
                for(ItemStack stack : enabledRings) {
                    stack.getTag().putBoolean("unique", false);
                    AbilityRing ring = ((AbilityRing) stack.getItem());
                    //Overflow
                    if(RingAbilitiesKt.getRingAbilitySource().grants(player, ring.getAbility())) {
                        RingAbilitiesKt.getRingAbilitySource().revokeFrom(player, ring.getAbility());
                    }
                }
            }else{
                for(ItemStack stack : enabledRings) {
                    stack.getTag().putBoolean("unique", true);
                    AbilityRing ring = ((AbilityRing) stack.getItem());
                    RingAbilitiesKt.getRingAbilitySource().grantTo(player, ring.getAbility());
                }
            }
        }
        //Slime Boots Logic
        Iterator<Entity> keyIt;
        if(world.isClient) keyIt = SlimeBounceHandler.Companion.getClientBouncingEntities().keySet().iterator();
        else keyIt = SlimeBounceHandler.Companion.getServerBouncingEntities().keySet().iterator();
        while(keyIt.hasNext()) {
            Entity entity = keyIt.next();
            SlimeBounceHandler bounce;
            if(world.isClient) bounce = SlimeBounceHandler.Companion.getClientBouncingEntities().get(entity);
            else bounce = SlimeBounceHandler.Companion.getServerBouncingEntities().get(entity);
            if(player.equals(entity) && !player.isFallFlying()) {
                if(player.age == bounce.getBounceTick()) {
                    Vec3d velocity = getVelocity();
                    this.setVelocity(velocity.x, bounce.getBounce(), velocity.z);
                    bounce.setBounceTick(0);
                }

                if(!player.isOnGround() && player.age != bounce.getBounceTick()) {
                    if(bounce.getLastMovX() != getVelocity().x || bounce.getLastMovZ() != getVelocity().z) {
                        double f = 0.91d + 0.025d;
                        Vec3d velocity = getVelocity();
                        this.setVelocity(velocity.x/f, velocity.y, velocity.z/f);
                        this.velocityDirty = true;
                        bounce.setLastMovX(getVelocity().x);
                        bounce.setLastMovZ(getVelocity().z);
                    }
                }

                if(bounce.getWasInAir() && player.isOnGround() || player.isTouchingWater()) {
                    if(bounce.getTimer() == 0) {
                        bounce.setTimer(player.age);
                    }else if(player.age - bounce.getTimer() > 5){
                        keyIt.remove();
                        SlimeBounceHandler.Companion.getServerBouncingEntities().remove(entity);
                    }
                }else{
                    bounce.setTimer(0);
                    bounce.setWasInAir(true);
                }
            }
        }
    }

}
