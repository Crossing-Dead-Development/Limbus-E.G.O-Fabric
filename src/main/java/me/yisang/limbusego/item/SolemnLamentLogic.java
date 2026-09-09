package me.yisang.limbusego.item;

import net.minecraft.util.Hand;

import java.util.Optional;

/**
 * 莊嚴哀悼雙持的判定規則。
 *
 * <p>刻意只吃 boolean、不碰 {@code ItemStack} 或 registry，因此可直接單元測試，
 * 無須 Minecraft bootstrap。由 {@link SolemnLamentItem} 負責把 stack 轉成這些旗標。
 */
public final class SolemnLamentLogic {

    private SolemnLamentLogic() {}

    /** 主手與副手是否構成合法的黑白配對（兩手都是莊嚴哀悼，且顏色相異）。 */
    public static boolean isPaired(boolean mainIsSolemn, boolean mainIsBlack,
                                   boolean offIsSolemn, boolean offIsBlack) {
        return mainIsSolemn && offIsSolemn && mainIsBlack != offIsBlack;
    }

    /**
     * 回傳這次應擊發的手；兩把都在冷卻時回傳 empty。
     *
     * <p>刻意不儲存「輪到誰」：兩把各自冷卻的情況下，優先主手就會自然產生交替，
     * 且不會與實際冷卻狀態不同步。副作用是停手超過一輪冷卻後，下一發必從主手開始。
     */
    public static Optional<Hand> pickHand(boolean mainReady, boolean offReady) {
        if (mainReady) return Optional.of(Hand.MAIN_HAND);
        if (offReady) return Optional.of(Hand.OFF_HAND);
        return Optional.empty();
    }
}
