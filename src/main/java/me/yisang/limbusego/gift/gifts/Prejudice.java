package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 偏見：攻擊血量比例低於自己的目標時最多 +30% 傷害。 */
public class Prejudice extends BaseGift {

    public Prejudice() {
        super("prejudice", 1); // Tier I
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float attackerMax = attacker.getMaxHealth();
        float targetMax = target.getMaxHealth();
        if (targetMax <= 0 || attackerMax <= 0) return amount;
        if (target.getHealth() / targetMax >= attacker.getHealth() / attackerMax) return amount;
        return amount * (float) (1.0 + Math.min(0.30, 0.15 * multiplier(self)));
    }
}
