package com.example.one;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/** Knife color hex inputs and an animation speed slider. Press K in game to open. */
public class KnifeConfigScreen extends Screen {
    private static final float MIN_SPEED = 0.25f;
    private static final float MAX_SPEED = 3.0f;
    private static KeyMapping openKey;

    private final Screen parent;
    private EditBox bladeBox;
    private EditBox handleBox;
    private EditBox accentBox;
    private float speed;
    private String error = "";

    public KnifeConfigScreen(Screen parent) {
        super(Component.literal("One: Knife Settings"));
        this.parent = parent;
        this.speed = OneMod.CONFIG.knifeAnimSpeed;
    }

    public static void registerKey() {
        KeyMapping.Category category = OneMod.keyCategory();
        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.one.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new KnifeConfigScreen(null));
            }
        });
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 3;

        bladeBox = addRenderableWidget(new EditBox(this.font, cx - 100, y, 200, 20, Component.literal("Blade")));
        handleBox = addRenderableWidget(new EditBox(this.font, cx - 100, y + 30, 200, 20, Component.literal("Handle")));
        accentBox = addRenderableWidget(new EditBox(this.font, cx - 100, y + 60, 200, 20, Component.literal("Accent")));
        bladeBox.setMaxLength(7);
        handleBox.setMaxLength(7);
        accentBox.setMaxLength(7);
        bladeBox.setValue(hex(OneMod.CONFIG.knifeBlade));
        handleBox.setValue(hex(OneMod.CONFIG.knifeHandle));
        accentBox.setValue(hex(OneMod.CONFIG.knifeAccent));

        double initial = (speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED);
        addRenderableWidget(new AbstractSliderButton(cx - 100, y + 90, 200, 20, Component.empty(), initial) {
            { updateMessage(); }

            @Override
            protected void updateMessage() {
                setMessage(Component.literal("Anim speed: " + String.format("%.2fx", speedFromSlider())));
            }

            @Override
            protected void applyValue() {
                speed = speedFromSlider();
            }

            private float speedFromSlider() {
                return (float) (MIN_SPEED + this.value * (MAX_SPEED - MIN_SPEED));
            }
        });

        addRenderableWidget(Button.builder(Component.literal("Save"), b -> save())
                .bounds(cx - 100, y + 125, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
                .bounds(cx + 2, y + 125, 98, 20).build());
    }

    private void save() {
        Integer blade = parseHex(bladeBox.getValue());
        Integer handle = parseHex(handleBox.getValue());
        Integer accent = parseHex(accentBox.getValue());
        if (blade == null || handle == null || accent == null) {
            error = "Colors need 6 hex digits, like FF7AD9";
            return;
        }
        OneMod.CONFIG.knifeBlade = blade;
        OneMod.CONFIG.knifeHandle = handle;
        OneMod.CONFIG.knifeAccent = accent;
        OneMod.CONFIG.knifeAnimSpeed = speed;
        OneMod.CONFIG.save();
        onClose();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        int y = this.height / 3;
        gfx.drawCenteredString(this.font, this.title, cx, y - 25, 0xFFFFFFFF);
        gfx.drawString(this.font, "Blade", cx - 100, y - 10, 0xFFAAAAAA, false);
        gfx.drawString(this.font, "Handle", cx - 100, y + 20, 0xFFAAAAAA, false);
        gfx.drawString(this.font, "Accent", cx - 100, y + 50, 0xFFAAAAAA, false);
        drawSwatch(gfx, cx + 106, y, parseHex(bladeBox.getValue()));
        drawSwatch(gfx, cx + 106, y + 30, parseHex(handleBox.getValue()));
        drawSwatch(gfx, cx + 106, y + 60, parseHex(accentBox.getValue()));
        if (!error.isEmpty()) {
            gfx.drawCenteredString(this.font, error, cx, y + 150, 0xFFFF6666);
        }
    }

    private void drawSwatch(GuiGraphics gfx, int x, int y, Integer color) {
        int c = color == null ? 0xFF555555 : color;
        gfx.fill(x, y, x + 20, y + 20, c);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String hex(int argb) {
        return String.format("%06X", argb & 0xFFFFFF);
    }

    private static Integer parseHex(String s) {
        if (s == null) return null;
        String t = s.trim().replace("#", "");
        if (!t.matches("[0-9A-Fa-f]{6}")) return null;
        return 0xFF000000 | Integer.parseInt(t, 16);
    }
}
