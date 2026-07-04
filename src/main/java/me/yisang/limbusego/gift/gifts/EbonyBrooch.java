package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 黑檀胸針：被動夜視；攻擊施加破裂 2·2，15% 機率追加束縛 1·2。 */
public class EbonyBrooch extends BaseGift {

    public EbonyBrooch() {
        super("ebony_brooch", 2); // Tier II
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 30, 0, true, false));
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.RUPTURE, 2, 2, attacker, self);
        if (Math.random() < 0.15) {
            apply(target, StatusEffect.BIND, 1, 2, attacker);
        }
        return amount;
    }
}
