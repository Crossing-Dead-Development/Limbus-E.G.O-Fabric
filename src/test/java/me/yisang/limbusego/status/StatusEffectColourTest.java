package me.yisang.limbusego.status;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class StatusEffectColourTest {

    /** 會被鏡射成原版效果的 10 個屬性（HASTE / BIND 除外）。 */
    private static final EnumSet<StatusEffect> MIRRORED = EnumSet.of(
            StatusEffect.BLEED, StatusEffect.BURN, StatusEffect.FRAGILE, StatusEffect.SINKING,
            StatusEffect.RUPTURE, StatusEffect.TREMOR, StatusEffect.POWER, StatusEffect.PROTECTION,
            StatusEffect.POISE, StatusEffect.CHARGE);

    @Test
    void mirroredColoursAreNonZeroAndDistinct() {
        var seen = new HashSet<Integer>();
        for (var e : MIRRORED) {
            assertNotEquals(0, e.rgb, e + " 的 rgb 不可為 0（會與黑色／未設定混淆）");
            assertTrue(seen.add(e.rgb), e + " 的 rgb 與其他屬性重複：" + Integer.toHexString(e.rgb));
        }
        assertEquals(10, seen.size());
    }

    @Test
    void rgbFitsIn24Bits() {
        for (var e : StatusEffect.values()) {
            assertEquals(0, e.rgb & 0xFF000000, e + " 的 rgb 不可帶 alpha");
        }
    }
}
