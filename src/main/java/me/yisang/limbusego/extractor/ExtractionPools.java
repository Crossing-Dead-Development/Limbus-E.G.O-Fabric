package me.yisang.limbusego.extractor;

import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.gift.GiftRegistry;
import me.yisang.limbusego.gift.ModGifts;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 提取機的抽獎池：階級 → 該階全部飾品物品。
 *
 * <p>從 {@link GiftRegistry} 依 {@link BaseGift#tier()} 反推，不維護第二份清單——
 * 日後新增飾品會自動進池。{@link #build()} 在 {@code ModGifts.register()} 之後呼叫一次，
 * 並把四階池大小寫進日誌，空池在啟動階段就看得見。
 */
public final class ExtractionPools {

    private static final List<List<Item>> POOLS = new ArrayList<>();

    private ExtractionPools() {}

    public static void build() {
        POOLS.clear();
        for (int t = 0; t <= ExtractionLogic.MAX_TIER; t++) POOLS.add(new ArrayList<>());

        Map<String, Item> byId = ModGifts.byId();
        for (BaseGift gift : GiftRegistry.all()) {
            int tier = gift.tier();
            Item item = byId.get(gift.id());
            if (item == null || !ExtractionLogic.isValidTier(tier)) continue;
            POOLS.get(tier).add(item);
        }
        for (int t = ExtractionLogic.MIN_TIER; t <= ExtractionLogic.MAX_TIER; t++) {
            POOLS.set(t, List.copyOf(POOLS.get(t)));
        }
        LimbusEGOMod.LOGGER.info("提取機抽獎池：I={} II={} III={} IV={}",
                of(1).size(), of(2).size(), of(3).size(), of(4).size());
    }

    /** 該階級的飾品池；階級越界或尚未 build 回空清單。 */
    public static List<Item> of(int tier) {
        if (!ExtractionLogic.isValidTier(tier) || tier >= POOLS.size()) return List.of();
        return POOLS.get(tier);
    }
}
