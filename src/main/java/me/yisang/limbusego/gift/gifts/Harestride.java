package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 卯足：被動速度 II、跳躍提升 I；速度效果中攻擊施加破裂 2·2。 */
public class Harestride extends BaseGift {

    public Harestride() {
        super("harestride", 2); // Tier II
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 30, 1, true, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 30, 0, true, false));
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (attacker.hasStatusEffect(StatusEffects.SPEED)) {
            applyScaled(target, StatusEffect.RUPTURE, 2, 2, attacker, self);
        }
        return amount;
    }
}
