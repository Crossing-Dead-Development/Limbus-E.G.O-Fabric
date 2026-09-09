package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 強韌丸：受擊獲得守護 2·2。 */
public class TenacityBolus extends BaseGift {

    public TenacityBolus() {
        super("tenacity_bolus", 2); // Tier II
    }

    @Override
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        applyScaled(victim, StatusEffect.PROTECTION, 2, 2, victim, self);
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.tenacity_bolus.damaged"),
            TooltipFormat.body("tooltip.limbusego.tenacity_bolus.damaged.protection",
                    TooltipFormat.status(StatusEffect.PROTECTION), TooltipFormat.potency(scaled(2, level), 2)));
    }
}
