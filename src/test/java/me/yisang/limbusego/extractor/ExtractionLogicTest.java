package me.yisang.limbusego.extractor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtractionLogicTest {

    @Test
    void costMatchesSpec() {
        assertEquals(8, ExtractionLogic.costOf(1));
        assertEquals(16, ExtractionLogic.costOf(2));
        assertEquals(32, ExtractionLogic.costOf(3));
        assertEquals(64, ExtractionLogic.costOf(4));
    }

    @Test
    void durationMatchesSpec() {
        assertEquals(100, ExtractionLogic.durationOf(1));
        assertEquals(160, ExtractionLogic.durationOf(2));
        assertEquals(240, ExtractionLogic.durationOf(3));
        assertEquals(400, ExtractionLogic.durationOf(4));
    }

    @Test
    void outOfRangeTierIsInvalid() {
        for (int tier : new int[]{-1, 0, 5, 99}) {
            assertEquals(-1, ExtractionLogic.costOf(tier), "costOf(" + tier + ")");
            assertEquals(-1, ExtractionLogic.durationOf(tier), "durationOf(" + tier + ")");
        }
    }

    @Test
    void startsWhenEverythingIsReady() {
        assertTrue(ExtractionLogic.canStart(1, 8, true, 10));
        assertTrue(ExtractionLogic.canStart(4, 64, true, 1));
        assertTrue(ExtractionLogic.canStart(2, 999, true, 3), "Enkephalin 超過所需也可以跑");
    }

    @Test
    void refusesInvalidTier() {
        // Vestiges.tierOf 對非殘影回 -1
        assertFalse(ExtractionLogic.canStart(-1, 64, true, 10));
        assertFalse(ExtractionLogic.canStart(0, 64, true, 10));
        assertFalse(ExtractionLogic.canStart(5, 64, true, 10));
    }

    @Test
    void refusesInsufficientEnkephalin() {
        assertFalse(ExtractionLogic.canStart(1, 7, true, 10));
        assertFalse(ExtractionLogic.canStart(3, 31, true, 10));
        assertFalse(ExtractionLogic.canStart(1, 0, true, 10));
    }

    @Test
    void refusesWhenOutputBlocked() {
        assertFalse(ExtractionLogic.canStart(1, 8, false, 10));
    }

    @Test
    void refusesEmptyPool() {
        assertFalse(ExtractionLogic.canStart(1, 8, true, 0));
        assertFalse(ExtractionLogic.canStart(1, 8, true, -3));
    }

    @Test
    void pickCoversBothEnds() {
        assertEquals(0, ExtractionLogic.pick(10, 0.0));
        assertEquals(9, ExtractionLogic.pick(10, 0.999999));
        assertEquals(5, ExtractionLogic.pick(10, 0.5));
    }

    @Test
    void pickNeverOverflowsEvenAtOne() {
        // Random.nextDouble() 理論上 < 1.0，但防禦性地把 1.0 也夾在最後一格
        assertEquals(9, ExtractionLogic.pick(10, 1.0));
        assertEquals(9, ExtractionLogic.pick(10, 1.7));
        assertEquals(0, ExtractionLogic.pick(10, -0.2));
    }

    @Test
    void pickSingletonPoolIsAlwaysZero() {
        for (double roll : new double[]{0.0, 0.3, 0.99, 1.0}) {
            assertEquals(0, ExtractionLogic.pick(1, roll));
        }
    }
}
