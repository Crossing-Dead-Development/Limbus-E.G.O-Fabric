package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 狐雨：晴天每 5 秒獲得迅捷 2·2，雨天再生 II；雨天攻擊獲得強壯 2·1；雷雨中免疫落雷。 */
public class Sunshower extends BaseGift {

    public Sunshower() {
        super("sunshower", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        boolean raining = player.getWorld().isRaining() || player.getWorld().isThundering();
        if (raining) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 120, 1, true, false));
        } else if (gate(player, 5000)) {
            applyScaled(player, StatusEffect.HASTE, 2, 2, player, self);
        }
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (attacker.getWorld().isRaining() || attacker.getWorld().isThundering()) {
            applyScaled(attacker, StatusEffect.POWER, 2, 1, attacker, self);
        }
        return amount;
    }

    @Override
    protected float onAnyDamage(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount,
                                DamageSource source) {
        if (victim.getWorld().isThundering() && source.isOf(DamageTypes.LIGHTNING_BOLT)) {
            return 0f;
        }
        return amount;
    }
}
