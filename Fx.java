package com.example.one;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;

import java.util.concurrent.ThreadLocalRandom;

/** Small helpers shared by the effect classes. */
public final class Fx {
    private Fx() {}

    public static double rnd(double lo, double hi) {
        return ThreadLocalRandom.current().nextDouble(lo, hi);
    }

    public static int irnd(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    /** How many particles to spawn, scaled by the density setting (at least 1 when base > 0). */
    public static int count(int base) {
        return Math.max(1, Math.round(base * OneMod.CONFIG.density));
    }

    /** Fully saturated rainbow color (RGB, no alpha) for t in any range; one loop per 1.0. */
    public static int rainbow(double t) {
        float h = (float) (t - Math.floor(t));
        float r = clamp(Math.abs(h * 6f - 3f) - 1f);
        float g = clamp(2f - Math.abs(h * 6f - 2f));
        float b = clamp(2f - Math.abs(h * 6f - 4f));
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    /** Linear blend between two RGB colors, f in 0..1. */
    public static int mix(int a, int b, float f) {
        int ar = a >> 16 & 255, ag = a >> 8 & 255, ab = a & 255;
        int br = b >> 16 & 255, bg = b >> 8 & 255, bb = b & 255;
        int r = (int) (ar + (br - ar) * f);
        int g = (int) (ag + (bg - ag) * f);
        int bl = (int) (ab + (bb - ab) * f);
        return (r << 16) | (g << 8) | bl;
    }

    public static void dust(ClientLevel level, int rgb, float scale, double x, double y, double z) {
        level.addParticle(new DustParticleOptions(rgb & 0xFFFFFF, scale), x, y, z, 0, 0, 0);
    }

    /** Same as dust() but ignores the 32 block particle distance limit (for sky effects). */
    public static void farDust(ClientLevel level, int rgb, float scale, double x, double y, double z) {
        level.addAlwaysVisibleParticle(new DustParticleOptions(rgb & 0xFFFFFF, scale), x, y, z, 0, 0, 0);
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
