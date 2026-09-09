package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 刺青：大器晚成：生命低於 50% 時攻擊獲得強壯 2·2 與守護 2·2。 */
public class LateBloomersTattoo extends BaseGift {

    public LateBloomersTattoo() {
        super("late_bloomers_tattoo", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (attacker.getHealth() < attacker.getMaxHealth() * 0.5f) {
            applyScaled(attacker, StatusEffect.POWER, 2, 2, attacker, self);
            apply(attacker, StatusEffect.PROTECTION, 2, 2, attacker);
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.late_bloomers_tattoo.attack"),
            TooltipFormat.body("tooltip.limbusego.late_bloomers_tattoo.attack.power",
                    TooltipFormat.status(StatusEffect.POWER), TooltipFormat.potency(scaled(2, level), 2)),
            TooltipFormat.body("tooltip.limbusego.late_bloomers_tattoo.attack.protection",
                    TooltipFormat.status(StatusEffect.PROTECTION), TooltipFormat.potency(2, 2)));
    }
}
