package com.example.one;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** On/off switches for every visual effect. Press L in game. */
public class VisualToggleScreen extends Screen {
    private static KeyMapping openKey;

    private final Screen parent;

    public VisualToggleScreen(Screen parent) {
        super(Component.literal("One: Visuals"));
        this.parent = parent;
    }

    public static void registerKey() {
        KeyMapping.Category category = OneMod.keyCategory();
        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.one.visuals", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, category));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new VisualToggleScreen(null));
            }
        });
    }

    private void toggle(String name, BooleanSupplier get, Consumer<Boolean> set, int x, int y) {
        addRenderableWidget(Button.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            OneMod.CONFIG.save();
            b.setMessage(label(name, get.getAsBoolean()));
        }).bounds(x, y, 150, 20).build());
    }

    private static Component label(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "ON" : "OFF"));
    }

    @Override
    protected void init() {
        OneConfig c = OneMod.CONFIG;
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 5;
        int y = Math.max(30, this.height / 2 - 100);
        int step = 24;

        toggle("Aurora + stars", () -> c.sky, v -> c.sky = v, left, y);
        toggle("Galaxy sky", () -> c.galaxySky, v -> c.galaxySky = v, right, y);
        y += step;
        toggle("Custom snow", () -> c.snow, v -> c.snow = v, left, y);
        toggle("Custom rain", () -> c.rain, v -> c.rain = v, right, y);
        y += step;
        toggle("Blizzard", () -> c.weather, v -> c.weather = v, left, y);
        toggle("Cyberspace", () -> c.cyberspace, v -> c.cyberspace = v, right, y);
        y += step;
        toggle("Glass ground", () -> c.glassGround, v -> c.glassGround = v, left, y);
        toggle("Glass players", () -> c.glassPlayers, v -> c.glassPlayers = v, right, y);
        y += step;
        toggle("China hat", () -> c.chinaHat, v -> c.chinaHat = v, left, y);
        toggle("Target effects", () -> c.targetEffects, v -> c.targetEffects = v, right, y);
        y += step;
        toggle("Trails", () -> c.trails, v -> c.trails = v, left, y);
        toggle("Jump rings", () -> c.jumpRings, v -> c.jumpRings = v, right, y);
        y += step;
        toggle("Rainbow colors", () -> c.rainbow, v -> c.rainbow = v, left, y);
        toggle("Fireflies", () -> c.fireflies, v -> c.fireflies = v, right, y);
        y += step + 6;

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(this.width / 2 - 100, y, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);
        gfx.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
