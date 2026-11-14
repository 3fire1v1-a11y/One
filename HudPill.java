package com.example.one;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Top-center rounded pill: clock and FPS. */
public final class HudPill {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private HudPill() {}

    public static void register() {
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            if (!OneMod.CONFIG.hudPill) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            String text = LocalTime.now().format(TIME) + "  |  " + mc.getFps() + " fps";
            int w = mc.font.width(text) + 16;
            int h = 14;
            int x = (mc.getWindow().getGuiScaledWidth() - w) / 2;
            int y = 4;

            graphics.fill(x, y, x + w, y + h, 0xB0101014);
            graphics.drawString(mc.font, text, x + 8, y + 3, 0xFFFFFFFF, false);
        });
    }
}
