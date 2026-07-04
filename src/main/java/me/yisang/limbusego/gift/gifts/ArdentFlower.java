package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 火光花：被動免疫火焰傷害；攻擊施加燒傷 2·2；
 * 攻擊燒傷中且生命低於 30% 的目標時 +30% 傷害。
 */
public class ArdentFlower extends BaseGift {

    public ArdentFlower() {
        super("ardent_flower");
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        // 免疫火焰：每 tick 續短時抗火（不顯示粒子）
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 30, 0, true, false));
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.BURN, 2, 2, attacker, self);
        float dmg = amount;
        double max = target.getMaxHealth();
        if (has(target, StatusEffect.BURN) && target.getHealth() < max * 0.30) {
            dmg *= 1.30f;
        }
        return dmg;
    }
}
