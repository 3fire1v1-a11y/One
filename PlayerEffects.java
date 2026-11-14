package com.example.one;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** China hat, glass player shimmer, target marker, and hit bursts. */
public final class PlayerEffects {
    private static final int PURPLE = 0xB56CFF;
    private static final int PINK = 0xFF7AD9;
    private static final int RED = 0xFF3B5C;
    private static int ticks = 0;
    private static LivingEntity lastHit = null;
    private static int lastHitTick = -1000;

    private PlayerEffects() {}

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world instanceof ClientLevel level && OneMod.CONFIG.targetEffects) {
                hitBurst(level, entity);
                if (entity instanceof LivingEntity le) {
                    lastHit = le;
                    lastHitTick = ticks;
                }
            }
            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ClientLevel level = mc.level;
            Player self = mc.player;
            if (level == null || self == null || mc.isPaused()) return;
            ticks++;

            if (OneMod.CONFIG.chinaHat || OneMod.CONFIG.glassPlayers) {
                for (Player p : level.players()) {
                    if (p.isInvisible() || p.isSpectator()) continue;
                    if (p.distanceToSqr(self) > 30 * 30) continue;
                    boolean firstPersonSelf = p == self && mc.options.getCameraType().isFirstPerson();
                    if (OneMod.CONFIG.chinaHat && !firstPersonSelf && ticks % 2 == 0) chinaHat(level, p);
                    if (OneMod.CONFIG.glassPlayers && !firstPersonSelf) glassShimmer(level, p);
                }
            }
            if (OneMod.CONFIG.targetEffects) tickTarget(mc, level, self);
        });
    }

    /** Cone of dust on top of the head, purple at the brim fading to pink at the tip. */
    private static void chinaHat(ClientLevel level, Player p) {
        double baseY = p.getY() + p.getBbHeight() + 0.02;
        double coneH = 0.32;
        double brim = 0.6;
        int n = Fx.count(9);
        for (int i = 0; i < n; i++) {
            double t = Fx.rnd(0, 1);
            double r = brim * (1 - t);
            double a = Fx.rnd(0, Math.PI * 2);
            int color = OneMod.CONFIG.rainbow
                    ? Fx.rainbow(ticks * 0.01 + a / (Math.PI * 2))
                    : Fx.mix(PURPLE, PINK, (float) t);
            Fx.dust(level, color, 0.7f, p.getX() + Math.cos(a) * r, baseY + t * coneH, p.getZ() + Math.sin(a) * r);
        }
        // Brim edge so the silhouette reads clearly
        int edge = Fx.count(4);
        for (int i = 0; i < edge; i++) {
            double a = Fx.rnd(0, Math.PI * 2);
            Fx.dust(level, PINK, 0.8f, p.getX() + Math.cos(a) * brim, baseY, p.getZ() + Math.sin(a) * brim);
        }
    }

    /** Sparkles inside the body box to sell the glass look (the tint comes from the renderer mixin). */
    private static void glassShimmer(ClientLevel level, Player p) {
        if (Fx.irnd(2) != 0) return;
        double w = p.getBbWidth() * 0.5;
        double x = p.getX() + Fx.rnd(-w, w);
        double y = p.getY() + Fx.rnd(0, p.getBbHeight());
        double z = p.getZ() + Fx.rnd(-w, w);
        if (Fx.irnd(5) == 0) {
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.005, 0);
        } else {
            Fx.dust(level, Fx.mix(0xDFF7FF, 0x8FD8FF, (float) Fx.rnd(0, 1)), 0.5f, x, y, z);
        }
    }

    /** Target marker: orbiting sparks, a ground ring, and a pointer above the head. */
    private static void tickTarget(Minecraft mc, ClientLevel level, Player self) {
        LivingEntity target = null;
        Entity pick = mc.crosshairPickEntity;
        if (pick instanceof LivingEntity le && le != self && le.isAlive()) {
            target = le;
        } else if (lastHit != null && lastHit.isAlive() && ticks - lastHitTick < 40) {
            target = lastHit;
        }
        if (target == null) return;

        double w = target.getBbWidth();
        double h = target.getBbHeight();
        double radius = w * 0.8 + 0.3;
        int color = OneMod.CONFIG.rainbow ? Fx.rainbow(ticks * 0.02) : RED;

        // Three sparks spiralling up and down the body
        double bob = (Math.sin(ticks * 0.12) + 1) / 2 * h;
        for (int i = 0; i < 3; i++) {
            double a = ticks * 0.25 + i * (Math.PI * 2 / 3);
            Fx.dust(level, color, 0.8f, target.getX() + Math.cos(a) * radius, target.getY() + bob, target.getZ() + Math.sin(a) * radius);
        }

        // Ground ring
        if (ticks % 2 == 0) {
            int points = Fx.count(10);
            for (int i = 0; i < points; i++) {
                double a = (Math.PI * 2 / points) * i + ticks * 0.05;
                Fx.dust(level, PINK, 0.6f, target.getX() + Math.cos(a) * radius, target.getY() + 0.03, target.getZ() + Math.sin(a) * radius);
            }
        }

        // Pointer above the head: a small ring with a tail pointing down
        double top = target.getY() + h + 0.45 + Math.sin(ticks * 0.15) * 0.08;
        for (int i = 0; i < 4; i++) {
            double a = ticks * 0.3 + i * (Math.PI / 2);
            Fx.dust(level, color, 0.6f, target.getX() + Math.cos(a) * 0.15, top + 0.15, target.getZ() + Math.sin(a) * 0.15);
        }
        for (int i = 0; i < 3; i++) {
            Fx.dust(level, color, 0.7f, target.getX(), top - i * 0.1, target.getZ());
        }
    }

    /** Radial spray plus a small ring where you hit something. */
    private static void hitBurst(ClientLevel level, Entity e) {
        Vec3 c = e.position().add(0, e.getBbHeight() * 0.5, 0);
        int n = Fx.count(18);
        for (int i = 0; i < n; i++) {
            double a = Fx.rnd(0, Math.PI * 2);
            double up = Fx.rnd(-0.3, 0.5);
            level.addParticle(ParticleTypes.END_ROD, c.x, c.y, c.z,
                    Math.cos(a) * 0.25, up * 0.25, Math.sin(a) * 0.25);
        }
        ParticleEffects.addRing(c, RED, PINK, 0.3, 0.18, 8);
    }
}
