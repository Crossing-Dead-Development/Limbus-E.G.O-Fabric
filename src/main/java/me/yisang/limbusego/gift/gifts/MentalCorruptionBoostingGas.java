package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 精神汙染加速氣體：攻擊施加沉淪 2·2；目標為玩家時額外 -1 SAN。 */
public class MentalCorruptionBoostingGas extends BaseGift {

    public MentalCorruptionBoostingGas() {
        super("mental_corruption_boosting_gas", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.SINKING, 2, 2, attacker, self);
        if (target instanceof ServerPlayerEntity pv) {
            sanity().dropSan(pv, 1);
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.mental_corruption_boosting_gas.attack"),
            TooltipFormat.body("tooltip.limbusego.mental_corruption_boosting_gas.attack.sinking",
                    TooltipFormat.status(StatusEffect.SINKING), TooltipFormat.potency(scaled(2, level), 2)),
            TooltipFormat.section("tooltip.limbusego.mental_corruption_boosting_gas.vs_player"),
            TooltipFormat.body("tooltip.limbusego.mental_corruption_boosting_gas.vs_player.san", 1));
    }
}
