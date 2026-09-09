package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 特殊合約：攻擊施加脆弱 2·2。 */
public class SpecialContract extends BaseGift {

    public SpecialContract() {
        super("special_contract", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.FRAGILE, 2, 2, attacker, self);
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.special_contract.attack"),
            TooltipFormat.body("tooltip.limbusego.special_contract.attack.fragile",
                    TooltipFormat.status(StatusEffect.FRAGILE), TooltipFormat.potency(scaled(2, level), 2)));
    }
}
