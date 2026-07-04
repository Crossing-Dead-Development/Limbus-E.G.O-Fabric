package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 火熱多汁枇杷腿：被動飽食度不流失；攻擊燒傷中目標時延長燒傷持續 2 層。 */
public class HotNJuicyDrumstick extends BaseGift {

    public HotNJuicyDrumstick() {
        super("hot_n_juicy_drumstick", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 30, 0, true, false));
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (has(target, StatusEffect.BURN)) {
            status().refresh(target, StatusEffect.BURN, 2);
        }
        return amount;
    }
}
