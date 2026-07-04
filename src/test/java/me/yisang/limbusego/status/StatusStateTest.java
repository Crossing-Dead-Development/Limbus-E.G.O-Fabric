package me.yisang.limbusego.status;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatusStateTest {

    @Test
    void addAccumulatesPotencyAndCount() {
        StatusState s = new StatusState();
        s.add(StatusEffect.BLEED, 3, 2);
        s.add(StatusEffect.BLEED, 2, 1);
        assertEquals(5, s.potency(StatusEffect.BLEED));
        assertEquals(3, s.count(StatusEffect.BLEED));
    }

    @Test
    void missingEffectReadsZero() {
        StatusState s = new StatusState();
        assertEquals(0, s.potency(StatusEffect.BURN));
        assertEquals(0, s.count(StatusEffect.BURN));
        assertTrue(s.isEmpty());
    }

    @Test
    void consumePartialKeepsEffect() {
        StatusState s = new StatusState();
        s.add(StatusEffect.BURN, 4, 5);
        assertEquals(2, s.consume(StatusEffect.BURN, 2));
        assertEquals(3, s.count(StatusEffect.BURN));
        assertEquals(4, s.potency(StatusEffect.BURN));
        assertFalse(s.isEmpty());
    }

    @Test
    void consumeToZeroRemovesEffectIncludingPotency() {
        StatusState s = new StatusState();
        s.add(StatusEffect.RUPTURE, 7, 2);
        assertEquals(2, s.consume(StatusEffect.RUPTURE, 5));
        assertEquals(0, s.count(StatusEffect.RUPTURE));
        assertEquals(0, s.potency(StatusEffect.RUPTURE));
        assertTrue(s.isEmpty());
    }

    @Test
    void consumeMissingEffectReturnsZero() {
        StatusState s = new StatusState();
        assertEquals(0, s.consume(StatusEffect.POISE, 1));
    }

    @Test
    void snapshotIsDeepCopy() {
        StatusState s = new StatusState();
        s.add(StatusEffect.TREMOR, 5, 3);
        Map<StatusEffect, int[]> snap = s.snapshot();
        snap.get(StatusEffect.TREMOR)[0] = 99;
        assertEquals(5, s.potency(StatusEffect.TREMOR));
    }
}
