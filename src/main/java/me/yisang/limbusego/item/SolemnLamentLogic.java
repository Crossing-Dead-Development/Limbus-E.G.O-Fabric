package me.yisang.limbusego.item;

/**
 * 莊嚴哀悼雙持的判定規則。
 *
 * <p>刻意只吃 boolean、不碰 {@code ItemStack} 或 registry，因此可直接單元測試，
 * 無須 Minecraft bootstrap。由 {@link SolemnLamentItem} 負責把 stack 轉成這些旗標。
 *
 * <p>這裡沒有「選哪一把槍」的邏輯：vanilla 的 doItemUse 依序試主手、副手，
 * 且在呼叫 use 前就會擋掉冷卻中的那隻手，交替是免費的。詳見
 * {@link SolemnLamentItem#use}。
 */
public final class SolemnLamentLogic {

    private SolemnLamentLogic() {}

    /** 主手與副手是否構成合法的黑白配對（兩手都是莊嚴哀悼，且顏色相異）。 */
    public static boolean isPaired(boolean mainIsSolemn, boolean mainIsBlack,
                                   boolean offIsSolemn, boolean offIsBlack) {
        return mainIsSolemn && offIsSolemn && mainIsBlack != offIsBlack;
    }
}
