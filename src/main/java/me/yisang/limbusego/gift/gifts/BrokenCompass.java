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

/** 破碎羅盤：攻擊 25% 機率施加沉淪 2·3（機率隨升級提升）。 */
public class BrokenCompass extends BaseGift {

    public BrokenCompass() {
        super("broken_compass", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (Math.random() < Math.min(1.0, 0.25 * multiplier(self))) {
            apply(target, StatusEffect.SINKING, 2, 3, attacker);
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.broken_compass.attack"),
            TooltipFormat.body("tooltip.limbusego.broken_compass.attack.chance",
                    TooltipFormat.num(Math.min(1.0, 0.25 * GiftUpgradeLogic.multiplier(level)) * 100),
                    TooltipFormat.status(StatusEffect.SINKING), TooltipFormat.potency(2, 3)));
    }
}
