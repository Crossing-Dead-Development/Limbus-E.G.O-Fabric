package me.yisang.limbusego.tooltip;

import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

/**
 * tooltip 行的樣式包裝。集中在此，避免 80 個 describe() 各自拼樣式。
 *
 * <p>注意：本類別的方法只建構 {@link Text}，不接觸 Minecraft registry，
 * 因此可在單元測試中直接使用，無須 Bootstrap。
 */
public final class TooltipFormat {

    private static final String[] ROMAN = {"", "I", "II", "III", "IV"};

    private TooltipFormat() {}

    /** 金色段落標題，前綴 ▸。 */
    public static Text section(String key, Object... args) {
        return prefixed("▸ ", key, Formatting.GOLD.getColorValue(), args);
    }

    /** 灰色內文，縮排兩格。 */
    public static Text body(String key, Object... args) {
        return prefixed("  ", key, Formatting.GRAY.getColorValue(), args);
    }

    /** 收合狀態的提示列（深灰斜體）。 */
    public static Text hint() {
        return Text.translatable("tooltip.limbusego.hint")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Formatting.DARK_GRAY.getColorValue())).withItalic(true));
    }

    /** 武器底部標籤。 */
    public static Text egoTag() {
        return styled(Text.translatable("tooltip.limbusego.tag.ego"), Formatting.DARK_GRAY.getColorValue(), false);
    }

    /** 飾品底部標籤，tier 為 1~4。 */
    public static Text giftTag(int tier) {
        String roman = tier >= 1 && tier < ROMAN.length ? ROMAN[tier] : String.valueOf(tier);
        return styled(Text.translatable("tooltip.limbusego.tag.gift", roman), Formatting.DARK_GRAY.getColorValue(), false);
    }

    /** 套用該屬性專屬顏色的名稱。 */
    public static Text status(StatusEffect effect) {
        return styled(Text.translatable(effect.translationKey()), legacyToRgb(effect.color), false);
    }

    /** 威力／次數，例：威力2・次數1。 */
    public static Text potency(int potency, int count) {
        return styled(Text.translatable("tooltip.limbusego.potency", potency, count), Formatting.WHITE.getColorValue(), false);
    }

    private static Text prefixed(String prefix, String key, int color, Object... args) {
        MutableText text = Text.literal(prefix).append(Text.translatable(key, args));
        return styled(text, color, false);
    }

    private static MutableText styled(MutableText text, int rgb, boolean italic) {
        return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withItalic(italic));
    }

    /** StatusEffect 的 §x legacy 色碼 → RGB。未知色碼回傳白色。 */
    private static int legacyToRgb(String legacy) {
        if (legacy == null || legacy.length() < 2) return Formatting.WHITE.getColorValue();
        Formatting f = Formatting.byCode(legacy.charAt(1));
        Integer rgb = f == null ? null : f.getColorValue();
        return rgb == null ? Formatting.WHITE.getColorValue() : rgb;
    }
}
