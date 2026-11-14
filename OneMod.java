package com.example.one;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class OneMod implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "one";
    public static final OneConfig CONFIG = new OneConfig();

    private static KeyMapping.Category keyCategory;

    /** One shared key category. Registering the same category twice throws, so everything uses this. */
    public static KeyMapping.Category keyCategory() {
        if (keyCategory == null) {
            keyCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "keys"));
        }
        return keyCategory;
    }

    @Override
    public void onInitialize() {
        CONFIG.load();
        KnifeItems.register();
    }

    @Override
    public void onInitializeClient() {
        HudPill.register();
        ParticleEffects.register();
        SkyWeatherEffects.register();
        GroundEffects.register();
        PlayerEffects.register();
        KnifeItems.registerColors();
        KnifeAnimator.register();
        KnifeConfigScreen.registerKey();
        VisualToggleScreen.registerKey();
    }
}
