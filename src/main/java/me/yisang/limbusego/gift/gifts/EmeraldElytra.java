package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 綠色鞘翅：被動緩降；疾跑中攻擊獲得呼吸法 3·2。 */
public class EmeraldElytra extends BaseGift {

    public EmeraldElytra() {
        super("emerald_elytra", 1); // Tier I
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 30, 0, true, false));
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (attacker.isSprinting()) {
            applyScaled(attacker, StatusEffect.POISE, 3, 2, attacker, self);
        }
        return amount;
    }
}
