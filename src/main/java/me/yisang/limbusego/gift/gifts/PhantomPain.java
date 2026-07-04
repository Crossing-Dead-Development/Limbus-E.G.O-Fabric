package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 幻痛：攻擊 +15% 傷害（隨升級，上限 30%）。 */
public class PhantomPain extends BaseGift {

    public PhantomPain() {
        super("phantom_pain", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        return amount * (float) (1.0 + Math.min(0.30, 0.15 * multiplier(self)));
    }
}
