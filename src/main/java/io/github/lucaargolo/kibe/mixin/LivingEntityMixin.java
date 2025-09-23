package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.block.Elevator;
import io.github.lucaargolo.kibe.effect.EffectCompendium;
import io.github.lucaargolo.kibe.item.Glider;
import io.github.lucaargolo.kibe.item.ItemCompendium;
import io.github.lucaargolo.kibe.item.SleepingBag;
import io.github.lucaargolo.kibe.mixed.LivingEntityMixed;
import io.github.lucaargolo.kibe.utils.SlimeBounceHandler;
import io.github.lucaargolo.kibe.utils.helper.SpikeHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityMixed {

    @SuppressWarnings("WrongEntityDataParameterClass")
    private static final TrackedData<Boolean> CURSED = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Shadow public abstract ItemStack getStackInHand(Hand hand);

    @Shadow public abstract boolean teleport(double x, double y, double z, boolean particleEffects);

    @Shadow public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("TAIL"), method = "initDataTracker")
    private void initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(CURSED, false);
    }

    @Inject(at = @At("HEAD"), method = "onStatusEffectApplied")
    private void addStatusEffect(StatusEffectInstance effect, Entity source, CallbackInfo ci) {
        if(effect.getEffectType().equals(EffectCompendium.INSTANCE.getCURSED()))
            dataTracker.set(CURSED, true);
    }

    @Inject(at = @At("HEAD"), method = "onStatusEffectRemoved")
    private void removeStatusEffect(StatusEffectInstance effect, CallbackInfo ci) {
        if(effect.getEffectType().equals(EffectCompendium.INSTANCE.getCURSED()))
            dataTracker.set(CURSED, false);
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    private void afterReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if(hasStatusEffect(EffectCompendium.INSTANCE.getCURSED()))
            dataTracker.set(CURSED, true);
    }

    @Inject(at = @At("HEAD"), method = "swingHand(Lnet/minecraft/util/Hand;)V", cancellable = true)
    private void swingHand(Hand hand, CallbackInfo info) {
        ItemStack stack = this.getStackInHand(hand);
        if(stack.getItem() instanceof Glider && Glider.Companion.isEnabled(stack)) {
            info.cancel();
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Inject(at = @At("HEAD"), method = "isSleepingInBed", cancellable = true)
    private void isSleepingInBed(CallbackInfoReturnable<Boolean> info) {
        if(SleepingBag.Companion.getPlayersSleeping().contains(this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "jump")
    private void jump(CallbackInfo info) {
        BlockPos pos = this.getBlockPos();
        Block block = this.getWorld().getBlockState(pos.down()).getBlock();
        if (block instanceof Elevator && getWorld().getBlockState(pos).getCollisionShape(getWorld(), pos).isEmpty()) {
            while(pos.getY() < getWorld().getTopY()) {
                if(getWorld().getBlockState(pos.up()).getBlock().equals(block) && Elevator.Companion.isElevatorValid(getWorld(), pos.up())) {
                    getWorld().playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, getWorld().random.nextFloat() * 0.25F + 0.6F);
                    this.teleport(this.getPos().x, pos.up().getY()+1.15, this.getPos().z, false);
                    break;
                }else{
                    pos = pos.up();
                }
            }
        }
    }

    /*
        Code adapted from Tinkers Construct
        Available at: https://github.com/SlimeKnights/TinkersConstruct/blob/c01173c0408352c50a2e8c5017552323ce42f5b4/src/main/java/slimeknights/tconstruct/gadgets/item/ItemSlimeBoots.java#L127
        Licensed under the MIT license available at: https://tldrlegal.com/license/mit-license
     */
    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("HEAD"), method = "handleFallDamage")
    private void handleFallDamage(float fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> info) {
        if((Object) this instanceof PlayerEntity) {
            PlayerEntity player = ((PlayerEntity) ((Object) this));
            if (player.getEquippedStack(EquipmentSlot.FEET).getItem() == ItemCompendium.INSTANCE.getSLIME_BOOTS()) {
                if(!isSneaking() && fallDistance > 2) {
                    this.fallDistance = 0;

                    if(getWorld().isClient) {
                        setVelocity(getVelocity().x, getVelocity().y*-0.9, getVelocity().z);
                        velocityDirty = true;
                        setOnGround(false);
                        double f = 0.91d + 0.04d;
                        setVelocity(getVelocity().x/f, getVelocity().y, getVelocity().z/f);
                    }else{
                        info.cancel();
                    }

                    this.playSound(SoundEvents.ENTITY_SLIME_SQUISH, 1f, 1f);
                    SlimeBounceHandler.Companion.addBounceHandler(player, getVelocity().y);

                }else if(!getWorld().isClient && isSneaking()) {
                    if(fallDistance > 5) this.fallDistance = 5;
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("HEAD"), method = "shouldDropLoot", cancellable = true)
    private void shouldDropLoot(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = ((LivingEntity) ((Object) this));
        if(SpikeHelper.INSTANCE.shouldCancelLootDrop(livingEntity)) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public boolean kibe$isCursed() {
        return dataTracker.get(CURSED);
    }
}
