package me.yisang.limbusego.gift;

/**
 * 殘影升級的純數值邏輯（不依賴 Minecraft，供 JUnit 直接驗證）。
 *
 * <p>規則對照插件（{@code GiftsModule} 選單拖曳升級）：殘影階級須等於飾品階級才可升，
 * 每次 +1 等級、消耗 1 殘影，等級上限 {@link #MAX_LEVEL}。Fabric 端把此語意搬到鐵砧。
 * 升級等級存飾品物品的 {@link ModComponents#GIFT_LEVEL}，效果 potency 依 {@link #multiplier} 放大。
 */
public final class GiftUpgradeLogic {

    /** 升級等級上限（0~3）。 */
    public static final int MAX_LEVEL = 3;

    /** 鐵砧每次升級的經驗等級花費（設計未定，取固定值；> 0 才允許取出）。 */
    public static final int XP_COST = 5;

    private GiftUpgradeLogic() {}

    /**
     * 解析升級結果。
     *
     * @param giftTier     飾品階級（1~4）
     * @param vestigeTier  殘影階級（1~4；dark=1、faint=2、twinkling=3、brilliant=4）
     * @param currentLevel 飾品當前等級（0~3）
     * @return 升級後等級（currentLevel+1）；若階級不符或已達上限則回 -1（無輸出）
     */
    public static int resolveUpgrade(int giftTier, int vestigeTier, int currentLevel) {
        if (vestigeTier != giftTier) return -1;
        if (currentLevel < 0 || currentLevel >= MAX_LEVEL) return -1;
        return currentLevel + 1;
    }

    /** 等級 → 效果倍率（0→1.0、1→1.25、2→1.50、3→2.00，對照插件 getUpgradeMultiplier）。 */
    public static double multiplier(int level) {
        return switch (level) {
            case 1 -> 1.25;
            case 2 -> 1.50;
            case 3 -> 2.00;
            default -> 1.0;
        };
    }
}
