package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 異想狩獵：攻擊 20% 機率（隨升級）獲得強壯 2·2。 */
public class IllusoryHunt extends BaseGift {

    public IllusoryHunt() {
        super("illusory_hunt", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (Math.random() < Math.min(1.0, 0.20 * multiplier(self))) {
            apply(attacker, StatusEffect.POWER, 2, 2, attacker);
        }
        return amount;
    }
}
