package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 輝光變動儀：攻擊施加震顫 2·2。 */
public class NixieDivergence extends BaseGift {

    public NixieDivergence() {
        super("nixie_divergence", 1); // Tier I
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.TREMOR, 2, 2, attacker, self);
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.nixie_divergence.attack"),
            TooltipFormat.body("tooltip.limbusego.nixie_divergence.attack.tremor",
                    TooltipFormat.status(StatusEffect.TREMOR), TooltipFormat.potency(scaled(2, level), 2)));
    }
}
