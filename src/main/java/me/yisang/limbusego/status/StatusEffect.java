package me.yisang.limbusego.status;

public enum StatusEffect {
    BLEED(0xFF5555),
    BURN(0xFFAA00),
    FRAGILE(0xFF55FF),
    POWER(0xFFFF55),
    SINKING(0xAA00AA),
    RUPTURE(0xAA0000),
    TREMOR(0x55FFFF),
    PROTECTION(0x55FF55),
    HASTE(0xFFFFFF),
    BIND(0x555555),
    POISE(0x00AAAA),
    CHARGE(0x5555FF);

    /** 0xRRGGBB，tooltip、粒子、鏡射效果共用的唯一顏色來源。 */
    public final int rgb;

    StatusEffect(int rgb) {
        this.rgb = rgb;
    }

    /** tooltip 用的翻譯鍵，例：status.limbusego.burn。 */
    public String translationKey() {
        return "status.limbusego." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
