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

/** 磨尖的樹枝：攻擊 20% 機率（隨升級）+30% 傷害並獲得呼吸法 1·1。 */
public class Keenbranch extends BaseGift {

    public Keenbranch() {
        super("keenbranch", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (Math.random() < Math.min(1.0, 0.20 * multiplier(self))) {
            dmg *= 1.30f;
            apply(attacker, StatusEffect.POISE, 1, 1, attacker);
        }
        return dmg;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.keenbranch.attack",
                    TooltipFormat.num(Math.min(1.0, 0.20 * GiftUpgradeLogic.multiplier(level)) * 100)),
            TooltipFormat.body("tooltip.limbusego.keenbranch.attack.bonus", 30),
            TooltipFormat.body("tooltip.limbusego.keenbranch.attack.poise",
                    TooltipFormat.status(StatusEffect.POISE), TooltipFormat.potency(1, 1)));
    }
}
