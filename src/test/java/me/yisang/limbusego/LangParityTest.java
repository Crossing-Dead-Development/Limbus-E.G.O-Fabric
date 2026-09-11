package me.yisang.limbusego;

import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class LangParityTest {

    @Test
    void allLanguagesHaveIdenticalKeySets() {
        var zh = LangKeys.zhTw();
        for (String file : LangKeys.FILES) {
            var other = LangKeys.values(file).keySet();
            var missing = new TreeSet<>(zh);
            missing.removeAll(other);
            var extra = new TreeSet<>(other);
            extra.removeAll(zh);
            assertTrue(missing.isEmpty(), file + " 缺少：" + missing);
            assertTrue(extra.isEmpty(), file + " 多出：" + extra);
        }
    }

    /** 簡中檔不得殘留繁體字（抽幾個高頻字檢查，避免整包直接複製繁中）。 */
    @Test
    void zhCnContainsNoTraditionalCharacters() {
        String trad = "屬飾擊傷體點淪擁護獲餘";
        for (var e : LangKeys.zhCnValues().entrySet()) {
            for (char c : trad.toCharArray()) {
                assertFalse(e.getValue().indexOf(c) >= 0, "zh_cn.json 殘留繁體字「" + c + "」：" + e.getKey());
            }
        }
    }

    /** 伺服端送出的訊息／GUI 標題全部走翻譯鍵，三語都要有。 */
    @Test
    void serverMessageKeysPresent() {
        for (String key : new String[] {
                "limbusego.sanity.bar", "limbusego.sanity.warn", "limbusego.sanity.panic", "limbusego.sanity.bottom",
                "limbusego.gui.weapon_catalog", "limbusego.gui.weapon_admin",
                "limbusego.gui.gift_catalog", "limbusego.gui.gift_admin",
                "limbusego.gui.prev_page", "limbusego.gui.next_page", "limbusego.gui.page", "limbusego.gui.given",
                "limbusego.msg.overload", "limbusego.msg.child_within_a_flask",
                "limbusego.cmd.unknown_status", "limbusego.cmd.unknown_weapon", "limbusego.cmd.unknown_gift",
                "limbusego.cmd.status_applied", "limbusego.cmd.status_none", "limbusego.cmd.status_list",
                "limbusego.cmd.status_cleared", "limbusego.cmd.given"}) {
            LangKeys.assertKeyExists(key);
        }
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
            assertEquals(LangKeys.zhCnValues().get(s.translationKey()), LangKeys.zhCnValues().get(effectKey),
                    "zh_cn：" + effectKey + " 與 " + s.translationKey() + " 值不同");
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
