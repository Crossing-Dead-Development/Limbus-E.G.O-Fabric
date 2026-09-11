package me.yisang.limbusego.extractor;

/**
 * 提取機的純決策函式：消耗、時間、可否啟動、池內抽選。
 *
 * <p>刻意不依賴任何 Minecraft 型別（照 {@code GiftUpgradeLogic} 慣例），
 * 平衡數值可直接用 JUnit 驗證。方塊層 {@link ExtractorBlockEntity} 只負責
 * 「讀槽位 → 呼叫本類 → 套用結果」。
 */
public final class ExtractionLogic {

    /** 索引 = 階級（1~4），索引 0 不用。Tier III 的 32 對齊 Paper 版 gacha.lunacy-cost。 */
    public static final int[] ENKEPHALIN_COST = {0, 8, 16, 32, 64};
    /** 索引 = 階級（1~4），單位 tick。 */
    public static final int[] DURATION_TICKS = {0, 100, 160, 240, 400};

    public static final int MIN_TIER = 1;
    public static final int MAX_TIER = 4;

    private ExtractionLogic() {}

    public static boolean isValidTier(int tier) {
        return tier >= MIN_TIER && tier <= MAX_TIER;
    }

    /** 該階級一次提取消耗的 Enkephalin；階級越界回 -1。 */
    public static int costOf(int tier) {
        return isValidTier(tier) ? ENKEPHALIN_COST[tier] : -1;
    }

    /** 該階級一次提取所需 tick；階級越界回 -1。 */
    public static int durationOf(int tier) {
        return isValidTier(tier) ? DURATION_TICKS[tier] : -1;
    }

    /**
     * @param tier       觸媒槽殘影的階級；非殘影為 -1
     * @param enkephalin Enkephalin 槽目前數量
     * @param outputFree 產出槽是否可放（空）
     * @param poolSize   該階級飾品池大小
     */
    public static boolean canStart(int tier, int enkephalin, boolean outputFree, int poolSize) {
        if (!isValidTier(tier)) return false;
        if (enkephalin < ENKEPHALIN_COST[tier]) return false;
        if (!outputFree) return false;
        return poolSize > 0;
    }

    /** 把 [0,1) 的亂數映射到池索引；任何輸入都夾在 [0, poolSize-1]。 */
    public static int pick(int poolSize, double roll) {
        if (poolSize <= 1) return 0;
        int idx = (int) Math.floor(roll * poolSize);
        if (idx < 0) return 0;
        if (idx >= poolSize) return poolSize - 1;
        return idx;
    }
}
