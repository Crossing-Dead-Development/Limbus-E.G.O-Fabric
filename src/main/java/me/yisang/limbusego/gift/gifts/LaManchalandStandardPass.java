package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 拉．曼查樂園常規通行券：被動速度 I；攻擊流血中目標獲得呼吸法 1·1。 */
public class LaManchalandStandardPass extends BaseGift {

    public LaManchalandStandardPass() {
        super("la_manchaland_standard_pass", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 30, 0, true, false));
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (has(target, StatusEffect.BLEED)) {
            applyScaled(attacker, StatusEffect.POISE, 1, 1, attacker, self);
        }
        return amount;
    }
}
