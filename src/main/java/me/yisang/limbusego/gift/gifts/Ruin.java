package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 破滅：攻擊施加破裂 3·2；對已破裂目標追加脆弱 1·1，但自身損失 0.5 生命。 */
public class Ruin extends BaseGift {

    public Ruin() {
        super("ruin", 4); // Tier IV
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        boolean already = has(target, StatusEffect.RUPTURE);
        applyScaled(target, StatusEffect.RUPTURE, 3, 2, attacker, self);
        if (already) {
            apply(target, StatusEffect.FRAGILE, 1, 1, attacker);
            attacker.setHealth((float) Math.max(1.0, attacker.getHealth() - 0.5));
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.ruin.attack"),
            TooltipFormat.body("tooltip.limbusego.ruin.attack.rupture",
                    TooltipFormat.status(StatusEffect.RUPTURE), TooltipFormat.potency(scaled(3, level), 2)),
            TooltipFormat.section("tooltip.limbusego.ruin.already",
                    TooltipFormat.status(StatusEffect.RUPTURE)),
            TooltipFormat.body("tooltip.limbusego.ruin.already.fragile",
                    TooltipFormat.status(StatusEffect.FRAGILE), TooltipFormat.potency(1, 1)),
            TooltipFormat.body("tooltip.limbusego.ruin.already.cost", "0.5"));
    }
}
