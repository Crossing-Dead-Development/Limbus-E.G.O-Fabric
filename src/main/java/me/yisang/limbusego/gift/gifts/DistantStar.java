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

/** 彼方之星：攻擊沉淪中目標時獲得 1 SAN（隨升級）並延長沉淪 1 層。 */
public class DistantStar extends BaseGift {

    public DistantStar() {
        super("distant_star", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (has(target, StatusEffect.SINKING)) {
            sanity().gainSan(attacker, (int) Math.round(1 * multiplier(self)));
            status().refresh(target, StatusEffect.SINKING, 1);
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.distant_star.attack",
                    TooltipFormat.status(StatusEffect.SINKING)),
            TooltipFormat.body("tooltip.limbusego.distant_star.attack.san",
                    Math.round(1 * GiftUpgradeLogic.multiplier(level))),
            TooltipFormat.body("tooltip.limbusego.distant_star.attack.extend",
                    TooltipFormat.status(StatusEffect.SINKING), 1));
    }
}
