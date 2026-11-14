package com.example.one;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Per-knife animation styles. B = signature move, V = inspect. Both use OneConfig.knifeAnimSpeed.
 *   Katana:     B slash arc, V blade roll
 *   Karambit:   B finger spin, V wobble
 *   Butterfly:  B open and close, V trick flip
 *   Greatsword: B overhead chop, V heavy spin
 * Visual only: other players won't see it.
 */
public final class KnifeAnimator {
    public enum Knife { NONE, KATANA, KARAMBIT, BUTTERFLY, GREATSWORD }

    private static Knife currentKnife = Knife.NONE;
    private static boolean signature = false;
    private static float tick = 0f;
    private static int length = 0;
    private static KeyMapping inspectKey;
    private static KeyMapping flipKey;

    private KnifeAnimator() {}

    public static void register() {
        KeyMapping.Category category = OneMod.keyCategory();
        inspectKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.one.inspect", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, category));
        flipKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.one.flip", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (currentKnife != Knife.NONE) {
                tick += OneMod.CONFIG.knifeAnimSpeed;
                if (tick >= length) {
                    currentKnife = Knife.NONE;
                    tick = 0f;
                }
            }
            if (currentKnife == Knife.NONE && mc.player != null && mc.screen == null) {
                Knife held = kindOf(mc.player.getMainHandItem());
                if (held != Knife.NONE) {
                    while (inspectKey.consumeClick()) start(held, false);
                    while (flipKey.consumeClick()) start(held, true);
                } else {
                    while (inspectKey.consumeClick()) { }
                    while (flipKey.consumeClick()) { }
                }
            }
        });
    }

    private static void start(Knife knife, boolean sig) {
        currentKnife = knife;
        signature = sig;
        tick = 0f;
        length = lengthOf(knife, sig);
    }

    private static int lengthOf(Knife knife, boolean sig) {
        return switch (knife) {
            case KATANA -> sig ? 12 : 30;
            case KARAMBIT -> sig ? 24 : 40;
            case BUTTERFLY -> sig ? 18 : 30;
            case GREATSWORD -> sig ? 20 : 44;
            default -> 0;
        };
    }

    public static Knife kindOf(ItemStack stack) {
        if (stack.isEmpty()) return Knife.NONE;
        if (stack.is(KnifeItems.KATANA)) return Knife.KATANA;
        if (stack.is(KnifeItems.KARAMBIT)) return Knife.KARAMBIT;
        if (stack.is(KnifeItems.BUTTERFLY_KNIFE)) return Knife.BUTTERFLY;
        if (stack.is(KnifeItems.GREATSWORD)) return Knife.GREATSWORD;
        return Knife.NONE;
    }

    public static boolean isKnife(ItemStack stack) {
        return kindOf(stack) != Knife.NONE;
    }

    /** Applies the current knife's animation to the held item's pose. Call between push and pop. */
    public static void applyTransform(PoseStack pose, float partialTick) {
        if (currentKnife == Knife.NONE || length <= 0) return;
        float t = Mth.clamp((tick + partialTick * OneMod.CONFIG.knifeAnimSpeed) / length, 0.0f, 1.0f);
        float s = Mth.sin(t * Mth.PI);

        switch (currentKnife) {
            case KATANA -> {
                if (signature) {
                    pose.mulPose(Axis.XP.rotationDegrees(-s * 80.0f));
                    pose.translate(0.0, 0.0, -0.1 * s);
                } else {
                    pose.mulPose(Axis.ZP.rotationDegrees(t * 180.0f));
                    pose.translate(0.0, 0.05 * s, 0.0);
                }
            }
            case KARAMBIT -> {
                if (signature) {
                    pose.mulPose(Axis.YP.rotationDegrees(t * 720.0f));
                } else {
                    pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(t * Mth.TWO_PI * 2.0f) * 20.0f * s));
                }
            }
            case BUTTERFLY -> {
                if (signature) {
                    pose.mulPose(Axis.YP.rotationDegrees(t * 180.0f));
                    pose.translate(0.1 * s, 0.0, 0.0);
                } else {
                    pose.mulPose(Axis.ZP.rotationDegrees(t * 360.0f));
                    pose.translate(0.0, 0.12 * s, 0.0);
                }
            }
            case GREATSWORD -> {
                if (signature) {
                    pose.mulPose(Axis.XP.rotationDegrees(-s * 120.0f));
                    pose.translate(0.0, -0.1 * s, 0.0);
                } else {
                    pose.mulPose(Axis.YP.rotationDegrees(t * 360.0f));
                    pose.translate(0.0, -0.05 * s, 0.0);
                }
            }
            default -> { }
        }
    }
}
