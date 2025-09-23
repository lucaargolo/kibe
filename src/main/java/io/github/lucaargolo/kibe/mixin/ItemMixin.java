package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.utils.helper.TooltipHelper;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(at = @At("HEAD"), method = "appendTooltip")
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        Map<ItemConvertible, List<Text>> map = TooltipHelper.INSTANCE.getTooltipRegistry();
        Item item = stack.getItem();
        if(item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            if(map.containsKey(block)) {
                tooltip.addAll(map.get(block));
            }
        }else{
            if(map.containsKey(item)) {
                tooltip.addAll(map.get(item));
            }
        }
    }

}
