package io.github.lucaargolo.kibe.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lucaargolo.kibe.block.EntangledChest;
import io.github.lucaargolo.kibe.block.EntangledTank;
import io.github.lucaargolo.kibe.blockentity.BlockEntityCompendium;
import io.github.lucaargolo.kibe.utils.ModIdentifier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final
    MinecraftClient client;

    @Unique
    private static final Identifier TEXTURE = ModIdentifier.INSTANCE.of("textures/gui/entangled_rune_set.png");

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getOverlay()Lnet/minecraft/client/gui/screen/Overlay;", ordinal = 0), method = "render", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void renderEntangledRuneSet(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci, boolean bl, int i, int j, Window window, Matrix4f matrix4f, Matrix4fStack matrix4fStack, DrawContext context) {
        if(client.world != null && client.player != null) {
            assert client.crosshairTarget != null;
            ItemStack stack = client.player.getStackInHand(Hand.MAIN_HAND);
            if(stack.getItem() instanceof DyeItem && client.crosshairTarget.getType() == HitResult.Type.BLOCK && client.crosshairTarget instanceof BlockHitResult blockHitResult) {
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState state = client.world.getBlockState(pos);
                Block block = state.getBlock();
                if(block instanceof EntangledChest || block instanceof EntangledTank) {
                    Integer selected = EntangledChest.Companion.getRuneByPos((blockHitResult.getPos().x-pos.getX()), (blockHitResult.getPos().z-pos.getZ()));
                    client.world.getBlockEntity(pos, BlockEntityCompendium.INSTANCE.getENTANGLED_CHEST()).ifPresent(chest -> {
                        drawEntangledRuneSet(context, window.getScaledWidth()/2 - 46, window.getScaledHeight()/2 + 8, chest.getRuneColors(), selected);
                    });
                    client.world.getBlockEntity(pos, BlockEntityCompendium.INSTANCE.getENTANGLED_TANK()).ifPresent(tank -> {
                        drawEntangledRuneSet(context, window.getScaledWidth()/2 - 46, window.getScaledHeight()/2 + 8, tank.getRuneColors(), selected);
                    });
                }
            }
        }

    }

    @Unique
    private static void drawEntangledRuneSet(DrawContext context, int startX, int startY, DyeColor[] runeColors, Integer selected) {
        context.setShaderColor(1.0F, 1.0F, 1.0F, 0.75f);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        context.drawTexture(TEXTURE, startX, startY, 0, 0, 92, 20);

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for(int idx = 0; idx < runeColors.length; idx++) {
            DyeColor col = runeColors[idx];
            context.drawTexture(TEXTURE, startX + 7 + idx*10, startY + 5, col.getId()*8, 20, 8, 10);
        }

        if(selected != null) {
            context.drawTexture(TEXTURE, startX + 6 + selected * 10, startY + 4, 92, 0, 10, 12);
        }
    }

}
