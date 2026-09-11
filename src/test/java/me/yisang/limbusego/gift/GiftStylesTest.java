package me.yisang.limbusego.gift;

import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 飾品呈現資料的把關：階級色對齊插件、風味顏色表與風味台詞表一一對應。
 *
 * <p>不碰 Minecraft registry。飾品清單用 {@link GiftDescriptionCoverageTest#registered()}
 * 解析 {@code ModGifts.java} 取得。
 */
class GiftStylesTest {

    /** 與 GiftFlavorTest.WITH_FLAVOR 相同的 46 件；此處重複列出是為了讓兩份表互相把關。 */
    private static final Set<String> WITH_FLAVOR = Set.of(
            "ardent_flower", "ashes_to_ashes", "dust_to_dust", "hot_n_juicy_drumstick",
            "pain_of_stifled_rage", "crystallized_blood", "la_manchaland_all_day_pass", "la_manchaland_standard_pass",
            "mask_of_the_parade", "black_sheet_music", "broken_compass", "cold_illusion",
            "distant_star", "frozen_cries", "rags", "rest",
            "tangled_bones", "dry_to_the_bone_breast", "ebony_brooch", "harestride",
            "moon_in_the_water", "strange_glyph_talisman", "sour_liquor_aroma", "piece_of_crumbled_egg",
            "cask_spirits", "clear_mirror_calm_water", "emerald_elytra", "dreaming_electric_sheep",
            "illusory_hunt", "hardship", "phantom_pain", "tenacity_bolus",
            "the_book_of_vengeance", "plume_of_proof", "spicebush_branch", "trauma_shield",
            "blue_zippo_lighter", "golden_urn", "homeward", "lithograph",
            "oracle", "piece_of_relationship", "flower_mound", "jin_gang_bolus",
            "piece_of_a_torn_summer", "tranquil_lotus_bolus");

    private static final Set<String> TWO_LINE = Set.of("phantom_pain", "spicebush_branch", "flower_mound");

    @Test
    void tierColorsMatchPlugin() {
        assertEquals(0xAAAAAA, GiftStyles.tierColor(1));
        assertEquals(0x55FF55, GiftStyles.tierColor(2));
        assertEquals(0x55AAFF, GiftStyles.tierColor(3));
        assertEquals(0xFFD700, GiftStyles.tierColor(4));
    }

    @Test
    void unknownTierFallsBackToGray() {
        assertEquals(0xAAAAAA, GiftStyles.tierColor(0));
        assertEquals(0xAAAAAA, GiftStyles.tierColor(5));
        assertEquals(0xAAAAAA, GiftStyles.tierColor(-1));
    }

    @Test
    void everyFlavorColorIdIsARegisteredGift() {
        var registered = GiftDescriptionCoverageTest.registered().keySet();
        for (String id : GiftStyles.flavorIds()) {
            assertTrue(registered.contains(id), "風味顏色表含有未註冊的 id：" + id);
        }
    }

    @Test
    void flavorColorTableCoversEveryFlavoredGift() {
        assertEquals(WITH_FLAVOR, GiftStyles.flavorIds(), "風味顏色表的 id 集合必須正好是 46 件有台詞的飾品");
    }

    @Test
    void flavorLinesMatchFlavorTable() {
        for (String id : WITH_FLAVOR) {
            assertEquals(TWO_LINE.contains(id) ? 2 : 1, GiftStyles.flavorLines(id), id + " 的 lore 行數");
        }
        assertEquals(0, GiftStyles.flavorLines("special_contract"), "無台詞的飾品行數應為 0");
        assertEquals(0, GiftStyles.flavorLines("not_a_gift"));
    }

    @Test
    void flavorColorsAreOpaqueRgb() {
        for (String id : GiftStyles.flavorIds()) {
            int rgb = GiftStyles.flavorColor(id);
            assertTrue(rgb >= 0 && rgb <= 0xFFFFFF, id + " 的顏色超出 24 位元：" + Integer.toHexString(rgb));
        }
        assertEquals(0xAAAAAA, GiftStyles.flavorColor("special_contract"), "查無顏色應回退灰色");
    }

    @Test
    void styledNameIsNullForNonGift() {
        // 測試環境沒有註冊任何飾品，GiftRegistry 為空 → 一律 null，mixin 維持原行為
        assertNull(GiftStyles.styledName("solemn_lament_black"));
        assertNull(GiftStyles.styledName("not_a_gift"));
        assertNull(GiftStyles.styledName(null));
    }

    @Test
    void styledNameUsesItemTranslationKeyAndTierColor() {
        var text = GiftStyles.styledName("ardent_flower", 3);
        var content = (TranslatableTextContent) text.getContent();
        assertEquals("item.limbusego.ardent_flower", content.getKey());
        assertEquals(0x55AAFF, text.getStyle().getColor().getRgb());
        assertEquals(Boolean.FALSE, text.getStyle().isItalic(), "名稱不可斜體");
    }
}
