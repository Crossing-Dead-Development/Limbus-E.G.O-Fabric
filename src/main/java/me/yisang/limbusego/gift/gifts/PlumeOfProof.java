package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 證明的羽飾：攻擊施加束縛 1·2 並獲得迅捷 1·2。 */
public class PlumeOfProof extends BaseGift {

    public PlumeOfProof() {
        super("plume_of_proof", 1); // Tier I
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.BIND, 1, 2, attacker, self);
        apply(attacker, StatusEffect.HASTE, 1, 2, attacker);
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.plume_of_proof.attack"),
            TooltipFormat.body("tooltip.limbusego.plume_of_proof.attack.bind",
                    TooltipFormat.status(StatusEffect.BIND), TooltipFormat.potency(scaled(1, level), 2)),
            TooltipFormat.body("tooltip.limbusego.plume_of_proof.attack.haste",
                    TooltipFormat.status(StatusEffect.HASTE), TooltipFormat.potency(1, 2)));
    }
}
