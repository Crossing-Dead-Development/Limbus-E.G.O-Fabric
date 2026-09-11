package me.yisang.limbusego.gift;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.Map;
import java.util.Set;

/**
 * 飾品的呈現資料集中對照：名稱樣式、風味台詞行數、風味台詞顏色。
 * 與武器側「{@code ModItems} 註冊、{@code WeaponStyles} 管樣式」的分工一致。
 *
 * <p>名稱樣式**不能**經 {@code Item.Settings.component(ITEM_NAME, …)} 設定（1.21.4 會被無條件蓋掉），
 * 改由 {@code ItemNameMixin} 攔截 {@code Item.getName(ItemStack)} 回傳 {@link #styledName(String)}。
 *
 * <p>階級不另建表：一律讀 {@link GiftRegistry#byId(String)} 的 {@link BaseGift#tier()}，
 * 與 Shift 底部標籤同一來源。
 */
public final class GiftStyles {

    private GiftStyles() {}

    /** 插件 {@code GiftsModule.TIER_COLORS}：I 灰、II 綠、III 藍、IV 金。索引 0 為超出範圍時的回退。 */
    private static final int[] TIER_COLORS = {0xAAAAAA, 0xAAAAAA, 0x55FF55, 0x55AAFF, 0xFFD700};

    /** 查無風味顏色時的回退，即原本寫死的 {@code Formatting.GRAY}。 */
    private static final int DEFAULT_FLAVOR_COLOR = 0xAAAAAA;

    /**
     * 有風味台詞的飾品 → lore 行數。來源為 Paper 插件 {@code BaseAccessory} 的 6 參數建構子；
     * 其餘 34 件在插件中本來就沒有台詞，刻意不補寫。
     */
    private static final Map<String, Integer> FLAVOR_LINES = Map.ofEntries(
        Map.entry("ardent_flower", 1),
        Map.entry("ashes_to_ashes", 1),
        Map.entry("black_sheet_music", 1),
        Map.entry("blue_zippo_lighter", 1),
        Map.entry("broken_compass", 1),
        Map.entry("cask_spirits", 1),
        Map.entry("clear_mirror_calm_water", 1),
        Map.entry("cold_illusion", 1),
        Map.entry("crystallized_blood", 1),
        Map.entry("distant_star", 1),
        Map.entry("dreaming_electric_sheep", 1),
        Map.entry("dry_to_the_bone_breast", 1),
        Map.entry("dust_to_dust", 1),
        Map.entry("ebony_brooch", 1),
        Map.entry("emerald_elytra", 1),
        Map.entry("flower_mound", 2),
        Map.entry("frozen_cries", 1),
        Map.entry("golden_urn", 1),
        Map.entry("hardship", 1),
        Map.entry("harestride", 1),
        Map.entry("homeward", 1),
        Map.entry("hot_n_juicy_drumstick", 1),
        Map.entry("illusory_hunt", 1),
        Map.entry("jin_gang_bolus", 1),
        Map.entry("la_manchaland_all_day_pass", 1),
        Map.entry("la_manchaland_standard_pass", 1),
        Map.entry("lithograph", 1),
        Map.entry("mask_of_the_parade", 1),
        Map.entry("moon_in_the_water", 1),
        Map.entry("oracle", 1),
        Map.entry("pain_of_stifled_rage", 1),
        Map.entry("phantom_pain", 2),
        Map.entry("piece_of_a_torn_summer", 1),
        Map.entry("piece_of_crumbled_egg", 1),
        Map.entry("piece_of_relationship", 1),
        Map.entry("plume_of_proof", 1),
        Map.entry("rags", 1),
        Map.entry("rest", 1),
        Map.entry("sour_liquor_aroma", 1),
        Map.entry("spicebush_branch", 2),
        Map.entry("strange_glyph_talisman", 1),
        Map.entry("tangled_bones", 1),
        Map.entry("tenacity_bolus", 1),
        Map.entry("the_book_of_vengeance", 1),
        Map.entry("tranquil_lotus_bolus", 1),
        Map.entry("trauma_shield", 1)
    );

