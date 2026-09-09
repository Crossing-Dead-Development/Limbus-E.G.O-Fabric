package me.yisang.limbusego.gift;

import me.yisang.limbusego.LangKeys;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 飾品風味台詞的把關。
 *
 * <p>Paper 插件的 {@code BaseAccessory} 有 6 參數與 4 參數兩種建構子，只有前者帶風味台詞。
 * 80 件中 46 件有、34 件本來就沒有——後者刻意留白，硬補會是憑空杜撰。
 */
class GiftFlavorTest {

    /** 插件中帶有風味台詞的 46 件。 */
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

    /** 插件中風味台詞為兩行的（原文含 \n）。 */
    private static final Set<String> TWO_LINE = Set.of("phantom_pain", "spicebush_branch", "flower_mound");

    @Test
    void flavorKeysExistInBothLanguages() {
        assertEquals(46, WITH_FLAVOR.size(), "有風味台詞的飾品應為 46 件");
        for (String id : WITH_FLAVOR) {
            LangKeys.assertKeyExists("item.limbusego." + id + ".lore.0");
            if (TWO_LINE.contains(id)) {
                LangKeys.assertKeyExists("item.limbusego." + id + ".lore.1");
            }
        }
    }

    @Test
    void giftsWithoutFlavorHaveNoLoreKey() {
        GiftDescriptionCoverageTest.registered().keySet().forEach(id -> {
            if (WITH_FLAVOR.contains(id)) return;
            assertFalse(LangKeys.zhTw().contains("item.limbusego." + id + ".lore.0"),
                    "此飾品在插件中沒有風味台詞，不應杜撰：" + id);
        });
    }

    @Test
    void obsoleteDescKeysRemoved() {
        GiftDescriptionCoverageTest.registered().keySet().forEach(id -> {
            assertFalse(LangKeys.zhTw().contains("item.limbusego." + id + ".desc"),
                    "舊的密集單行 .desc 應已移除：" + id);
            assertFalse(LangKeys.enUs().contains("item.limbusego." + id + ".desc"),
                    "舊的密集單行 .desc 應已移除：" + id);
        });
    }

    @Test
    void everyFlavorIdIsARegisteredGift() {
        var registered = GiftDescriptionCoverageTest.registered().keySet();
        for (String id : WITH_FLAVOR) {
            assertTrue(registered.contains(id), "WITH_FLAVOR 含有未註冊的 id：" + id);
        }
    }
}
