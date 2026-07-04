package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 刺青：大器晚成：生命低於 50% 時攻擊獲得強壯 2·2 與守護 2·2。 */
public class LateBloomersTattoo extends BaseGift {

    public LateBloomersTattoo() {
        super("late_bloomers_tattoo", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (attacker.getHealth() < attacker.getMaxHealth() * 0.5f) {
            applyScaled(attacker, StatusEffect.POWER, 2, 2, attacker, self);
            apply(attacker, StatusEffect.PROTECTION, 2, 2, attacker);
        }
        return amount;
    }
}
