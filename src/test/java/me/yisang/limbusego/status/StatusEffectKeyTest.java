package me.yisang.limbusego.status;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusEffectKeyTest {

    @Test
    void keyFollowsConvention() {
        assertEquals("status.limbusego.burn", StatusEffect.BURN.translationKey());
        assertEquals("status.limbusego.poise", StatusEffect.POISE.translationKey());
        assertEquals("status.limbusego.charge", StatusEffect.CHARGE.translationKey());
    }

    @Test
    void everyStatusHasDistinctKey() {
        long distinct = java.util.Arrays.stream(StatusEffect.values())
                .map(StatusEffect::translationKey)
                .distinct()
                .count();
        assertEquals(StatusEffect.values().length, distinct);
    }
}
