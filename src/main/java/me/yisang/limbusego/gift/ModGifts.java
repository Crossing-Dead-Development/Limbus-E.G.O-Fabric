package me.yisang.limbusego.gift;

import io.wispforest.accessories.api.AccessoryRegistry;
import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.gift.gifts.ArdentFlower;
import me.yisang.limbusego.gift.gifts.BloodyGadget;
import me.yisang.limbusego.gift.gifts.GlimpseOfFlames;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

/**
 * E.G.O 飾品物品註冊。
 *
 * <p>Phase 2 框架期先收 3 件試水（燒傷／強壯／引爆），驗證
 * 佩戴 → 效果 → 升級全鏈路後，再依 9 體系批次補齊 80 件。
 */
public final class ModGifts {

    public static Item BLOODY_GADGET;
    public static Item ARDENT_FLOWER;
    public static Item GLIMPSE_OF_FLAMES;

    private ModGifts() {}

    public static void register() {
        BLOODY_GADGET = reg("bloody_gadget", new BloodyGadget());
        ARDENT_FLOWER = reg("ardent_flower", new ArdentFlower());
        GLIMPSE_OF_FLAMES = reg("glimpse_of_flames", new GlimpseOfFlames());
    }

    private static Item reg(String name, BaseGift gift) {
        Item item = new Item(new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, LimbusEGOMod.id(name)))
                .maxCount(1)
                .rarity(Rarity.EPIC));
        Registry.register(Registries.ITEM, LimbusEGOMod.id(name), item);
        AccessoryRegistry.register(item, gift);
        GiftRegistry.register(gift, item);
        return item;
    }
}
