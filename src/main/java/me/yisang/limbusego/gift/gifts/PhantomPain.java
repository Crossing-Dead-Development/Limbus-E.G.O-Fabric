package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;
import me.yisang.limbusego.gift.GiftUpgradeLogic;

/** 幻痛：攻擊 +15% 傷害（隨升級，上限 30%）。 */
public class PhantomPain extends BaseGift {

    public PhantomPain() {
        super("phantom_pain", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        return amount * (float) (1.0 + Math.min(0.30, 0.15 * multiplier(self)));
    }

    @Override
    public List<Text> describe(int level) {
        int pct = Math.round((float) Math.min(0.30, 0.15 * GiftUpgradeLogic.multiplier(level)) * 100);
        return List.of(
            TooltipFormat.section("tooltip.limbusego.phantom_pain.attack"),
            TooltipFormat.body("tooltip.limbusego.phantom_pain.attack.bonus", pct));
    }
}
