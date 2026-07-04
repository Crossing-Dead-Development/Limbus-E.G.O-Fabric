package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 綠光果實：攻擊施加震顫 2·2。 */
public class GreenSpirit extends BaseGift {

    public GreenSpirit() {
        super("green_spirit", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.TREMOR, 2, 2, attacker, self);
        return amount;
    }
}
