package me.yisang.limbusego.status;

import org.junit.jupiter.api.Test;

import static me.yisang.limbusego.status.StatusDisplayLogic.Action.*;
import static org.junit.jupiter.api.Assertions.*;

class StatusDisplayLogicTest {

    @Test
    void addsWhenAbsent() {
        var d = StatusDisplayLogic.decide(3, null);
        assertEquals(ADD, d.action());
        assertEquals(2, d.amplifier());
    }

    @Test
    void updatesWhenAmplifierDiffers() {
        var d = StatusDisplayLogic.decide(5, 2);
        assertEquals(UPDATE, d.action());
        assertEquals(4, d.amplifier());
    }

    @Test
    void updatesWhenPotencyDrops() {
        // 原版 addStatusEffect 不會降 amplifier，所以降級也必須是 UPDATE 而非 NONE
        var d = StatusDisplayLogic.decide(1, 2);
        assertEquals(UPDATE, d.action());
        assertEquals(0, d.amplifier());
    }

    @Test
    void noopWhenUnchanged() {
        assertEquals(NONE, StatusDisplayLogic.decide(3, 2).action());
    }

    @Test
    void removesWhenPotencyGone() {
        assertEquals(REMOVE, StatusDisplayLogic.decide(0, 2).action());
    }

    @Test
    void noopWhenNothingOnEitherSide() {
        assertEquals(NONE, StatusDisplayLogic.decide(0, null).action());
    }

    @Test
    void particlesOnlyForOthers() {
        Object a = new Object();
        Object b = new Object();
        assertFalse(StatusDisplayLogic.shouldSpawnParticles(null, a), "無 source 不冒");
        assertFalse(StatusDisplayLogic.shouldSpawnParticles(a, a), "source == target 不冒");
        assertTrue(StatusDisplayLogic.shouldSpawnParticles(a, b), "打別人才冒");
    }
}
