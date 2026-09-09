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

/** 生鏽的紀念幣：攻擊低血量目標時每 8 秒處決一次；擊殺獲得強壯 2·2。 */
public class RustyCommemorativeCoin extends BaseGift {

    public RustyCommemorativeCoin() {
        super("rusty_commemorative_coin", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float max = target.getMaxHealth();
        if (max <= 0) return amount;
        double m = multiplier(self);
        if (target.getHealth() / max >= Math.min(0.30, 0.15 * m)) return amount;
        if (!gate(attacker, 8000)) return amount;
        status().hurtTrue(target, attacker, target.getHealth() + 10, StatusEffect.RUPTURE);
        return amount;
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        applyScaled(killer, StatusEffect.POWER, 2, 2, killer, self);
    }

    @Override
    public List<Text> describe(int level) {
        int pct = Math.round((float) Math.min(0.30, 0.15 * GiftUpgradeLogic.multiplier(level)) * 100);
        return List.of(
            TooltipFormat.section("tooltip.limbusego.rusty_commemorative_coin.execute", pct),
            TooltipFormat.body("tooltip.limbusego.rusty_commemorative_coin.execute.kill"),
            TooltipFormat.body("tooltip.limbusego.rusty_commemorative_coin.execute.cooldown", 8),
            TooltipFormat.section("tooltip.limbusego.rusty_commemorative_coin.kill"),
            TooltipFormat.body("tooltip.limbusego.rusty_commemorative_coin.kill.power",
                    TooltipFormat.status(StatusEffect.POWER), TooltipFormat.potency(scaled(2, level), 2)));
    }
}
