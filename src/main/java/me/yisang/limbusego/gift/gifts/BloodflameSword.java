package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 血炎刀：攻擊施加燒傷 3·2 並獲得 1 點 SAN。 */
public class BloodflameSword extends BaseGift {

    public BloodflameSword() {
        super("bloodflame_sword", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.BURN, 3, 2, attacker, self);
        sanity().gainSan(attacker, 1);
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.bloodflame_sword.attack"),
            TooltipFormat.body("tooltip.limbusego.bloodflame_sword.attack.burn",
                    TooltipFormat.status(StatusEffect.BURN), TooltipFormat.potency(scaled(3, level), 2)),
            TooltipFormat.body("tooltip.limbusego.bloodflame_sword.attack.san", 1));
    }
}
