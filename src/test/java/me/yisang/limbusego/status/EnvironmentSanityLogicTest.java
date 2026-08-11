package me.yisang.limbusego.status;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentSanityLogicTest {

    // ── 門檻邊界 ──────────────────────────────────────────────────────────

    @Test
    void lightFourIsDarkAndFiveIsNeutral() {
        // 光照 4（午夜露天）：曝光值往負走
        assertEquals(-1, EnvironmentSanityLogic.step(4, 0, 0, false).exposure());
        // 光照 5：中性，曝光值 0 維持 0
        assertEquals(0, EnvironmentSanityLogic.step(5, 0, 0, false).exposure());
    }

    @Test
    void lightEightIsNeutralAndNineIsBright() {
        assertEquals(0, EnvironmentSanityLogic.step(8, 0, 0, false).exposure());
        assertEquals(1, EnvironmentSanityLogic.step(9, 0, 0, false).exposure());
    }

    // ── 累計器 ────────────────────────────────────────────────────────────

    @Test
    void darkDropsOneSanOnlyAfterThresholdSteps() {
        int exposure = 0;
        // 前 14 步：只累積，不動 SAN
        for (int i = 0; i < EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1; i++) {
            EnvironmentSanityLogic.Result r =
                    EnvironmentSanityLogic.step(0, exposure, 0, false);
            assertEquals(0, r.sanDelta(), "第 " + (i + 1) + " 步不該動 SAN");
            exposure = r.exposure();
        }
        // 第 15 步：扣 1 點，累計器歸零
        EnvironmentSanityLogic.Result last =
                EnvironmentSanityLogic.step(0, exposure, 0, false);
        assertEquals(-1, last.sanDelta());
        assertEquals(0, last.exposure(), "跨門檻後累計器必須歸零");
    }

    @Test
    void neutralDecaysExposureTowardZero() {
        assertEquals(4, EnvironmentSanityLogic.step(7, 5, 0, false).exposure());
        assertEquals(-4, EnvironmentSanityLogic.step(7, -5, 0, false).exposure());
        assertEquals(0, EnvironmentSanityLogic.step(7, 0, 0, false).exposure());
    }

    // ── 明亮回復只到 0 ────────────────────────────────────────────────────

    @Test
    void brightRecoveryAppliesWhenSanIsNegative() {
        int atThreshold = EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1;
        EnvironmentSanityLogic.Result r =
                EnvironmentSanityLogic.step(15, atThreshold, -5, false);
        assertEquals(1, r.sanDelta());
        assertEquals(0, r.exposure());
    }

    @Test
    void brightRecoveryDoesNothingWhenSanIsZero() {
        int atThreshold = EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1;
        EnvironmentSanityLogic.Result r =
                EnvironmentSanityLogic.step(15, atThreshold, 0, false);
        assertEquals(0, r.sanDelta(), "SAN 已是 0，明亮不得使其變正");
        assertEquals(0, r.exposure(), "累計器仍須歸零");
    }

    @Test
    void brightRecoveryDoesNothingWhenSanIsPositive() {
        int atThreshold = EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1;
        assertEquals(0, EnvironmentSanityLogic.step(15, atThreshold, 20, false).sanDelta());
    }

    // ── 脫戰回復與黑暗抑制 ────────────────────────────────────────────────

    @Test
    void outOfCombatRecoveryAppliesWhenNotDark() {
        // 中性亮度、脫戰、SAN 為負 → +1
        assertEquals(1, EnvironmentSanityLogic.step(7, 0, -5, true).sanDelta());
    }

    @Test
    void darkSuppressesOutOfCombatRecovery() {
        // 黑暗中脫戰也不回復
        assertEquals(0, EnvironmentSanityLogic.step(0, 0, -5, true).sanDelta());
    }

    @Test
    void outOfCombatRecoveryDoesNothingWhenSanIsZero() {
        assertEquals(0, EnvironmentSanityLogic.step(7, 0, 0, true).sanDelta());
    }

    @Test
    void inCombatMeansNoOutOfCombatRecovery() {
        assertEquals(0, EnvironmentSanityLogic.step(7, 0, -5, false).sanDelta());
    }

    // ── 不變式：回復永不越過 0 ────────────────────────────────────────────

    @Test
    void combinedRecoveryNeverPushesSanAboveZero() {
        // 明亮 + 脫戰 + 曝光值剛好跨門檻，兩條回復可能同時觸發
        int atThreshold = EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1;
        for (int san = -3; san <= 0; san++) {
            EnvironmentSanityLogic.Result r =
                    EnvironmentSanityLogic.step(15, atThreshold, san, true);
            assertTrue(san + r.sanDelta() <= 0,
                    "SAN " + san + " + delta " + r.sanDelta() + " 不得越過 0");
        }
    }

    @Test
    void combinedRecoveryReachesZeroExactly() {
        int atThreshold = EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1;
        EnvironmentSanityLogic.Result r =
                EnvironmentSanityLogic.step(15, atThreshold, -2, true);
        assertEquals(2, r.sanDelta(), "門檻回復 +1 與脫戰回復 +1 應同時生效");
    }

    // ── 黑暗掉落不受 SAN 值影響（夾制由 SanityManager.setSan 負責） ────────

    @Test
    void darkStillReturnsNegativeDeltaAtFloor() {
        int atThreshold = -(EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1);
        assertEquals(-1, EnvironmentSanityLogic.step(0, atThreshold, -45, false).sanDelta());
    }
}
