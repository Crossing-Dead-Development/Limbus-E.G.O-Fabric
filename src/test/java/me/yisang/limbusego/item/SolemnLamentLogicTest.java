package me.yisang.limbusego.item;

import net.minecraft.util.Hand;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SolemnLamentLogicTest {

    @Test
    void pairingRequiresOneBlackAndOneWhite() {
        // 主手黑 + 副手白
        assertTrue(SolemnLamentLogic.isPaired(true, true, true, false));
        // 主手白 + 副手黑
        assertTrue(SolemnLamentLogic.isPaired(true, false, true, true));
    }

    @Test
    void sameColourIsNotPaired() {
        assertFalse(SolemnLamentLogic.isPaired(true, true, true, true), "黑+黑不應成立");
        assertFalse(SolemnLamentLogic.isPaired(true, false, true, false), "白+白不應成立");
    }

    @Test
    void singleWieldIsNotPaired() {
        assertFalse(SolemnLamentLogic.isPaired(true, true, false, false), "副手非莊嚴哀悼");
        assertFalse(SolemnLamentLogic.isPaired(false, false, true, true), "主手非莊嚴哀悼");
        assertFalse(SolemnLamentLogic.isPaired(false, false, false, false), "兩手都不是");
    }

    @Test
    void pickHandPrefersMainWhenBothReady() {
        assertEquals(Optional.of(Hand.MAIN_HAND), SolemnLamentLogic.pickHand(true, true));
    }

    @Test
    void pickHandFallsBackToOffHand() {
        assertEquals(Optional.of(Hand.OFF_HAND), SolemnLamentLogic.pickHand(false, true));
    }

    @Test
    void pickHandReturnsEmptyWhenBothOnCooldown() {
        assertEquals(Optional.empty(), SolemnLamentLogic.pickHand(false, false));
    }

    @Test
    void alternationEmergesFromCooldownAlone() {
        // 模擬連點：每把冷卻 24 tick，每 12 tick 點一次，應交替
        // readyAt[0]=主手可用的 tick，readyAt[1]=副手
        int[] readyAt = {0, 0};
        Hand[] fired = new Hand[4];
        int now = 0;
        for (int i = 0; i < 4; i++) {
            Optional<Hand> pick = SolemnLamentLogic.pickHand(readyAt[0] <= now, readyAt[1] <= now);
            fired[i] = pick.orElseThrow();
            readyAt[fired[i] == Hand.MAIN_HAND ? 0 : 1] = now + 24;
            now += 12;
        }
        assertArrayEquals(
                new Hand[]{Hand.MAIN_HAND, Hand.OFF_HAND, Hand.MAIN_HAND, Hand.OFF_HAND},
                fired,
                "每 12 tick 點一次應穩定交替");
    }
}
