package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import me.yisang.limbusego.status.StatusState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 炎鱗：攻擊燒傷中目標時，引爆燒傷造成真傷（potency × 消耗量 × 0.5）
 * 並施加易損 1·2。內建冷卻 8 秒，隨升級倍率縮短。
 */
public class GlimpseOfFlames extends BaseGift {

    public GlimpseOfFlames() {
        super("glimpse_of_flames", 4); // Tier IV → brilliant_vestige
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        StatusState s = status().get(target);
        if (s == null || s.potency(StatusEffect.BURN) <= 0) return amount;
        double m = multiplier(self);
        if (!gate(attacker, (long) (8000 / m))) return amount;
        int p = s.potency(StatusEffect.BURN);
        int consumed = s.consume(StatusEffect.BURN, 5); // 引爆上限 c5
        if (consumed <= 0) return amount;
        status().hurtTrue(target, attacker, p * consumed * 0.5, StatusEffect.BURN);
        apply(target, StatusEffect.FRAGILE, 1, 2, attacker);
        return amount;
    }
}
