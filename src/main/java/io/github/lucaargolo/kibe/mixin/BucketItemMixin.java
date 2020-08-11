package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.items.ItemCompendiumKt;
import io.github.lucaargolo.kibe.items.miscellaneous.WoodenBucket;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BucketItem.class)
public class BucketItemMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/Fluid;getBucketItem()Lnet/minecraft/item/Item;"), method = "use")
    public Item getBucketItem(Fluid fluid) {
        Item oldBucket = fluid.getBucketItem();
        if(((Object) this) instanceof WoodenBucket) {
            if(oldBucket == Items.BUCKET) return ItemCompendiumKt.getWOODEN_BUCKET();
            if(oldBucket == Items.WATER_BUCKET) return ItemCompendiumKt.getWATER_WOODEN_BUCKET();
        }
        return oldBucket;
    }

}
