package me.yisang.limbusego.status;

/**
 * 屬性顯示層的純決策函式：鏡射效果該做什麼、施加粒子該不該冒。
 *
 * <p>刻意不依賴任何 Minecraft 型別，方便單元測試。實際的 addStatusEffect /
 * spawnParticles 由 {@link StatusMirror} 與 {@link StatusManager} 執行。
 */
public final class StatusDisplayLogic {

    public enum Action { ADD, UPDATE, REMOVE, NONE }

    /** amplifier 只在 ADD / UPDATE 時有意義，其餘為 0。 */
    public record Decision(Action action, int amplifier) {}

    private StatusDisplayLogic() {}

    /**
     * @param potency          StatusState 中該屬性目前的威力；0 表示沒有
     * @param currentAmplifier 玩家身上鏡射效果目前的 amplifier；null 表示沒有該效果
     */
    public static Decision decide(int potency, Integer currentAmplifier) {
        if (potency <= 0) {
            return currentAmplifier == null
                    ? new Decision(Action.NONE, 0)
                    : new Decision(Action.REMOVE, 0);
        }
        int wanted = potency - 1;
        if (currentAmplifier == null) return new Decision(Action.ADD, wanted);
        if (currentAmplifier == wanted) return new Decision(Action.NONE, wanted);
        return new Decision(Action.UPDATE, wanted);
    }

    /** 只有「打出去」（有施術者且不是自己）才在目標身上冒粒子。 */
    public static boolean shouldSpawnParticles(Object source, Object target) {
        return source != null && !source.equals(target);
    }
}
