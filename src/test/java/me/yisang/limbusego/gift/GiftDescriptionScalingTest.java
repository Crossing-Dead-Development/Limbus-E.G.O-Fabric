package me.yisang.limbusego.gift;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 防止 tooltip 說一套、程式算另一套。
 *
 * <p>每個斷言都把「說明裡印出的數字」與「照著該飾品實際邏輯重算一次的數字」對照，
 * 若日後有人改了平衡卻忘了改說明，這裡會紅。
 */
class GiftDescriptionScalingTest {

    @Test
    void restBonusMatchesActualLogicAndCaps() {
        var rest = GiftDescriptionCoverageTest.registered().get("rest");
        assertNotNull(rest);

        // Rest.onAttack：dmg *= 1.0 + Math.min(0.30, 0.15 * multiplier(self))
        for (int level = 0; level <= 3; level++) {
            int expected = Math.round((float) Math.min(0.30, 0.15 * GiftUpgradeLogic.multiplier(level)) * 100);
            assertTrue(args(rest.describe(level)).contains(expected),
                    "rest 在 Lv." + level + " 的說明應含 " + expected + "，實得 " + args(rest.describe(level)));
        }
        assertEquals(15, Math.round((float) Math.min(0.30, 0.15 * GiftUpgradeLogic.multiplier(0)) * 100),
                "Lv.0 應為基礎 15%");
        assertEquals(30, Math.round((float) Math.min(0.30, 0.15 * GiftUpgradeLogic.multiplier(3)) * 100),
                "Lv.3 應被 30% 上限截斷");
    }

    @Test
    void ragsBonusIsCappedAtThirtyPercent() {
        var rags = GiftDescriptionCoverageTest.registered().get("rags");
        // Rags.onAttack：Math.min(0.30, 0.075 * multiplier)，Lv.3 為 0.15 → 未觸及上限
        assertTrue(args(rags.describe(0)).contains(8), "Lv.0 應為 8%（0.075 四捨五入）");
        assertTrue(args(rags.describe(3)).contains(15), "Lv.3 應為 15%");
    }

    @Test
    void ashesToAshesPotencyMatchesApplyScaled() {
        var ashes = GiftDescriptionCoverageTest.registered().get("ashes_to_ashes");
        // AshesToAshes：applyScaled(BURN, 2, 1) → Math.round(2 * multiplier)
        for (int level = 0; level <= 3; level++) {
            int expected = (int) Math.round(2 * GiftUpgradeLogic.multiplier(level));
            assertTrue(args(ashes.describe(level)).contains(expected),
                    "ashes_to_ashes 在 Lv." + level + " 的威力應為 " + expected);
        }
    }

    @Test
    void descriptionsDifferAcrossLevelsWhereScalingExists() {
        var rest = GiftDescriptionCoverageTest.registered().get("rest");
        assertNotEquals(args(rest.describe(0)), args(rest.describe(3)),
                "有縮放的飾品在 Lv.0 與 Lv.3 的說明不應相同");
    }

    /** 取出說明中所有 translatable 參數（含巢狀），未翻譯時 getString() 會丟掉它們。 */
    private static List<Object> args(List<Text> lines) {
        var out = new java.util.ArrayList<>();
        lines.forEach(line -> collect(line, out));
        return out;
    }

    private static void collect(Text text, List<Object> out) {
        if (text.getContent() instanceof TranslatableTextContent t) {
            for (Object arg : t.getArgs()) {
                if (arg instanceof Text nested) collect(nested, out);
                else out.add(arg);
            }
        }
        text.getSiblings().forEach(s -> collect(s, out));
    }
}
