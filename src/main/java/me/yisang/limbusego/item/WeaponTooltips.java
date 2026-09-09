package me.yisang.limbusego.item;

import me.yisang.limbusego.tooltip.TooltipFormat;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 武器／彈藥的 Shift 展開說明集中表。
 *
 * <p>武器數值住在 {@code WeaponEvents} 與各 {@code *Item} class，不在單一 class，
 * 因此說明放集中表而非 per-class，與 {@link WeaponStyles} 的慣例一致。
 *
 * <p><b>修改武器數值時務必同步這裡。</b>本表的每個數字都對應 {@code WeaponEvents}
 * 中的常數或字面量，來源已標在各段註解。
 */
public final class WeaponTooltips {

    private static final String P = "tooltip.limbusego.";

    private static final Map<String, List<Text>> TABLE = Map.ofEntries(

        // 莊嚴哀悼（黑）：WeaponEvents.tickProjectiles isBlack 分支
        Map.entry("solemn_lament_black", List.of(
            TooltipFormat.section(P + "solemn_lament.fire"),
            TooltipFormat.body(P + "solemn_lament.fire.load"),
            TooltipFormat.body(P + "solemn_lament.fire.damage", 8),
            TooltipFormat.body(P + "solemn_lament.fire.wither", 4),
            TooltipFormat.body(P + "solemn_lament.fire.status",
                    TooltipFormat.status(StatusEffect.SINKING), TooltipFormat.potency(4, 3)),
            TooltipFormat.section(P + "solemn_lament.melee"),
            TooltipFormat.body(P + "solemn_lament.melee.note"))),

        // 莊嚴哀悼（白）：同上 else 分支
        Map.entry("solemn_lament_white", List.of(
            TooltipFormat.section(P + "solemn_lament.fire"),
            TooltipFormat.body(P + "solemn_lament.fire.load"),
            TooltipFormat.body(P + "solemn_lament.fire.damage", 4),
            TooltipFormat.body(P + "solemn_lament.fire.blind", 3),
            TooltipFormat.body(P + "solemn_lament.fire.status",
                    TooltipFormat.status(StatusEffect.SINKING), TooltipFormat.potency(3, 2)),
            TooltipFormat.section(P + "solemn_lament.melee"),
            TooltipFormat.body(P + "solemn_lament.melee.note"))),

        // 聖宣：WeaponEvents.tickShieldAura（每 5 tick = 0.25 秒）
        Map.entry("solemn_shield", List.of(
            TooltipFormat.section(P + "solemn_shield.aura"),
            TooltipFormat.body(P + "solemn_shield.aura.debuff", 5,
                    TooltipFormat.status(StatusEffect.BIND), TooltipFormat.potency(1, 2)),
            TooltipFormat.body(P + "solemn_shield.aura.self",
                    TooltipFormat.status(StatusEffect.PROTECTION), 3))),

        // 生蝶、亡蝶：莊嚴哀悼的彈藥
        Map.entry("butterfly_quartz", List.of(
            TooltipFormat.section(P + "ammo"),
            TooltipFormat.body(P + "butterfly_quartz.use"))),

        // 擬態：WeaponEvents.handleMimicry
        Map.entry("mimicry", List.of(
            TooltipFormat.section(P + "mimicry.attack"),
            TooltipFormat.body(P + "mimicry.attack.crit", 10, 40, 90),
            TooltipFormat.body(P + "mimicry.attack.power",
                    TooltipFormat.status(StatusEffect.POWER), TooltipFormat.potency(3, 4)),
            TooltipFormat.body(P + "mimicry.attack.lifesteal", 25))),

        // DaCapo：WeaponEvents.handleDaCapo / processDaCapo（AoE 3.5 格、70% 傷害）
        Map.entry("dacapo", List.of(
            TooltipFormat.section(P + "dacapo.attack"),
            TooltipFormat.body(P + "dacapo.attack.normal", 60, 5, "1.5"),
            TooltipFormat.body(P + "dacapo.attack.special", 40, 3, "5.0"),
            TooltipFormat.body(P + "dacapo.attack.aoe", "3.5", 70),
            TooltipFormat.body(P + "dacapo.attack.status",
                    TooltipFormat.status(StatusEffect.SINKING), TooltipFormat.potency(1, 1)))),

        // 環指筆刷：WeaponEvents.handleRingBrush / applyBrushEffect
        Map.entry("ring_brush", List.of(
            TooltipFormat.section(P + "ring_brush.strike"),
            TooltipFormat.body(P + "ring_brush.strike.damage", "3.5"),
            TooltipFormat.body(P + "ring_brush.strike.debuff"),
            TooltipFormat.body(P + "ring_brush.strike.status", TooltipFormat.potency(1, 3)),
            TooltipFormat.section(P + "ring_brush.double"),
            TooltipFormat.body(P + "ring_brush.double.effect"),
            TooltipFormat.section(P + "ring_brush.dash"),
            TooltipFormat.body(P + "ring_brush.dash.effect"))),

        // 天退星刀：TiantuiStarItem（蓄力 20/60 tick）＋ WeaponEvents.tickTiantuiDashes
        Map.entry("tiantui_star", List.of(
            TooltipFormat.section(P + "tiantui_star.tiger"),
            TooltipFormat.body(P + "tiantui_star.tiger.damage", 8),
            TooltipFormat.body(P + "tiantui_star.tiger.fire", 3),
            TooltipFormat.body(P + "tiantui_star.tiger.status",
                    TooltipFormat.status(StatusEffect.TREMOR), TooltipFormat.potency(5, 6),
                    TooltipFormat.status(StatusEffect.BURN), TooltipFormat.potency(4, 3)),
            TooltipFormat.section(P + "tiantui_star.savage"),
            TooltipFormat.body(P + "tiantui_star.savage.damage", 18),
            TooltipFormat.body(P + "tiantui_star.savage.fire", 5),
            TooltipFormat.body(P + "tiantui_star.savage.status",
                    TooltipFormat.status(StatusEffect.TREMOR), TooltipFormat.potency(8, 6),
                    TooltipFormat.status(StatusEffect.BURN), TooltipFormat.potency(6, 4)))),

        Map.entry("tiger_mark", List.of(
            TooltipFormat.section(P + "ammo"),
            TooltipFormat.body(P + "tiger_mark.use"))),

        Map.entry("savage_tiger_mark", List.of(
            TooltipFormat.section(P + "ammo"),
            TooltipFormat.body(P + "savage_tiger_mark.use"))),

        // 薄暝：WeaponEvents.handleTwilight / twilightSlash（TWILIGHT_MAX_LOWHP_BONUS 1.5 → 最高 ×2.5）
        Map.entry("twilight", List.of(
            TooltipFormat.section(P + "twilight.attack"),
            TooltipFormat.body(P + "twilight.attack.lowhp", "2.5"),
            TooltipFormat.body(P + "twilight.attack.true", 30),
            TooltipFormat.section(P + "twilight.slash"),
            TooltipFormat.body(P + "twilight.slash.damage", 55, 6, 14),
            TooltipFormat.body(P + "twilight.slash.inherit"),
            TooltipFormat.body(P + "twilight.slash.wither", 4),
            TooltipFormat.body(P + "twilight.slash.status",
                    TooltipFormat.status(StatusEffect.RUPTURE), TooltipFormat.potency(5, 2)),
            TooltipFormat.body(P + "twilight.slash.cooldown", 6))),

        // 提比婭：WeaponEvents.handleTibiaMelee / tibiaAnatomize
        Map.entry("tibia", List.of(
            TooltipFormat.section(P + "tibia.attack"),
            TooltipFormat.body(P + "tibia.attack.bleed",
                    TooltipFormat.status(StatusEffect.BLEED), TooltipFormat.potency(3, 2)),
            TooltipFormat.body(P + "tibia.attack.melody", 3, 3, 30),
            TooltipFormat.section(P + "tibia.anatomize"),
            TooltipFormat.body(P + "tibia.anatomize.damage", 60, 5, 16),
            TooltipFormat.body(P + "tibia.anatomize.true", 35),
            TooltipFormat.body(P + "tibia.anatomize.bleed",
                    TooltipFormat.status(StatusEffect.BLEED), TooltipFormat.potency(12, 6), 3),
            TooltipFormat.body(P + "tibia.anatomize.cooldown", 8))),

        // W 公司匕首：WeaponEvents.handleWCorpKnife（WCORP_CHARGE_CAP 10）
        Map.entry("w_corp_knife", List.of(
            TooltipFormat.section(P + "w_corp_knife.attack"),
            TooltipFormat.body(P + "w_corp_knife.attack.charge",
                    TooltipFormat.status(StatusEffect.CHARGE), TooltipFormat.potency(1, 5), 10),
            TooltipFormat.body(P + "w_corp_knife.attack.capped"),
            TooltipFormat.body(P + "w_corp_knife.attack.overload", 20,
                    TooltipFormat.status(StatusEffect.CHARGE), TooltipFormat.potency(1, 1)))),

        // 著影揮刀：WeaponEvents.handleBladesinger / bladesingerSlash
        // 觸發條件依程式為生命 ≤ 50%（README 的「<3 心」已過時）
        Map.entry("bladesinger", List.of(
            TooltipFormat.section(P + "bladesinger.attack"),
            TooltipFormat.body(P + "bladesinger.attack.poise",
                    TooltipFormat.status(StatusEffect.POISE), TooltipFormat.potency(1, 4), 10),
            TooltipFormat.section(P + "bladesinger.slash"),
            TooltipFormat.body(P + "bladesinger.slash.damage", 5, 7),
            TooltipFormat.body(P + "bladesinger.slash.rooted"),
            TooltipFormat.body(P + "bladesinger.slash.cooldown", 12)))
    );

    private WeaponTooltips() {}

    /** 查表；查無回傳空清單。 */
    public static List<Text> of(String itemId) {
        return TABLE.getOrDefault(itemId, List.of());
    }

    /** 表中所有 id。 */
    public static Set<String> ids() {
        return TABLE.keySet();
    }
}
