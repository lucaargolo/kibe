package io.github.lucaargolo.kibe.mixin;

import io.github.ladysnake.pal.PlayerAbility;
import io.github.lucaargolo.kibe.KibeMod;
import io.github.lucaargolo.kibe.data.component.ComponentTypeCompendium;
import io.github.lucaargolo.kibe.item.AbilityRing;
import io.github.lucaargolo.kibe.item.Glider;
import io.github.lucaargolo.kibe.item.ItemCompendium;
import io.github.lucaargolo.kibe.item.SleepingBag;
import io.github.lucaargolo.kibe.mixed.PlayerEntityMixed;
import io.github.lucaargolo.kibe.utils.SlimeBounceHandler;
import io.github.lucaargolo.kibe.utils.helper.AbilityHelper;
import io.github.lucaargolo.kibe.utils.helper.GliderHelper;
import kotlin.Pair;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressWarnings({"SuspiciousMethodCalls"})
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityMixed {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Unique
    private final List<Pair<ItemStack, Long>> kibe_activeRingsList = new ArrayList<>();

    @Override
    public List<Pair<ItemStack, Long>> kibe$getActiveRingsList() {
        return kibe_activeRingsList;
    }

    @Inject(at = @At("TAIL"), method = "eatFood")
    public void eatFood(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        if(stack.getItem().equals(ItemCompendium.INSTANCE.getCURSED_KIBE()) && !world.isClient) {
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
        /*
            Code adapted from Open Gliders
            Available at: https://github.com/gr8pefish/OpenGlider/blob/002ec43e3b22e9a6c2cc94c0a5fb3d49bcce7594/src/main/java/gr8pefish/openglider/common/helper/OpenGliderPlayerHelper.java#L29
            Licensed under the MIT license available at: https://github.com/gr8pefish/OpenGlider/blob/1.12/LICENSE
         */
        ItemStack cursorStack = player.playerScreenHandler.getCursorStack();
        if(cursorStack.getItem() instanceof Glider && Glider.Companion.isEnabled(cursorStack)) {
            cursorStack.set(ComponentTypeCompendium.INSTANCE.getENABLED(), false);
        }
        if(!isOnGround() && !isTouchingWater() && !isFallFlying() && getVelocity().y < 0.0) {
            ItemStack mainHandStack = player.getMainHandStack();
            ItemStack offHandStack = player.getOffHandStack();
            ItemStack stack;
            EquipmentSlot slot;
            if(mainHandStack.getItem() instanceof Glider && Glider.Companion.isEnabled(mainHandStack)) {
                slot = EquipmentSlot.MAINHAND;
                stack = mainHandStack;
            }else if(offHandStack.getItem() instanceof Glider && Glider.Companion.isEnabled(offHandStack)) {
                slot = EquipmentSlot.OFFHAND;
                stack = offHandStack;
            } else {
                slot = EquipmentSlot.MAINHAND;
                stack = ItemStack.EMPTY;
            }
            boolean isGliding = !stack.isEmpty();
            if(isGliding) {
                GliderHelper.INSTANCE.setPlayerGliding(player, true);

                if(!KibeMod.INSTANCE.getCONFIG().getMiscellaneousModule().getGliderUnbreakable()) {
                    stack.damage(1, player, slot);
                }

                float hSpeed = 0.05f;
                float vSpeed = 0.5f;

                if(isSneaking()) {
                    hSpeed *= 2.5F;
                    vSpeed *= 1.5F;
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
        // Slime Boots logic
        /*
            Code adapted from Tinkers Construct
            Available at: https://github.com/SlimeKnights/TinkersConstruct/blob/c01173c0408352c50a2e8c5017552323ce42f5b4/src/main/java/slimeknights/tconstruct/library/SlimeBounceHandler.java#L46
            Licensed under the MIT license available at: https://tldrlegal.com/license/mit-license
         */
        Iterator<Entity> keyIt;
        if(getWorld().isClient) keyIt = SlimeBounceHandler.Companion.getClientBouncingEntities().keySet().iterator();
        else keyIt = SlimeBounceHandler.Companion.getServerBouncingEntities().keySet().iterator();
        while(keyIt.hasNext()) {
            Entity entity = keyIt.next();
            SlimeBounceHandler bounce;
            if(getWorld().isClient) bounce = SlimeBounceHandler.Companion.getClientBouncingEntities().get(entity);
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
        //Ring logic
        if(!getWorld().isClient) {
            for(PlayerAbility pa : AbilityHelper.INSTANCE.getABILITY_TO_EFFECT().keySet()) {
                if(pa.isEnabledFor(player)) {
                    RegistryEntry<StatusEffect> se = AbilityHelper.INSTANCE.getABILITY_TO_EFFECT().get(pa);
                    StatusEffectInstance sei = new StatusEffectInstance(se, 100, 1, false, false, true);
                    player.addStatusEffect(sei);
                }
            }

            LinkedHashMap<AbilityRing, List<ItemStack>> ringMap = new LinkedHashMap<>();
            for (Pair<ItemStack, Long> pair : kibe_activeRingsList) {
                ItemStack ringStack = pair.getFirst();
                Item ringItem = ringStack.getItem();
                if (ringItem instanceof AbilityRing && Boolean.TRUE.equals(ringStack.get(ComponentTypeCompendium.INSTANCE.getENABLED()))) {
                    ringMap.computeIfAbsent((AbilityRing) ringItem, k -> new ArrayList<>());
                    ringMap.get(ringItem).add(ringStack);
                }
            }
            int ringQnt = ringMap.values().stream().mapToInt(List::size).sum();
            AbilityRing.Companion.getRINGS().forEach(ring -> {
                if (ringMap.containsKey(ring)) {
                    if (ringQnt == -1 || ringQnt <= KibeMod.INSTANCE.getCONFIG().getMiscellaneousModule().getMaxRingsPerPlayer()) {
                        AbilityHelper.INSTANCE.getRING_SOURCE().grantTo(player, ring.getAbility());
                        for (ItemStack ringStack : ringMap.get(ring)) {
                            if (!Boolean.TRUE.equals(ringStack.get(ComponentTypeCompendium.INSTANCE.getUNIQUE()))) {
                                ringStack.set(ComponentTypeCompendium.INSTANCE.getUNIQUE(), true);
                            }
                        }
                    } else {
                        if (AbilityHelper.INSTANCE.getRING_SOURCE().grants(player, ring.getAbility())) {
                            AbilityHelper.INSTANCE.getRING_SOURCE().revokeFrom(player, ring.getAbility());
                        }
                        for (ItemStack ringStack : ringMap.get(ring)) {
                            if (Boolean.TRUE.equals(ringStack.get(ComponentTypeCompendium.INSTANCE.getUNIQUE()))) {
                                ringStack.set(ComponentTypeCompendium.INSTANCE.getUNIQUE(), false);
                            }
                        }
                    }
                } else {
                    if (AbilityHelper.INSTANCE.getRING_SOURCE().grants(player, ring.getAbility())) {
                        AbilityHelper.INSTANCE.getRING_SOURCE().revokeFrom(player, ring.getAbility());
                    }
                }
            });
        }

    }

}
