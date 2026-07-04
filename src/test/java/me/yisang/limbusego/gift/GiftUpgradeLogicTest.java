package me.yisang.limbusego.gift;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GiftUpgradeLogicTest {

    @Test
    void matchingTierIncrementsLevel() {
        // dark_vestige(1) 升 Tier I 飾品，等級 0→1
        assertEquals(1, GiftUpgradeLogic.resolveUpgrade(1, 1, 0));
        assertEquals(2, GiftUpgradeLogic.resolveUpgrade(3, 3, 1));
        assertEquals(3, GiftUpgradeLogic.resolveUpgrade(4, 4, 2));
    }

    @Test
    void mismatchedTierRejected() {
        // brilliant_vestige(4) 不能升 Tier I 飾品
        assertEquals(-1, GiftUpgradeLogic.resolveUpgrade(1, 4, 0));
        assertEquals(-1, GiftUpgradeLogic.resolveUpgrade(3, 2, 0));
    }

    @Test
    void maxedLevelRejected() {
        assertEquals(-1, GiftUpgradeLogic.resolveUpgrade(2, 2, GiftUpgradeLogic.MAX_LEVEL));
        assertEquals(-1, GiftUpgradeLogic.resolveUpgrade(2, 2, 4)); // 超出上限也拒絕
    }

    @Test
    void negativeLevelRejected() {
        assertEquals(-1, GiftUpgradeLogic.resolveUpgrade(1, 1, -1));
    }

    @Test
    void multiplierByLevel() {
        assertEquals(1.0, GiftUpgradeLogic.multiplier(0));
        assertEquals(1.25, GiftUpgradeLogic.multiplier(1));
        assertEquals(1.50, GiftUpgradeLogic.multiplier(2));
        assertEquals(2.00, GiftUpgradeLogic.multiplier(3));
        assertEquals(1.0, GiftUpgradeLogic.multiplier(99)); // 越界回預設
    }
}