    /**
     * 風味台詞顏色，逐筆取自 Paper 插件各飾品建構子的第 4 參數 {@code descColor}（{@code &#RRGGBB}）。
     * 46 筆、40 種相異顏色。
     */
    private static final Map<String, Integer> FLAVOR_COLORS = Map.ofEntries(
        Map.entry("ardent_flower", 0xFF7000),
        Map.entry("ashes_to_ashes", 0x9A9A9A),
        Map.entry("black_sheet_music", 0xFFFFFF),
        Map.entry("blue_zippo_lighter", 0x44D8DB),
        Map.entry("broken_compass", 0x0772AB),
        Map.entry("cask_spirits", 0xA8BE78),
        Map.entry("clear_mirror_calm_water", 0x56BBDB),
        Map.entry("cold_illusion", 0x33CF4F),
        Map.entry("crystallized_blood", 0xFF0000),
        Map.entry("distant_star", 0x00DAFF),
        Map.entry("dreaming_electric_sheep", 0x9863E7),
        Map.entry("dry_to_the_bone_breast", 0xD77F00),
        Map.entry("dust_to_dust", 0x9A9A9A),
        Map.entry("ebony_brooch", 0x5B1365),
        Map.entry("emerald_elytra", 0x16B569),
        Map.entry("flower_mound", 0xF1B1B1),
        Map.entry("frozen_cries", 0x4498DB),
        Map.entry("golden_urn", 0xDA8F24),
        Map.entry("hardship", 0x9E9E41),
        Map.entry("harestride", 0xAAD179),
        Map.entry("homeward", 0x8EC58E),
        Map.entry("hot_n_juicy_drumstick", 0xD77F00),
        Map.entry("illusory_hunt", 0xC6CDEF),
        Map.entry("jin_gang_bolus", 0x9F8B07),
        Map.entry("la_manchaland_all_day_pass", 0xFCD05C),
        Map.entry("la_manchaland_standard_pass", 0xFCD05C),
        Map.entry("lithograph", 0x169876),
        Map.entry("mask_of_the_parade", 0x9928BB),
        Map.entry("moon_in_the_water", 0x8EC5F0),
        Map.entry("oracle", 0xB3F2F9),
        Map.entry("pain_of_stifled_rage", 0xDA3D24),
        Map.entry("phantom_pain", 0x656565),
        Map.entry("piece_of_a_torn_summer", 0x5FE2C5),
        Map.entry("piece_of_crumbled_egg", 0x33CF4F),
        Map.entry("piece_of_relationship", 0x444444),
        Map.entry("plume_of_proof", 0x4498DB),
        Map.entry("rags", 0x755E42),
        Map.entry("rest", 0xFFFFFF),
        Map.entry("sour_liquor_aroma", 0x00BC6B),
        Map.entry("spicebush_branch", 0xFFF29B),
        Map.entry("strange_glyph_talisman", 0x8E5608),
        Map.entry("tangled_bones", 0x969696),
        Map.entry("tenacity_bolus", 0x0C440C),
        Map.entry("the_book_of_vengeance", 0xB900FF),
        Map.entry("tranquil_lotus_bolus", 0xE07F9A),
        Map.entry("trauma_shield", 0xCBCBCB)
    );

    /** 階級 1~4 的名稱顏色；超出範圍回退灰色（比照插件 {@code tierColor} 的預設）。 */
    public static int tierColor(int tier) {
        return tier >= 1 && tier < TIER_COLORS.length ? TIER_COLORS[tier] : TIER_COLORS[0];
    }

    /** 飾品 id 的樣式化顯示名稱（依階級上色、無斜體）；非飾品回傳 {@code null}。 */
    public static Text styledName(String id) {
        if (id == null) return null;
        BaseGift gift = GiftRegistry.byId(id);
        if (gift == null) return null;
        return styledName(id, gift.tier());
    }

    /** 給定階級的樣式化名稱；拆出來讓測試不必填充 {@link GiftRegistry}。 */
    static Text styledName(String id, int tier) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(tierColor(tier))).withItalic(false);
        return Text.translatable("item.limbusego." + id).setStyle(style);
    }

    /** 風味台詞行數；沒有台詞的飾品回傳 0。 */
    public static int flavorLines(String id) {
        return FLAVOR_LINES.getOrDefault(id, 0);
    }

    /** 風味台詞顏色；查無回退灰色（即改動前的外觀）。 */
    public static int flavorColor(String id) {
        return FLAVOR_COLORS.getOrDefault(id, DEFAULT_FLAVOR_COLOR);
    }

    /** 風味顏色表涵蓋的 id，供測試與註冊端核對。 */
    public static Set<String> flavorIds() {
        return FLAVOR_COLORS.keySet();
    }
}
