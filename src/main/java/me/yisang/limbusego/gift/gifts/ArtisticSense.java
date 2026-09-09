package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 美感：攻擊施加沉淪 2·2；攻擊沉淪中或抑鬱目標 +25% 傷害。 */
public class ArtisticSense extends BaseGift {

    public ArtisticSense() {
        super("artistic_sense", 4); // Tier IV
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (has(target, StatusEffect.SINKING) || sanity().isDepressed(target)) {
            dmg *= 1.25f;
        }
        applyScaled(target, StatusEffect.SINKING, 2, 2, attacker, self);
        return dmg;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.artistic_sense.attack"),
            TooltipFormat.body("tooltip.limbusego.artistic_sense.attack.sinking",
                    TooltipFormat.status(StatusEffect.SINKING), TooltipFormat.potency(scaled(2, level), 2)),
            TooltipFormat.section("tooltip.limbusego.artistic_sense.vs_sinking",
                    TooltipFormat.status(StatusEffect.SINKING)),
            TooltipFormat.body("tooltip.limbusego.artistic_sense.vs_sinking.bonus", 25));
    }
}
