package me.yisang.limbusego;

/**
 * Phase 1 訊息常數：先硬編碼繁中，鍵名對齊插件 lang/weapons/zh_TW.yml。
 * Phase 3 引入 LangManager 後改為查表。
 */
public final class Messages {
    private Messages() {}

    // sanity.*
    public static final String SANITY_BAR_TITLE = "§f理智值 {0}{1} §7/ §f{2}";
    public static final String SANITY_WARN_DROP = "§5§l⚠ 理智值 §7» §d{0} §8/ §7{1}";
    public static final String SANITY_PANIC = "§5§l▼ 陷入恐慌 §c你的雙眼與四肢已不再聽從指揮";
    public static final String SANITY_BOTTOM = "§4§l▼ 理智觸底 §c你已陷入憂鬱";

    /** 簡易 {0}/{1}… 樣板替換（與插件 msg() 語意一致）。 */
    public static String fmt(String pattern, Object... args) {
        String out = pattern;
        for (int i = 0; i < args.length; i++) {
            out = out.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return out;
    }
}
