package com.example.one.mixin;

import com.example.one.OneMod;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Glass players: tints every player model ice blue. The sparkles come from PlayerEffects.
 * require = 0 means a changed method name logs a warning instead of crashing the game.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    private static final int GLASS_TINT = 0xC8BFEFFF;

    @Inject(method = "getModelTint", at = @At("HEAD"), cancellable = true, require = 0)
    private void one$glassTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (OneMod.CONFIG.glassPlayers && state instanceof AvatarRenderState) {
            cir.setReturnValue(GLASS_TINT);
        }
    }
}
