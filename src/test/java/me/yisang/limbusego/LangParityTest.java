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
}
