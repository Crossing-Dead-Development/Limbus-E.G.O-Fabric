package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 冰冷的幻想：攻擊施加沉淪 2·2 與束縛 1·2。 */
public class ColdIllusion extends BaseGift {

    public ColdIllusion() {
        super("cold_illusion", 4); // Tier IV
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.SINKING, 2, 2, attacker, self);
        apply(target, StatusEffect.BIND, 1, 2, attacker);
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.cold_illusion.attack"),
            TooltipFormat.body("tooltip.limbusego.cold_illusion.attack.sinking",
                    TooltipFormat.status(StatusEffect.SINKING), TooltipFormat.potency(scaled(2, level), 2)),
            TooltipFormat.body("tooltip.limbusego.cold_illusion.attack.bind",
                    TooltipFormat.status(StatusEffect.BIND), TooltipFormat.potency(1, 2)));
    }
}
