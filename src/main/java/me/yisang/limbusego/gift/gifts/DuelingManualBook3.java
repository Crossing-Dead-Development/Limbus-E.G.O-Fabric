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

/** 決鬥教材第3冊：被動每 5 秒獲得強壯 2·2；受擊 25% 機率獲得迅捷 2·3。 */
public class DuelingManualBook3 extends BaseGift {

    public DuelingManualBook3() {
        super("dueling_manual_book_3", 4); // Tier IV
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        if (!gate(player, 5000)) return;
        applyScaled(player, StatusEffect.POWER, 2, 2, player, self);
    }

    @Override
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        if (Math.random() < Math.min(1.0, 0.25 * multiplier(self))) {
            apply(victim, StatusEffect.HASTE, 2, 3, victim);
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.dueling_manual_book_3.passive"),
            TooltipFormat.body("tooltip.limbusego.dueling_manual_book_3.passive.power",
                    TooltipFormat.status(StatusEffect.POWER), TooltipFormat.potency(scaled(2, level), 2)),
            TooltipFormat.section("tooltip.limbusego.dueling_manual_book_3.damaged"),
            TooltipFormat.body("tooltip.limbusego.dueling_manual_book_3.damaged.haste",
                    TooltipFormat.num(Math.min(1.0, 0.25 * GiftUpgradeLogic.multiplier(level)) * 100),
                    TooltipFormat.status(StatusEffect.HASTE), TooltipFormat.potency(2, 3)));
    }
}
