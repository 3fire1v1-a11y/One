package com.example.one;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Sky and weather, all particle based (no render hooks).
 * Sky: aurora curtain, shooting stars, and a galaxy dome (stars plus nebula bands).
 * Weather: blizzard, custom snow, custom rain with ground splashes.
 * Sky particles use addAlwaysVisibleParticle so they are not culled at 32 blocks.
 * These are approximations of shaders, not real GLSL.
 */
public final class SkyWeatherEffects {
    private static final int[] AURORA = {0xB56CFF, 0x7CF5FF, 0x9DFF8A, 0xFF7AD9};
    private static final int[] NEBULA = {0x3B1C7A, 0x7A2CBF, 0xFF4FD8, 0x2C6BFF, 0x00D5FF};
    private static final List<Star> stars = new ArrayList<>();
    private static int ticks = 0;

    private SkyWeatherEffects() {}

    private static final class Star {
        Vec3 pos;
        final Vec3 vel;
        int life = 14;
        Star(Vec3 pos, Vec3 vel) { this.pos = pos; this.vel = vel; }
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ClientLevel level = mc.level;
            Player player = mc.player;
            if (level == null || player == null || mc.isPaused()) return;
            ticks++;

            OneConfig c = OneMod.CONFIG;
            if (c.sky) {
                tickAurora(level, player);
                tickShootingStars(level, player);
            }
            if (c.galaxySky) tickGalaxy(level, player);
            if (c.weather) tickBlizzard(level, player);
            if (c.snow) tickSnow(level, player);
            if (c.rain) tickRain(level, player);
        });
    }

    private static void tickAurora(ClientLevel level, Player p) {
        if (ticks % 2 != 0) return;
        Vec3 c = p.position();
        int color = AURORA[(ticks / 40) % AURORA.length];
        int next = AURORA[(ticks / 40 + 1) % AURORA.length];
        float blend = (ticks % 40) / 40f;
        int n = Fx.count(8);
        for (int i = 0; i < n; i++) {
            double a = Fx.rnd(0, Math.PI * 2);
            double r = Fx.rnd(45, 65);
            double h = 40 + Math.sin(a * 3 + ticks * 0.05) * 7 + Fx.rnd(0, 10);
            Fx.farDust(level, Fx.mix(color, next, blend), 4.0f,
                    c.x + Math.cos(a) * r, c.y + h, c.z + Math.sin(a) * r);
        }
    }

    private static void tickGalaxy(ClientLevel level, Player p) {
        Vec3 c = p.position();
        int n = Fx.count(14);
        for (int i = 0; i < n; i++) {
            // Random point on the upper dome
            double az = Fx.rnd(0, Math.PI * 2);
            double el = Math.asin(Fx.rnd(0.12, 1.0));
            double r = 70;
            double x = c.x + Math.cos(az) * Math.cos(el) * r;
            double y = c.y + Math.sin(el) * r;
            double z = c.z + Math.sin(az) * Math.cos(el) * r;

            // Nebula bands: color follows a slow wave across the dome
            double wave = Math.sin(az * 2 + el * 3 + ticks * 0.004) * 0.5 + 0.5;
            if (Fx.irnd(3) == 0) {
                int idx = (int) (wave * (NEBULA.length - 1));
                Fx.farDust(level, NEBULA[idx], 4.0f, x, y, z);
            } else if (Fx.irnd(4) == 0) {
                // Bright star
                Fx.farDust(level, 0xFFFFFF, 1.6f, x, y, z);
            } else {
                Fx.farDust(level, Fx.mix(0xBFD8FF, 0xFFE9B0, (float) Fx.rnd(0, 1)), 1.0f, x, y, z);
            }
        }
    }

    private static void tickShootingStars(ClientLevel level, Player p) {
        if (ticks % 90 == 0 && Fx.irnd(2) == 0) {
            Vec3 c = p.position();
            Vec3 start = new Vec3(c.x + Fx.rnd(-40, 40), c.y + Fx.rnd(35, 55), c.z + Fx.rnd(-40, 40));
            stars.add(new Star(start, new Vec3(Fx.rnd(-1.2, 1.2), Fx.rnd(-0.9, -0.5), Fx.rnd(-1.2, 1.2))));
        }
        Iterator<Star> it = stars.iterator();
        while (it.hasNext()) {
            Star s = it.next();
            level.addAlwaysVisibleParticle(ParticleTypes.END_ROD, s.pos.x, s.pos.y, s.pos.z, 0, 0, 0);
            s.pos = s.pos.add(s.vel);
            if (--s.life <= 0) it.remove();
        }
    }

    private static void tickBlizzard(ClientLevel level, Player p) {
        Vec3 c = p.position();
        int n = Fx.count(10);
        for (int i = 0; i < n; i++) {
            level.addParticle(ParticleTypes.SNOWFLAKE,
                    c.x + Fx.rnd(-16, 16), c.y + Fx.rnd(2, 10), c.z + Fx.rnd(-16, 16),
                    0.25, -0.15, 0.05);
        }
    }

    /** Gentle swirling snow with a few tinted sparkles. Skips columns under a roof. */
    private static void tickSnow(ClientLevel level, Player p) {
        Vec3 c = p.position();
        double wind = Math.sin(ticks * 0.02) * 0.04;
        int n = Fx.count(8);
        for (int i = 0; i < n; i++) {
            double x = c.x + Fx.rnd(-14, 14);
            double z = c.z + Fx.rnd(-14, 14);
            if (covered(level, x, z, c.y)) continue;
            double y = c.y + Fx.rnd(3, 12);
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z,
                    wind + Math.sin(ticks * 0.05 + x) * 0.02, -0.04, Math.cos(ticks * 0.05 + z) * 0.02);
            if (Fx.irnd(5) == 0) {
                Fx.dust(level, Fx.mix(0xFFFFFF, 0xBFE8FF, (float) Fx.rnd(0, 1)), 0.5f, x, y, z);
            }
        }
    }

    /** Falling droplets plus splashes where they land. Skips columns under a roof. */
    private static void tickRain(ClientLevel level, Player p) {
        Vec3 c = p.position();
        int n = Fx.count(14);
        for (int i = 0; i < n; i++) {
            double x = c.x + Fx.rnd(-14, 14);
            double z = c.z + Fx.rnd(-14, 14);
            if (covered(level, x, z, c.y)) continue;
            level.addParticle(ParticleTypes.FALLING_WATER, x, c.y + Fx.rnd(4, 12), z, 0, -0.5, 0);
        }
        int splashes = Fx.count(6);
        for (int i = 0; i < splashes; i++) {
            double x = c.x + Fx.rnd(-12, 12);
            double z = c.z + Fx.rnd(-12, 12);
            if (covered(level, x, z, c.y)) continue;
            int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(x), (int) Math.floor(z));
            if (Math.abs(top - c.y) > 12) continue;
            level.addParticle(ParticleTypes.RAIN, x, top + 0.1, z, 0, 0, 0);
        }
    }

    private static boolean covered(ClientLevel level, double x, double z, double playerY) {
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(x), (int) Math.floor(z));
        return top > playerY + 2;
    }
}
