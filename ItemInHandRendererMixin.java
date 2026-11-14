package com.example.one.mixin;

import com.example.one.KnifeAnimator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    // Parameter list must match ItemInHandRenderer#renderArmWithItem for the current 1.21.x build.
    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void one$pushKnifePose(AbstractClientPlayer player, float partialTick, float pitch,
                                   InteractionHand hand, float swing, ItemStack stack, float equip,
                                   PoseStack pose, SubmitNodeCollector buffer, int light, CallbackInfo ci) {
        pose.pushPose();
        if (hand == InteractionHand.MAIN_HAND && KnifeAnimator.isKnife(stack)) {
            KnifeAnimator.applyTransform(pose, partialTick);
        }
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void one$popKnifePose(AbstractClientPlayer player, float partialTick, float pitch,
                                  InteractionHand hand, float swing, ItemStack stack, float equip,
                                  PoseStack pose, SubmitNodeCollector buffer, int light, CallbackInfo ci) {
        pose.popPose();
    }
}
