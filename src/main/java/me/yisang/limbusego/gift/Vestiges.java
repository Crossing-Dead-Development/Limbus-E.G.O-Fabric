package me.yisang.limbusego.gift;

import me.yisang.limbusego.LimbusEGOMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 殘影升級材料。4 種殘影對應飾品階級 I~IV：
 * dark→1、faint→2、twinkling→3、brilliant→4（對照插件 vestigeTier）。
 * 於鐵砧中與同階飾品合成，把飾品 {@link ModComponents#GIFT_LEVEL} +1。
 */
public final class Vestiges {

    public static Item DARK_VESTIGE;
    public static Item FAINT_VESTIGE;
    public static Item TWINKLING_VESTIGE;
    public static Item BRILLIANT_VESTIGE;

    private static final Map<Item, Integer> TIER = new LinkedHashMap<>();

    private Vestiges() {}

    public static void register() {
        DARK_VESTIGE = reg("dark_vestige", 1);
        FAINT_VESTIGE = reg("faint_vestige", 2);
        TWINKLING_VESTIGE = reg("twinkling_vestige", 3);
        BRILLIANT_VESTIGE = reg("brilliant_vestige", 4);
    }

    /** 殘影物品 → 階級；非殘影回 -1。 */
    public static int tierOf(Item item) {
        return TIER.getOrDefault(item, -1);
    }

    public static boolean isVestige(Item item) {
        return TIER.containsKey(item);
    }

    private static Item reg(String name, int tier) {
        Item item = new Item(new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, LimbusEGOMod.id(name)))
                .maxCount(64)
                .rarity(Rarity.UNCOMMON));
        Registry.register(Registries.ITEM, LimbusEGOMod.id(name), item);
        TIER.put(item, tier);
        return item;
    }
}
