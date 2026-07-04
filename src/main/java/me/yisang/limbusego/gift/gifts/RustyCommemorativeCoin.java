package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 生鏽的紀念幣：攻擊低血量目標時每 8 秒處決一次；擊殺獲得強壯 2·2。 */
public class RustyCommemorativeCoin extends BaseGift {

    public RustyCommemorativeCoin() {
        super("rusty_commemorative_coin", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float max = target.getMaxHealth();
        if (max <= 0) return amount;
        double m = multiplier(self);
        if (target.getHealth() / max >= Math.min(0.30, 0.15 * m)) return amount;
        if (!gate(attacker, 8000)) return amount;
        status().hurtTrue(target, attacker, target.getHealth() + 10, StatusEffect.RUPTURE);
        return amount;
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        applyScaled(killer, StatusEffect.POWER, 2, 2, killer, self);
    }
}
