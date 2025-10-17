package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.enchantment.EnchantmentCompendium;
import io.github.lucaargolo.kibe.item.ItemCompendium;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow @Final private Property levelCost;

    @Shadow private @Nullable String newItemName;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getOrDefault(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0), method = "updateResult", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onUpdateResult(CallbackInfo ci, ItemStack input, int i, long l, int j, ItemStack output, ItemStack extra, ItemEnchantmentsComponent.Builder builder) {
        if(output.isIn(ItemTags.FOOT_ARMOR_ENCHANTABLE) && output.getEnchantments().getEnchantments().stream().noneMatch(enchantment -> EnchantmentCompendium.INSTANCE.getSLIMY().equals(enchantment.getKey().orElse(null))) && extra.isOf(ItemCompendium.INSTANCE.getSLIME_BOOTS())) {
            player.getWorld().getRegistryManager().getOptionalWrapper(RegistryKeys.ENCHANTMENT).flatMap(registry -> registry.getOptional(EnchantmentCompendium.INSTANCE.getSLIMY())).ifPresent(enchantment -> {
                output.addEnchantment(enchantment, 1);

                if (this.newItemName != null && !StringHelper.isBlank(this.newItemName)) {
                    if (!this.newItemName.equals(input.getName().getString())) {
                        output.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.newItemName));
                    }
                } else if (input.contains(DataComponentTypes.CUSTOM_NAME)) {
                    output.remove(DataComponentTypes.CUSTOM_NAME);
                }

                this.levelCost.set(4);
                this.output.setStack(0, output);
                this.sendContentUpdates();
                ci.cancel();
            });
        }
    }

}
