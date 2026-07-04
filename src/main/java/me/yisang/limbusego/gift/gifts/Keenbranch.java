package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 磨尖的樹枝：攻擊 20% 機率（隨升級）+30% 傷害並獲得呼吸法 1·1。 */
public class Keenbranch extends BaseGift {

    public Keenbranch() {
        super("keenbranch", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (Math.random() < Math.min(1.0, 0.20 * multiplier(self))) {
            dmg *= 1.30f;
            apply(attacker, StatusEffect.POISE, 1, 1, attacker);
        }
        return dmg;
    }
}
