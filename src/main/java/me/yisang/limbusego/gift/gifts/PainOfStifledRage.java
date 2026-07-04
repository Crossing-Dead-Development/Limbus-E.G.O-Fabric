package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 鬱火：攻擊燒傷中目標則自身獲得強壯 2·1；否則對目標施加燒傷 2·2。 */
public class PainOfStifledRage extends BaseGift {

    public PainOfStifledRage() {
        super("pain_of_stifled_rage", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (has(target, StatusEffect.BURN)) {
            applyScaled(attacker, StatusEffect.POWER, 2, 1, attacker, self);
        } else {
            applyScaled(target, StatusEffect.BURN, 2, 2, attacker, self);
        }
        return amount;
    }
}
