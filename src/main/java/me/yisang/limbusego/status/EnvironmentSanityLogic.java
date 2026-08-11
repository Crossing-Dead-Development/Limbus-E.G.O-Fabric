package me.yisang.limbusego.status;

/**
 * 環境光照對 SAN 影響的純數值邏輯（不依賴 Minecraft，供 JUnit 直接驗證）。
 *
 * <p>規則（每 2 秒呼叫一次，見 {@link SanityManager} 的 {@code recoveryTick}）：
 * <ul>
 *   <li>光照 ≤ {@link #LIGHT_DARK_MAX} 視為黑暗，曝光值 -1</li>
 *   <li>光照 ≥ {@link #LIGHT_BRIGHT_MIN} 視為明亮，曝光值 +1</li>
 *   <li>中間為中性，曝光值向 0 收斂 1</li>
 *   <li>曝光值絕對值達 {@link #EXPOSURE_THRESHOLD} 時動 1 點 SAN，並歸零</li>
 *   <li>明亮回復只能回到 0，正值 SAN 一律靠戰鬥命中取得</li>
 *   <li>黑暗中暫停脫戰回復（「黑暗中無法平復」）</li>
 * </ul>
 *
 * <p>SAN 下限夾制不在此處理，由 {@link SanityManager#setSan} 負責。
 */
public final class EnvironmentSanityLogic {

    /** 光照 ≤ 此值視為黑暗。4 = 午夜露天（天空光 15 − ambient darkness 11）；洞穴為 0。 */
    public static final int LIGHT_DARK_MAX = 4;

    /** 光照 ≥ 此值視為明亮。火把附近為 10~14。 */
    public static final int LIGHT_BRIGHT_MIN = 9;

    /** 曝光值達此絕對值即動 1 點 SAN。15 × 2 秒 = 30 秒 1 點。 */
    public static final int EXPOSURE_THRESHOLD = 15;

    /**
     * 一次判定的結果。
     *
     * @param exposure 新的曝光累計值
     * @param sanDelta SAN 變化量（已保證回復不越過 0）
     */
    public record Result(int exposure, int sanDelta) {}

    private EnvironmentSanityLogic() {}

    /**
     * 計算一次（每 2 秒）的環境判定。
     *
     * @param light        當前方塊位置的環境亮度（0~15，含時間影響）
     * @param exposure     當前曝光累計值（正=明亮累積、負=黑暗累積）
     * @param san          當前 SAN
     * @param outOfCombat  是否已脫戰（從未戰鬥亦視為脫戰）
     */
    public static Result step(int light, int exposure, int san, boolean outOfCombat) {
        boolean dark = light <= LIGHT_DARK_MAX;
        boolean bright = light >= LIGHT_BRIGHT_MIN;

        // 1. 曝光累計；中性亮度向 0 收斂，讓黑暗累積在離開洞穴後自然消散
        int next;
        if (dark) next = exposure - 1;
        else if (bright) next = exposure + 1;
        else if (exposure > 0) next = exposure - 1;
        else if (exposure < 0) next = exposure + 1;
        else next = 0;

        // 2. 跨門檻 → 動 1 點 SAN，累計器歸零
        int delta = 0;
        if (next <= -EXPOSURE_THRESHOLD) {
            delta -= 1;
            next = 0;
        } else if (next >= EXPOSURE_THRESHOLD) {
            if (san < 0) delta += 1;   // 明亮回復只到 0
            next = 0;
        }

        // 3. 脫戰回復（既有規則），黑暗中暫停
        if (!dark && outOfCombat && san + delta < 0) delta += 1;

        return new Result(next, delta);
    }
}
