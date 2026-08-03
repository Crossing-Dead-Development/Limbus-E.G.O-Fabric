package me.yisang.limbusego.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 武器名稱／lore 樣式集中對照。文字走翻譯鍵（雙語），顏色與粗體在此套用。
 * 對照來源：Limbus-E.G.O 插件 lang/weapons。漸層項以代表色近似。
 *
 * 註：名稱樣式**不能**經 {@code Item.Settings.component(ITEM_NAME, …)} 設定——1.21.4 的
 * Settings 會在建置時用翻譯鍵的預設無樣式 item_name 無條件蓋掉。改由 {@code ItemNameMixin}
 * 攔截 {@code Item.getName(ItemStack)} 回傳 {@link #styledName(String)}。Lore 元件不受此限，仍走 {@link #apply}。
 */
public final class WeaponStyles {

    private WeaponStyles() {}

    /** 一把武器的樣式：名稱色、是否粗體、各行 lore 顏色（長度＝lore 行數）。 */
    private record Spec(int nameColor, boolean bold, int[] loreColors) {}

    private static final Map<String, Spec> SPECS = Map.ofEntries(
        Map.entry("solemn_lament_black", new Spec(0x333333, true,  new int[]{0xD8D8D8})),
        Map.entry("solemn_lament_white", new Spec(0xFFFFFF, true,  new int[]{0xD8D8D8})),
        Map.entry("solemn_shield",       new Spec(0xFFFFFF, true,  new int[]{0xD8D8D8})),
        Map.entry("butterfly_quartz",    new Spec(0xB1B1B1, false, new int[]{0xD8D8D8})),
        Map.entry("mimicry",             new Spec(0xFF0000, false, new int[]{0xFF0000})),
        Map.entry("dacapo",              new Spec(0xFFFFFF, false, new int[]{0xFFFFFF})),
        Map.entry("ring_brush",          new Spec(0xFFFFFF, false, new int[]{0xFF9500})),
        Map.entry("tiantui_star",        new Spec(0xE67E22, false, new int[]{0xAAAAAA, 0x555555})),
        Map.entry("tiger_mark",          new Spec(0xE67E22, false, new int[]{0xAAAAAA})),
        Map.entry("savage_tiger_mark",   new Spec(0xC0392B, false, new int[]{0xAAAAAA})),
        Map.entry("twilight",            new Spec(0xFFD700, false, new int[]{0xAAAAAA, 0x555555, 0x555555})),
        Map.entry("tibia",               new Spec(0x8B0000, false, new int[]{0xAAAAAA})),
        Map.entry("w_corp_knife",        new Spec(0x66E1FF, false, new int[]{0xB3F0FF})),
        Map.entry("bladesinger",         new Spec(0xAEDBFF, false, new int[]{0xD0E7FF}))
    );

    /** 對已知武器 id 掛上 LORE component。未知 id 原樣返回。名稱樣式改由 mixin 處理（見類別註解）。 */
    public static Item.Settings apply(Item.Settings s, String id) {
        Spec spec = SPECS.get(id);
        if (spec == null) return s;

        int[] colors = spec.loreColors;
        if (colors.length > 0) {
            List<Text> lore = new ArrayList<>(colors.length);
            for (int i = 0; i < colors.length; i++) {
                Style ls = Style.EMPTY.withColor(TextColor.fromRgb(colors[i])).withItalic(false);
                lore.add(Text.translatable("item.limbusego." + id + ".lore." + i).setStyle(ls));
            }
            s.component(DataComponentTypes.LORE, new LoreComponent(lore));
        }
        return s;
    }

    /** 已知武器 id 的樣式化顯示名稱（翻譯鍵＋顏色/粗體，無斜體）；未知 id 回傳 {@code null}。 */
    public static Text styledName(String id) {
        if (id == null) return null;
        Spec spec = SPECS.get(id);
        if (spec == null) return null;
        Style nameStyle = Style.EMPTY
                .withColor(TextColor.fromRgb(spec.nameColor))
                .withBold(spec.bold)
                .withItalic(false);
        return Text.translatable("item.limbusego." + id).setStyle(nameStyle);
    }
}
