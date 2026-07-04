package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 卡蜜拉：攻擊滿血目標時 +20% 傷害（隨升級，上限 30%）。 */
public class Carmilla extends BaseGift {

    public Carmilla() {
        super("carmilla", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (target.getHealth() >= target.getMaxHealth() - 0.01f) {
            return amount * (float) (1.0 + Math.min(0.30, 0.20 * multiplier(self)));
        }
        return amount;
    }
}
