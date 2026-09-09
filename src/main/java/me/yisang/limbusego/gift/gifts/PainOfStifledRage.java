package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 鬱火：攻擊燒傷中目標則自身獲得強壯 2·1；否則對目標施加燒傷 2·2。 */
public class PainOfStifledRage extends BaseGift {

    public PainOfStifledRage() {
        super("pain_of_stifled_rage", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (has(target, StatusEffect.BURN)) {
            applyScaled(attacker, StatusEffect.POWER, 2, 1, attacker, self);
        } else {
            applyScaled(target, StatusEffect.BURN, 2, 2, attacker, self);
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.pain_of_stifled_rage.burning"),
            TooltipFormat.body("tooltip.limbusego.pain_of_stifled_rage.burning.power",
                    TooltipFormat.status(StatusEffect.POWER), TooltipFormat.potency(scaled(2, level), 1)),
            TooltipFormat.section("tooltip.limbusego.pain_of_stifled_rage.otherwise"),
            TooltipFormat.body("tooltip.limbusego.pain_of_stifled_rage.otherwise.burn",
                    TooltipFormat.status(StatusEffect.BURN), TooltipFormat.potency(scaled(2, level), 2)));
    }
}
