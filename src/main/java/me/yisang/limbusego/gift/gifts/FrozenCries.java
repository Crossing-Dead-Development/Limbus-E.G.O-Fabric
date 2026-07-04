package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 冰封的哀號：受擊時對攻擊者施加沉淪 3·2。 */
public class FrozenCries extends BaseGift {

    public FrozenCries() {
        super("frozen_cries", 2); // Tier II
    }

    @Override
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        if (attacker != null) {
            applyScaled(attacker, StatusEffect.SINKING, 3, 2, victim, self);
        }
        return amount;
    }
}
