package com.example.one;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Toggles, colors, and knife settings. Saved to config/one.json. */
public class OneConfig {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("one.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean hudPill = true;
    public boolean jumpRings = true;
    public boolean trails = true;
    public boolean fireflies = true;
    public boolean shockwaves = true;
    public boolean sky = true;           // aurora and shooting stars
    public boolean galaxySky = false;    // starfield and nebula dome
    public boolean weather = false;      // heavy blizzard
    public boolean snow = false;         // custom gentle snow
    public boolean rain = false;         // custom rain with splashes
    public boolean glassGround = false;  // glassy sparkling floor with footstep ripples
    public boolean glassPlayers = false; // ice-blue glass tint and shimmer on players
    public boolean cyberspace = false;   // neon grid floor and falling code
    public boolean chinaHat = false;     // cone hat on every player
    public boolean targetEffects = true; // marker on your target and hit bursts
    public boolean rainbow = false;      // rainbow colors for trails, rings, hat

    /** Particle amount multiplier, 0.25 to 2.0. Lower it if FPS drops. */
    public float density = 1.0f;

    // Knife customization. Tint 0 = blade, 1 = handle, 2 = accent.
    public int knifeBlade  = 0xFFE6DDFF;
    public int knifeHandle = 0xFF2B1B4A;
    public int knifeAccent = 0xFFFF7AD9;

    /** Multiplier for flip and inspect speed. 0.25 to 3.0. */
    public float knifeAnimSpeed = 1.0f;

    public void load() {
        try {
            if (!Files.exists(PATH)) return;
            OneConfig s = GSON.fromJson(Files.readString(PATH), OneConfig.class);
            if (s == null) return;
            hudPill = s.hudPill;
            jumpRings = s.jumpRings;
            trails = s.trails;
            fireflies = s.fireflies;
            shockwaves = s.shockwaves;
            sky = s.sky;
            galaxySky = s.galaxySky;
            weather = s.weather;
            snow = s.snow;
            rain = s.rain;
            glassGround = s.glassGround;
            glassPlayers = s.glassPlayers;
            cyberspace = s.cyberspace;
            chinaHat = s.chinaHat;
            targetEffects = s.targetEffects;
            rainbow = s.rainbow;
            density = Math.max(0.25f, Math.min(2.0f, s.density == 0f ? 1f : s.density));
            knifeBlade = s.knifeBlade;
            knifeHandle = s.knifeHandle;
            knifeAccent = s.knifeAccent;
            knifeAnimSpeed = Math.max(0.25f, Math.min(3.0f, s.knifeAnimSpeed));
        } catch (Exception e) {
            // Bad or missing file: keep defaults
        }
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (Exception e) {
            // Could not write: settings still apply for this session
        }
    }
}
