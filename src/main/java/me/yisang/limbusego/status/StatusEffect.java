package me.yisang.limbusego.status;

public enum StatusEffect {
    BLEED("流血", "§c", 0xFF5555),
    BURN("燒傷", "§6", 0xFFAA00),
    FRAGILE("易損", "§d", 0xFF55FF),
    POWER("強壯", "§e", 0xFFFF55),
    SINKING("沉淪", "§5", 0xAA00AA),
    RUPTURE("破裂", "§4", 0xAA0000),
    TREMOR("震顫", "§b", 0x55FFFF),
    PROTECTION("守護", "§a", 0x55FF55),
    HASTE("迅捷", "§f", 0xFFFFFF),
    BIND("束縛", "§8", 0x555555),
    POISE("呼吸法", "§3", 0x00AAAA),
    CHARGE("充能", "§9", 0x5555FF);

    public final String zh;
    public final String color;
    /** 0xRRGGBB，tooltip、粒子、鏡射效果共用的唯一顏色來源。 */
    public final int rgb;

    StatusEffect(String zh, String color, int rgb) {
        this.zh = zh;
        this.color = color;
        this.rgb = rgb;
    }

    /** tooltip 用的翻譯鍵，例：status.limbusego.burn。 */
    public String translationKey() {
        return "status.limbusego." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
