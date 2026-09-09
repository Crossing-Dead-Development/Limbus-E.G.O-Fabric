package me.yisang.limbusego.gift;

import me.yisang.limbusego.LangKeys;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 飾品說明的覆蓋率把關。
 *
 * <p>刻意不使用 {@link GiftRegistry}——填充它需要註冊物品、會碰 Minecraft registry。
 * 改為解析 {@code ModGifts.java} 的註冊列表再以反射實例化，因此涵蓋的正好是實際
 * 註冊的那 80 件，新增飾品會自動納入。
 */
class GiftDescriptionCoverageTest {

    /** 已完成 describe() 的飾品 id。每完成一組就把該組 id 加進來。 */
    static final Set<String> DONE = Set.of(
            // 燒傷 burn（8）
            "ardent_flower", "ashes_to_ashes", "bloodflame_sword", "dust_to_dust",
            "glimpse_of_flames", "hot_n_juicy_drumstick", "pain_of_stifled_rage", "royal_jelly_perfume",
            // 流血 bleed（6）
            "crystallized_blood", "la_manchaland_all_day_pass", "la_manchaland_standard_pass",
            "mask_of_the_parade", "millarca", "sanguine_blossom_bolus",
            // 沉淪 sinking（10）
            "artistic_sense", "black_sheet_music", "broken_compass", "cold_illusion", "distant_star",
            "frozen_cries", "mental_corruption_boosting_gas", "rags", "rest", "tangled_bones",
            // 破裂 rupture（11，含束縛掛靠）
            "dry_to_the_bone_breast", "ebony_brooch", "flower_in_the_mirror", "harestride",
            "moon_in_the_water", "ruin", "smoking_gunpowder", "strange_glyph_inscriptions",
            "strange_glyph_talisman", "thunderbranch", "chief_butlers_secret_arts",
            // 震顫 tremor（6）
            "green_spirit", "nixie_divergence", "sour_liquor_aroma", "sownpour",
            "piece_of_crumbled_egg", "handheld_mirror",
            // 呼吸法 poise（7）
            "cask_spirits", "clear_mirror_calm_water", "emerald_elytra", "finifugality",
            "keenbranch", "nebulizer", "cqc_manual");

    private static final Path MOD_GIFTS =
            Path.of("src/main/java/me/yisang/limbusego/gift/ModGifts.java");
    private static final Pattern REG =
            Pattern.compile("reg\\(\"([a-z0-9_]+)\"\\s*,\\s*new\\s+([A-Za-z0-9_]+)\\s*\\(");

    @Test
    void modGiftsParsesToEightyEntries() {
        assertEquals(80, registered().size(), "解析 ModGifts.java 得到的飾品數不符");
    }

    @Test
    void completedGiftsHaveNonEmptyDescription() {
        registered().forEach((id, gift) -> {
            if (!DONE.contains(id)) return;
            assertFalse(gift.describe(0).isEmpty(), "飾品缺少說明：" + id);
        });
    }

    @Test
    void completedGiftKeysExistInBothLanguages() {
        registered().forEach((id, gift) -> {
            if (!DONE.contains(id)) return;
            for (int level = 0; level <= 3; level++) {
                translationKeys(gift.describe(level)).forEach(LangKeys::assertKeyExists);
            }
        });
    }

    @Test
    void doneListOnlyContainsRealGifts() {
        var unknown = new TreeSet<>(DONE);
        unknown.removeAll(registered().keySet());
        assertTrue(unknown.isEmpty(), "DONE 含有不存在的飾品 id：" + unknown);
    }

    /** id → 已實例化的飾品，來源為 ModGifts.java 的註冊列表。 */
    static Map<String, BaseGift> registered() {
        try {
            String src = Files.readString(MOD_GIFTS, StandardCharsets.UTF_8);
            Map<String, BaseGift> out = new LinkedHashMap<>();
            Matcher m = REG.matcher(src);
            while (m.find()) {
                String className = "me.yisang.limbusego.gift.gifts." + m.group(2);
                Object instance = Class.forName(className).getDeclaredConstructor().newInstance();
                out.put(m.group(1), (BaseGift) instance);
            }
            return out;
        } catch (IOException | ReflectiveOperationException e) {
            throw new IllegalStateException("無法從 ModGifts.java 建立飾品實例", e);
        }
    }

    /** 遞迴取出所有翻譯鍵（含 sibling 與 translatable 參數）。 */
    static List<String> translationKeys(List<Text> lines) {
        return lines.stream().flatMap(GiftDescriptionCoverageTest::flatten)
                .map(Text::getContent)
                .filter(TranslatableTextContent.class::isInstance)
                .map(c -> ((TranslatableTextContent) c).getKey())
                .toList();
    }

    private static Stream<Text> flatten(Text text) {
        Stream<Text> siblings = text.getSiblings().stream().flatMap(GiftDescriptionCoverageTest::flatten);
        Stream<Text> args = text.getContent() instanceof TranslatableTextContent t
                ? Stream.of(t.getArgs()).filter(Text.class::isInstance).map(Text.class::cast)
                        .flatMap(GiftDescriptionCoverageTest::flatten)
                : Stream.empty();
        return Stream.concat(Stream.of(text), Stream.concat(siblings, args));
    }
}
