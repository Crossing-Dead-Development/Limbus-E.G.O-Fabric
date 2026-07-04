package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 安息：靜止時生命再生 I；攻擊沉淪中目標 +15% 傷害（隨升級，上限 30%）。 */
public class Rest extends BaseGift {

    public Rest() {
        super("rest", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        if (player.getVelocity().length() < 0.05) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 30, 0, true, false));
        }
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (has(target, StatusEffect.SINKING)) {
            dmg *= (float) (1.0 + Math.min(0.30, 0.15 * multiplier(self)));
        }
        return dmg;
    }
}
