package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 強韌丸：受擊獲得守護 2·2。 */
public class TenacityBolus extends BaseGift {

    public TenacityBolus() {
        super("tenacity_bolus", 2); // Tier II
    }

    @Override
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        applyScaled(victim, StatusEffect.PROTECTION, 2, 2, victim, self);
        return amount;
    }
}
