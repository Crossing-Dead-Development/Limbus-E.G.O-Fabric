package me.yisang.limbusego;

import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class LangParityTest {

    @Test
    void bothLanguagesHaveIdenticalKeySets() {
        var zh = LangKeys.zhTw();
        var en = LangKeys.enUs();

        var missingInEn = new TreeSet<>(zh);
        missingInEn.removeAll(en);
        var missingInZh = new TreeSet<>(en);
        missingInZh.removeAll(zh);

        assertTrue(missingInEn.isEmpty(), "en_us.json 缺少：" + missingInEn);
        assertTrue(missingInZh.isEmpty(), "zh_tw.json 缺少：" + missingInZh);
    }

    @Test
    void statusKeysPresent() {
        for (var s : me.yisang.limbusego.status.StatusEffect.values()) {
            LangKeys.assertKeyExists(s.translationKey());
        }
    }

    /** GUI 效果名（effect.*，原版由 registry id 推導）與 tooltip 屬性名（status.*）必須是同一個詞。 */
    @Test
    void mirroredEffectNamesMatchStatusNames() {
        for (var s : me.yisang.limbusego.status.ModStatusEffects.MIRRORED) {
            String path = s.name().toLowerCase(java.util.Locale.ROOT);
            String effectKey = "effect.limbusego." + path;
            LangKeys.assertKeyExists(effectKey);
            assertEquals(LangKeys.zhTwValues().get(s.translationKey()), LangKeys.zhTwValues().get(effectKey),
                    "zh_tw：" + effectKey + " 與 " + s.translationKey() + " 值不同");
            assertEquals(LangKeys.enUsValues().get(s.translationKey()), LangKeys.enUsValues().get(effectKey),
                    "en_us：" + effectKey + " 與 " + s.translationKey() + " 值不同");
        }
    }

    @Test
    void exactlyTenStatusesAreMirrored() {
        var mirrored = me.yisang.limbusego.status.ModStatusEffects.MIRRORED;
        assertEquals(10, mirrored.size());
        assertFalse(mirrored.contains(me.yisang.limbusego.status.StatusEffect.HASTE), "迅捷已是原版速度，不鏡射");
        assertFalse(mirrored.contains(me.yisang.limbusego.status.StatusEffect.BIND), "束縛已是原版緩速，不鏡射");
    }
}
