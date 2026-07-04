package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 酸味的酒香：攻擊施加震顫 2·1；攻擊震顫≥3 目標時 +20% 傷害（隨升級，上限 30%）。 */
public class SourLiquorAroma extends BaseGift {

    public SourLiquorAroma() {
        super("sour_liquor_aroma", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (pot(target, StatusEffect.TREMOR) >= 3) {
            dmg *= (float) (1.0 + Math.min(0.30, 0.20 * multiplier(self)));
        }
        apply(target, StatusEffect.TREMOR, 2, 1, attacker);
        return dmg;
    }
}
