package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 塵歸塵：攻擊燒傷中目標時疊加燒傷 2·1。 */
public class AshesToAshes extends BaseGift {

    public AshesToAshes() {
        super("ashes_to_ashes", 1); // Tier I
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (has(target, StatusEffect.BURN)) {
            applyScaled(target, StatusEffect.BURN, 2, 1, attacker, self);
        }
        return amount;
    }
}
