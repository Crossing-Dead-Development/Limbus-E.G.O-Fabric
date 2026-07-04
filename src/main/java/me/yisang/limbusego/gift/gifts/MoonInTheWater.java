package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 水中月：被動夜視；攻擊破裂≥3 目標時自身獲得呼吸法 2·1。 */
public class MoonInTheWater extends BaseGift {

    public MoonInTheWater() {
        super("moon_in_the_water", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 30, 0, true, false));
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (pot(target, StatusEffect.RUPTURE) >= 3) {
            applyScaled(attacker, StatusEffect.POISE, 2, 1, attacker, self);
        }
        return amount;
    }
}
