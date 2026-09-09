package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 桶裝烈酒：攻擊獲得呼吸法 2·2；自身呼吸法≥4 時攻擊額外獲得 1 SAN。 */
public class CaskSpirits extends BaseGift {

    public CaskSpirits() {
        super("cask_spirits", 4); // Tier IV
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(attacker, StatusEffect.POISE, 2, 2, attacker, self);
        if (pot(attacker, StatusEffect.POISE) >= 4) {
            sanity().gainSan(attacker, 1);
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.cask_spirits.attack"),
            TooltipFormat.body("tooltip.limbusego.cask_spirits.attack.poise",
                    TooltipFormat.status(StatusEffect.POISE), TooltipFormat.potency(scaled(2, level), 2)),
            TooltipFormat.section("tooltip.limbusego.cask_spirits.high_poise",
                    TooltipFormat.status(StatusEffect.POISE), 4),
            TooltipFormat.body("tooltip.limbusego.cask_spirits.high_poise.san", 1));
    }
}
