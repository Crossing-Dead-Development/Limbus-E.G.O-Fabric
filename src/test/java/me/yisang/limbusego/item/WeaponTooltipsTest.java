package me.yisang.limbusego.item;

import me.yisang.limbusego.LangKeys;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class WeaponTooltipsTest {

    /** 必須全部有說明的武器與彈藥 id。 */
    private static final Set<String> EXPECTED = Set.of(
            "solemn_lament_black", "solemn_lament_white", "solemn_shield", "butterfly_quartz",
            "mimicry", "dacapo", "ring_brush", "tiantui_star", "tiger_mark", "savage_tiger_mark",
            "twilight", "tibia", "w_corp_knife", "bladesinger");

    @Test
    void everyWeaponHasDetails() {
        for (String id : EXPECTED) {
            assertFalse(WeaponTooltips.of(id).isEmpty(), "武器缺少說明：" + id);
        }
    }

    @Test
    void tableHasNoUnexpectedEntries() {
        assertEquals(EXPECTED, WeaponTooltips.ids(), "WeaponTooltips 的 id 集合與預期不符");
    }

    @Test
    void unknownItemReturnsEmpty() {
        assertTrue(WeaponTooltips.of("diamond_sword").isEmpty());
        assertTrue(WeaponTooltips.of("").isEmpty());
    }

    @Test
    void everyReferencedKeyExistsInBothLanguages() {
        for (String id : WeaponTooltips.ids()) {
            collectKeys(WeaponTooltips.of(id)).forEach(LangKeys::assertKeyExists);
        }
    }

    private static List<String> collectKeys(List<Text> lines) {
        return lines.stream()
                .flatMap(WeaponTooltipsTest::flatten)
                .map(Text::getContent)
                .filter(TranslatableTextContent.class::isInstance)
                .map(c -> ((TranslatableTextContent) c).getKey())
                .toList();
    }

    /** 遞迴展開 sibling 與 translatable 參數中的 Text。 */
    private static Stream<Text> flatten(Text text) {
        Stream<Text> siblings = text.getSiblings().stream().flatMap(WeaponTooltipsTest::flatten);
        Stream<Text> args = text.getContent() instanceof TranslatableTextContent t
                ? Stream.of(t.getArgs()).filter(Text.class::isInstance).map(Text.class::cast)
                        .flatMap(WeaponTooltipsTest::flatten)
                : Stream.empty();
        return Stream.concat(Stream.of(text), Stream.concat(siblings, args));
    }
}
