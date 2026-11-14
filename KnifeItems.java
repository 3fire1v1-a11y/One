package com.example.one;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/** Katana, karambit, butterfly knife, greatsword. 3D models live in assets/one/models/item. */
public final class KnifeItems {
    public static final List<Item> ALL = new ArrayList<>();
    public static Item KATANA;
    public static Item KARAMBIT;
    public static Item BUTTERFLY_KNIFE;
    public static Item GREATSWORD;

    private KnifeItems() {}

    public static void register() {
        KATANA = reg("katana");
        KARAMBIT = reg("karambit");
        BUTTERFLY_KNIFE = reg("butterfly_knife");
        GREATSWORD = reg("greatsword");
    }

    private static Item reg(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(OneMod.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item item = new Item(new Item.Properties()
                .setId(key)
                .stacksTo(1)
                .component(DataComponents.ITEM_MODEL, id));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        ALL.add(registered);
        return registered;
    }

    /** Live color customization from OneConfig. */
    public static void registerColors() {
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> OneMod.CONFIG.knifeBlade;
            case 1 -> OneMod.CONFIG.knifeHandle;
            case 2 -> OneMod.CONFIG.knifeAccent;
            default -> 0xFFFFFFFF;
        }, ALL.toArray(new Item[0]));
    }
}
