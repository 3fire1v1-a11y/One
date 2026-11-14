package com.example.one;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Jump rings (expanding), motion trails, fireflies, shockwaves. */
public final class ParticleEffects {
    private static final int PURPLE = 0xB56CFF;
    private static final int PINK = 0xFF7AD9;
    private static final int[] FIREFLY_COLORS = {0x7CF5FF, 0xFFD36B, 0xFF7AD9, 0x9DFF8A, 0xB56CFF};

    private static boolean wasOnGround = true;
    private static Vec3 lastPos = null;
    private static int ticks = 0;
    private static final List<Ring> rings = new ArrayList<>();

    private ParticleEffects() {}

    /** A ring that grows every tick until its life runs out. */
    private static final class Ring {
        final Vec3 center;
        final int color;
        final int color2;
        double radius;
        final double speed;
        int life;
        Ring(Vec3 c, int color, int color2, double r0, double speed, int life) {
            this.center = c; this.color = color; this.color2 = color2;
            this.radius = r0; this.speed = speed; this.life = life;
        }
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ClientLevel level = mc.level;
            Player player = mc.player;
            if (level == null || player == null || mc.isPaused()) return;
            ticks++;

            tickJumpRings(player);
            tickTrails(level, player);
            tickFireflies(level, player);
            tickRings(level);
        });
    }

    /** Adds an expanding ring. Used by jump rings, shockwaves, glass ripples and hit effects. */
    public static void addRing(Vec3 at, int color, int color2, double startRadius, double speed, int life) {
        rings.add(new Ring(at, color, color2, startRadius, speed, life));
    }

    public static void spawnShockwave(Vec3 at) {
        if (OneMod.CONFIG.shockwaves) addRing(at, PURPLE, PINK, 0.5, 0.25, 30);
    }

    private static void tickJumpRings(Player p) {
        boolean onGround = p.onGround();
        if (OneMod.CONFIG.jumpRings && onGround != wasOnGround) {
            Vec3 at = p.position().add(0, 0.05, 0);
            int a = OneMod.CONFIG.rainbow ? Fx.rainbow(ticks * 0.02) : PURPLE;
            int b = OneMod.CONFIG.rainbow ? Fx.rainbow(ticks * 0.02 + 0.4) : PINK;
            if (!onGround) {
                // Takeoff: two quick rings
                addRing(at, a, b, 0.3, 0.10, 12);
                addRing(at, b, a, 0.15, 0.16, 9);
            } else {
                // Landing: one bigger ring
                addRing(at, a, b, 0.4, 0.16, 14);
            }
        }
        wasOnGround = onGround;
    }

    private static void tickTrails(ClientLevel level, Player p) {
        Vec3 now = p.position();
        Vec3 prev = lastPos;
        lastPos = now;
        if (!OneMod.CONFIG.trails || prev == null) return;

        double speed = now.subtract(prev).horizontalDistance();
        boolean moving = speed > 0.08 || Math.abs(now.y - prev.y) > 0.08;
        if (!moving) return;

        int n = Fx.count(3);
        for (int i = 0; i < n; i++) {
            // Spread along the path since last tick so the ribbon stays continuous at speed
            double f = Fx.rnd(0, 1);
            Vec3 at = prev.lerp(now, f);
            int color = OneMod.CONFIG.rainbow
                    ? Fx.rainbow(ticks * 0.015 + i * 0.1)
                    : Fx.mix(PURPLE, PINK, (float) Fx.rnd(0, 1));
            Fx.dust(level, color, 0.9f, at.x + Fx.rnd(-0.12, 0.12), at.y + Fx.rnd(0.05, 1.0), at.z + Fx.rnd(-0.12, 0.12));
        }
    }

    private static void tickFireflies(ClientLevel level, Player p) {
        if (!OneMod.CONFIG.fireflies) return;
        if (Fx.irnd(4) != 0) return;
        Vec3 c = p.position();
        int color = FIREFLY_COLORS[Fx.irnd(FIREFLY_COLORS.length)];
        level.addParticle(new net.minecraft.core.particles.DustParticleOptions(color, 0.5f),
                c.x + Fx.rnd(-14, 14), c.y + Fx.rnd(0.5, 6), c.z + Fx.rnd(-14, 14),
                Fx.rnd(-0.05, 0.05), Fx.rnd(-0.01, 0.03), Fx.rnd(-0.05, 0.05));
    }

    private static void tickRings(ClientLevel level) {
        Iterator<Ring> it = rings.iterator();
        while (it.hasNext()) {
            Ring r = it.next();
            r.radius += r.speed;
            int points = Fx.count(Math.max(12, (int) (r.radius * 14)));
            double offset = Fx.rnd(0, Math.PI * 2);
            for (int i = 0; i < points; i++) {
                double a = offset + (Math.PI * 2 / points) * i;
                int color = (i & 1) == 0 ? r.color : r.color2;
                Fx.dust(level, color, 0.9f, r.center.x + Math.cos(a) * r.radius, r.center.y, r.center.z + Math.sin(a) * r.radius);
            }
            if (--r.life <= 0) it.remove();
        }
    }
}
