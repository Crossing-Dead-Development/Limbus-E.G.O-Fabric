package me.yisang.limbusego.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 只測配對判定。
 *
 * <p>「輪流發射」沒有對應的測試，因為那不是我們的邏輯：vanilla 的
 * {@code MinecraftClient.doItemUse} 依序試主手、副手並在 {@code isAccepted()} 時停止，
 * 而 {@code interactItem} 在呼叫 {@code Item.use} 之前就會擋掉冷卻中的那隻手。
 * 交替因此是 vanilla 行為的副產品，單元測試無從涵蓋——只能在遊戲內驗證。
 */
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
}
