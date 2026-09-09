package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;
import me.yisang.limbusego.gift.GiftUpgradeLogic;

/** 酸味的酒香：攻擊施加震顫 2·1；攻擊震顫≥3 目標時 +20% 傷害（隨升級，上限 30%）。 */
public class SourLiquorAroma extends BaseGift {

    public SourLiquorAroma() {
        super("sour_liquor_aroma", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (pot(target, StatusEffect.TREMOR) >= 3) {
            dmg *= (float) (1.0 + Math.min(0.30, 0.20 * multiplier(self)));
        }
        apply(target, StatusEffect.TREMOR, 2, 1, attacker);
        return dmg;
    }

    @Override
    public List<Text> describe(int level) {
        int pct = Math.round((float) Math.min(0.30, 0.20 * GiftUpgradeLogic.multiplier(level)) * 100);
        return List.of(
            TooltipFormat.section("tooltip.limbusego.sour_liquor_aroma.attack"),
            TooltipFormat.body("tooltip.limbusego.sour_liquor_aroma.attack.tremor",
                    TooltipFormat.status(StatusEffect.TREMOR), TooltipFormat.potency(2, 1)),
            TooltipFormat.section("tooltip.limbusego.sour_liquor_aroma.vs_deep",
                    TooltipFormat.status(StatusEffect.TREMOR), 3),
            TooltipFormat.body("tooltip.limbusego.sour_liquor_aroma.vs_deep.bonus", pct));
    }
}
