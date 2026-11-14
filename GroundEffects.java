package com.example.one;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Glass ground: a pale sparkling floor with ripples under your feet.
 * Cyberspace: a neon grid on the floor, rising data streams and falling code.
 * Both find the surface near the player by scanning a few blocks down.
 */
public final class GroundEffects {
    private static final int CYAN = 0x00F0FF;
    private static final int MAGENTA = 0xFF2BD6;
    private static final int GREEN = 0x39FF14;
    private static int ticks = 0;
    private static Vec3 lastPos = null;

    private GroundEffects() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ClientLevel level = mc.level;
            Player player = mc.player;
            if (level == null || player == null || mc.isPaused()) return;
            ticks++;
            if (OneMod.CONFIG.glassGround) tickGlass(level, player);
            if (OneMod.CONFIG.cyberspace) tickCyber(level, player);
            lastPos = player.position();
        });
    }

    /** Y of the top of the first solid block under (x, z), searching near startY, or NaN. */
    private static double surface(ClientLevel level, int x, int z, int startY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy >= -4; dy--) {
            pos.set(x, startY + dy, z);
            if (level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir()) {
                return pos.getY();
            }
        }
        return Double.NaN;
    }

    private static void tickGlass(ClientLevel level, Player p) {
        Vec3 c = p.position();
        int startY = (int) Math.floor(c.y);
        int n = Fx.count(16);
        for (int i = 0; i < n; i++) {
            double x = c.x + Fx.rnd(-10, 10);
            double z = c.z + Fx.rnd(-10, 10);
            double y = surface(level, (int) Math.floor(x), (int) Math.floor(z), startY);
            if (Double.isNaN(y)) continue;
            Fx.dust(level, Fx.mix(0xDFF7FF, 0x9FDFFF, (float) Fx.rnd(0, 1)), 0.7f, x, y + 0.02, z);
            if (Fx.irnd(8) == 0) {
                level.addParticle(ParticleTypes.END_ROD, x, y + 0.05, z, 0, 0.01, 0);
            }
        }
        // Ripple under your feet while walking on the ground
        if (p.onGround() && lastPos != null && c.subtract(lastPos).horizontalDistance() > 0.06 && ticks % 6 == 0) {
            ParticleEffects.addRing(c.add(0, 0.03, 0), 0xDFF7FF, 0x9FDFFF, 0.2, 0.08, 10);
        }
    }

    private static void tickCyber(ClientLevel level, Player p) {
        Vec3 c = p.position();
        int startY = (int) Math.floor(c.y);

        // Neon grid lines along block edges
        int n = Fx.count(26);
        for (int i = 0; i < n; i++) {
            int cx = (int) Math.floor(c.x + Fx.rnd(-12, 12));
            int cz = (int) Math.floor(c.z + Fx.rnd(-12, 12));
            double y = surface(level, cx, cz, startY);
            if (Double.isNaN(y)) continue;
            boolean alongX = Fx.irnd(2) == 0;
            double x = cx + (alongX ? Fx.rnd(0, 1) : 0);
            double z = cz + (alongX ? 0 : Fx.rnd(0, 1));
            int color = ((cx + cz) & 1) == 0 ? CYAN : MAGENTA;
            if (OneMod.CONFIG.rainbow) color = Fx.rainbow(ticks * 0.01 + (cx + cz) * 0.05);
            Fx.dust(level, color, 0.8f, x, y + 0.02, z);
        }

        // Data streams rising from the floor
        if (ticks % 3 == 0) {
            int cx = (int) Math.floor(c.x + Fx.rnd(-10, 10));
            int cz = (int) Math.floor(c.z + Fx.rnd(-10, 10));
            double y = surface(level, cx, cz, startY);
            if (!Double.isNaN(y)) {
                int height = 3 + Fx.irnd(4);
                for (int i = 0; i < height; i++) {
                    Fx.dust(level, GREEN, 0.6f, cx + 0.5, y + 0.3 + i * 0.5, cz + 0.5);
                }
            }
        }

        // Falling code
        int rain = Fx.count(3);
        for (int i = 0; i < rain; i++) {
            level.addParticle(ParticleTypes.END_ROD,
                    c.x + Fx.rnd(-10, 10), c.y + Fx.rnd(5, 12), c.z + Fx.rnd(-10, 10),
                    0, -0.2, 0);
        }
    }
}
