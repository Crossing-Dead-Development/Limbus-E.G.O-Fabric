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

/** 破布：攻擊沉淪中目標時 +7.5% 傷害（隨升級，上限 30%）並獲得 1 SAN。 */
public class Rags extends BaseGift {

    public Rags() {
        super("rags", 1); // Tier I
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (has(target, StatusEffect.SINKING)) {
            dmg *= (float) (1.0 + Math.min(0.30, 0.075 * multiplier(self)));
            sanity().gainSan(attacker, 1);
        }
        return dmg;
    }

    @Override
    public List<Text> describe(int level) {
        int pct = Math.round((float) Math.min(0.30, 0.075 * GiftUpgradeLogic.multiplier(level)) * 100);
        return List.of(
            TooltipFormat.section("tooltip.limbusego.rags.attack",
                    TooltipFormat.status(StatusEffect.SINKING)),
            TooltipFormat.body("tooltip.limbusego.rags.attack.bonus", pct),
            TooltipFormat.body("tooltip.limbusego.rags.attack.san", 1));
    }
}
