package me.yisang.limbusego.gift;

import net.minecraft.item.Item;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 飾品登錄表：id ↔ {@link BaseGift} 實例、以及物品 → 飾品對照。
 * 供 {@link GiftDispatcher} 分派、指令 give、圖鑑列舉使用。
 */
public final class GiftRegistry {

    private static final Map<String, BaseGift> BY_ID = new LinkedHashMap<>();
    private static final Map<Item, BaseGift> BY_ITEM = new LinkedHashMap<>();

    private GiftRegistry() {}

    public static void register(BaseGift gift, Item item) {
        BY_ID.put(gift.id(), gift);
        BY_ITEM.put(item, gift);
    }

    public static BaseGift byId(String id) {
        return BY_ID.get(id);
    }

    public static BaseGift byItem(Item item) {
        return BY_ITEM.get(item);
    }

    public static Collection<BaseGift> all() {
        return BY_ID.values();
    }
}
