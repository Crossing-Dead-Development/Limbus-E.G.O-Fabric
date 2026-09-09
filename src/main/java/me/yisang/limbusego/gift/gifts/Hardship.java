package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 苦難：SAN≥40 時攻擊獲得強壯 2·1；擊殺獲得 2 SAN。 */
public class Hardship extends BaseGift {

    public Hardship() {
        super("hardship", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (sanity().getSan(attacker) >= 40) {
            applyScaled(attacker, StatusEffect.POWER, 2, 1, attacker, self);
        }
        return amount;
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        sanity().gainSan(killer, 2);
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.hardship.attack", 40),
            TooltipFormat.body("tooltip.limbusego.hardship.attack.power",
                    TooltipFormat.status(StatusEffect.POWER), TooltipFormat.potency(scaled(2, level), 1)),
            TooltipFormat.section("tooltip.limbusego.hardship.kill"),
            TooltipFormat.body("tooltip.limbusego.hardship.kill.san", 2));
    }
}
